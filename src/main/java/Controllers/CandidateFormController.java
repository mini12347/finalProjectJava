package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import Entities.Candidat;
import Service.CandidateService;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;

public class CandidateFormController {

    @FXML
    private TextField nomField, prenomField, cinField, telephoneField, emailField;
    @FXML
    private DatePicker dateNaissancePicker;
    @FXML
    private ImageView cinImageView;

    private File selectedImageFile;
    private Stage dialogStage;
    private Candidat candidate;
    private boolean isEditMode = false;
    private CandidateService candidateService = new CandidateService();

    public CandidateFormController() throws SQLException {
    }

    /**
     * Définit la fenêtre de dialogue
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Définit le candidat à éditer
     */
    public void setCandidat(Candidat candidate) {
        this.candidate = candidate;
        this.isEditMode = (candidate != null && candidate.getCIN() != 0);

        if (isEditMode) {
            nomField.setText(candidate.getNom());
            prenomField.setText(candidate.getPrenom());
            cinField.setText(String.valueOf(candidate.getCIN()));
            cinField.setDisable(true); // Ne pas permettre de modifier le CIN lors de l'édition
            telephoneField.setText(String.valueOf(candidate.getNumTelephone()));
            emailField.setText(candidate.getMail());

            // Convertir java.util.Date en LocalDate pour DatePicker
            if (candidate.getDateNaissance() != null) {
                LocalDate localDate = candidate.getDateNaissance().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                dateNaissancePicker.setValue(localDate);
            }

            // Afficher l'image si disponible
            if (candidate.getCinImage() != null && candidate.getCinImage().length > 0) {
                try {
                    Image image = new Image(new ByteArrayInputStream(candidate.getCinImage()));
                    cinImageView.setImage(image);
                } catch (Exception e) {
                    System.err.println("Error displaying image: " + e.getMessage());
                }
            }
        } else {
            // Mode création - réinitialiser les champs
            cinField.setDisable(false);
        }
    }

    /**
     * Gère la sauvegarde du candidat
     */
    @FXML
    private void handleSave() {
        if (!validateInput()) {
            return;
        }

        try {
            // Si c'est un nouveau candidat
            if (!isEditMode) {
                candidate = new Candidat();
            }

            // Mise à jour des informations du candidat
            candidate.setNom(nomField.getText());
            candidate.setPrenom(prenomField.getText());

            int cin = Integer.parseInt(cinField.getText());
            // Si nouveau candidat, vérifier si CIN existe déjà
            if (!isEditMode && candidateService.candidateExists(cin)) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Ce CIN existe déjà");
                return;
            }
            candidate.setCIN(cin);

            candidate.setMail(emailField.getText());
            candidate.setNumTelephone(Integer.parseInt(telephoneField.getText()));

            // Convertir LocalDate du DatePicker en java.util.Date pour l'entité
            if (dateNaissancePicker.getValue() != null) {
                Date dateNaissance = Date.from(dateNaissancePicker.getValue()
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant());
                candidate.setDateNaissance(dateNaissance);
            }

            // Gérer l'image CIN
            if (selectedImageFile != null) {
                try {
                    byte[] imageBytes = Files.readAllBytes(selectedImageFile.toPath());
                    candidate.setCinImage(imageBytes);
                } catch (IOException e) {
                    System.err.println("Error reading image file: " + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de lire le fichier image");
                }
            }

            // Sauvegarder ou mettre à jour le candidat
            if (isEditMode) {
                candidateService.updateCandidate(candidate);
            } else {
                candidateService.saveCandidate(candidate);
            }

            dialogStage.close();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Format invalide",
                    "Veuillez vérifier les champs numériques (CIN, téléphone)");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données", e.getMessage());
        }
    }

    /**
     * Valide les entrées du formulaire
     */
    private boolean validateInput() {
        String errorMessage = "";

        // Validation du nom
        if (!isValidName(nomField.getText())) {
            errorMessage += "Le nom est invalide. Utilisez uniquement des caractères alphabétiques.\n";
        }

        // Validation du prénom
        if (!isValidName(prenomField.getText())) {
            errorMessage += "Le prénom est invalide. Utilisez uniquement des caractères alphabétiques.\n";
        }

        // Validation du CIN
        if (!isValidCIN(cinField.getText())) {
            errorMessage += "Le CIN doit être composé exactement de 8 chiffres.\n";
        }

        // Validation du numéro de téléphone
        if (!isValidPhoneNumber(telephoneField.getText())) {
            errorMessage += "Le numéro de téléphone doit être composé de 8 chiffres.\n";
        }

        // Validation de l'email
        if (!isValidEmail(emailField.getText())) {
            errorMessage += "L'email n'est pas valide.\n";
        }

        // Validation de l'âge
        if (!isValidAge(dateNaissancePicker.getValue())) {
            errorMessage += "L'âge doit être égal ou supérieur à 18 ans.\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            showAlert(Alert.AlertType.ERROR, "Champs invalides", errorMessage);
            return false;
        }
    }

    /**
     * Vérifie si le nom est valide (chaîne alphabétique)
     */
    private boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return name.matches("^[a-zA-ZÀ-ÿ\\s'-]+$");
    }

    /**
     * Vérifie si le CIN est valide (8 chiffres)
     */
    private boolean isValidCIN(String cin) {
        if (cin == null || cin.trim().isEmpty()) {
            return false;
        }
        return cin.matches("^\\d{8}$");
    }

    /**
     * Vérifie si le numéro de téléphone est valide (8 chiffres)
     */
    private boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return phone.matches("^\\d{8}$");
    }

    /**
     * Vérifie si l'âge est valide (≥ 18 ans)
     */
    private boolean isValidAge(LocalDate birthDate) {
        if (birthDate == null) {
            return false;
        }

        LocalDate currentDate = LocalDate.now();
        Period period = Period.between(birthDate, currentDate);

        return period.getYears() >= 18;
    }

    /**
     * Valide le format de l'email
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String regex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        return email.matches(regex);
    }

    /**
     * Gère le téléchargement d'image
     */
    @FXML
    private void handleImageUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image CIN");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        selectedImageFile = fileChooser.showOpenDialog(cinImageView.getScene().getWindow());

        if (selectedImageFile != null) {
            try {
                Image image = new Image(selectedImageFile.toURI().toString());
                cinImageView.setImage(image);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger l'image");
            }
        }
    }

    /**
     * Affiche une alerte
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Annule l'édition et ferme la fenêtre
     */
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    /**
     * Récupère le candidat modifié ou créé
     */
    public Candidat getCandidat() {
        return candidate;
    }
}