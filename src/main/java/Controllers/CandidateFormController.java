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

        if (nomField.getText() == null || nomField.getText().trim().isEmpty()) {
            errorMessage += "Le nom est obligatoire\n";
        }
        if (prenomField.getText() == null || prenomField.getText().trim().isEmpty()) {
            errorMessage += "Le prénom est obligatoire\n";
        }
        if (cinField.getText() == null || cinField.getText().trim().isEmpty()) {
            errorMessage += "Le CIN est obligatoire\n";
        } else {
            try {
                Integer.parseInt(cinField.getText());
            } catch (NumberFormatException e) {
                errorMessage += "Le CIN doit être un nombre valide\n";
            }
        }
        if (telephoneField.getText() == null || telephoneField.getText().trim().isEmpty()) {
            errorMessage += "Le numéro de téléphone est obligatoire\n";
        } else {
            try {
                Integer.parseInt(telephoneField.getText());
            } catch (NumberFormatException e) {
                errorMessage += "Le numéro de téléphone doit être un nombre valide\n";
            }
        }
        if (emailField.getText() == null || emailField.getText().trim().isEmpty()) {
            errorMessage += "L'email est obligatoire\n";
        } else if (!isValidEmail(emailField.getText())) {
            errorMessage += "L'email n'est pas valide\n";
        }
        if (dateNaissancePicker.getValue() == null) {
            errorMessage += "La date de naissance est obligatoire\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            showAlert(Alert.AlertType.ERROR, "Champs invalides", errorMessage);
            return false;
        }
    }

    /**
     * Valide le format de l'email
     */
    private boolean isValidEmail(String email) {
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
     * Récupère le candidat modifié ou créé
     */
    public Candidat getCandidat() {
        return candidate;
    }
}