package Controllers;
import java.time.LocalDate;
import java.time.Period;

import DAO.VehiculesDAO;
import Entities.Moniteur;
import Entities.Vehicule;
import Entities.Disponibility;
import DAO.MoniteurDAO;
import DAO.DisponibilityDAO;
import Connection.ConxDB;
import Service.MoniteurPDFGenerator;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.util.regex.Pattern;

import com.lowagie.text.DocumentException;

public class MoniteurController {

    private MoniteurDAO moniteurDAO;
    private VehiculesDAO vehiculeDAO;
    private DisponibilityDAO disponibilityDAO;
    private MoniteurPDFGenerator pdfGenerator;

    @FXML
    private TextField cinField, nomField, prenomField, adresseField, mailField, numTelephoneField;
    @FXML
    private DatePicker dateNaissanceField;
    @FXML
    private ComboBox<Vehicule> vehiculeComboBox;
    @FXML
    private ComboBox<Disponibility> dispoComboBox;
    @FXML
    private TableView<Moniteur> moniteurTableView;
    @FXML
    private TableColumn<Moniteur, Integer> cinColumn;
    @FXML
    private TableColumn<Moniteur, String> nomColumn, prenomColumn, adresseColumn, mailColumn, numTelephoneColumn, dateNaissanceColumn, vehiculeColumn, dispoColumn;
    @FXML
    private Label nbMoniteursLabel;
    @FXML
    private Button genererPDFButton, genererPDFSelectionButton;

    // Patterns de validation
    private static final Pattern ALPHA_PATTERN = Pattern.compile("^[a-zA-ZÀ-ÿ\\s'-]+$");
    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^[0-9]+$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public MoniteurController() throws SQLException {
        this.moniteurDAO = new MoniteurDAO();
        this.vehiculeDAO = new VehiculesDAO();
        this.disponibilityDAO = new DisponibilityDAO();
        this.pdfGenerator = new MoniteurPDFGenerator();
    }

    public void initialize() {
        // Configuration des colonnes du tableau
        cinColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getCIN()).asObject());
        nomColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNom()));
        prenomColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPrenom()));

        // Assurez-vous que toutes les colonnes sont configurées
        if (adresseColumn != null) {
            adresseColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAdresse()));
        }
        if (mailColumn != null) {
            mailColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMail()));
        }
        if (numTelephoneColumn != null) {
            numTelephoneColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getNumTelephone())));
        }
        if (dateNaissanceColumn != null) {
            dateNaissanceColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                    cellData.getValue().getDateNaissance() != null ?
                            cellData.getValue().getDateNaissance().toString() : ""));
        }
        if (vehiculeColumn != null) {
            vehiculeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                    cellData.getValue().getVehicule() != null ?
                            String.valueOf(cellData.getValue().getVehicule().getMatricule()) : ""));
        }
        if (dispoColumn != null) {
            dispoColumn.setCellValueFactory(cellData -> {
                Disponibility dispo = cellData.getValue().getDisponibilite();
                return new SimpleStringProperty(dispo != null ? dispo.toString() : "Aucune");
            });
        }

        // Définir les factories et l'affichage des ComboBox
        initializeComboBoxes();

        // Charger les données de référence
        loadVehicules();
        loadDisponibilities();

        // IMPORTANT: Configurer le tableau pour qu'il prenne tout l'espace vertical disponible
        VBox.setVgrow(moniteurTableView, Priority.ALWAYS);

        // Écouter les sélections dans le tableau pour remplir le formulaire
        moniteurTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                fillFieldsWithMoniteur(newSelection);
            }
        });

        // Ajouter des listeners pour validation en temps réel
        addInputValidationListeners();

        // Charger initialement tous les moniteurs
        afficherMoniteurs();
    }

    /**
     * Ajoute des écouteurs pour valider les entrées en temps réel
     */
    private void addInputValidationListeners() {
        // Validation du CIN pendant la saisie
        cinField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !isValidCIN(newValue)) {
                cinField.setStyle("-fx-border-color: red;");
            } else {
                cinField.setStyle("");
            }
        });

        // Validation du nom pendant la saisie
        nomField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !isValidName(newValue)) {
                nomField.setStyle("-fx-border-color: red;");
            } else {
                nomField.setStyle("");
            }
        });

        // Validation du prénom pendant la saisie
        prenomField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !isValidName(newValue)) {
                prenomField.setStyle("-fx-border-color: red;");
            } else {
                prenomField.setStyle("");
            }
        });

        // Validation du numéro de téléphone pendant la saisie
        numTelephoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !isValidPhoneNumber(newValue)) {
                numTelephoneField.setStyle("-fx-border-color: red;");
            } else {
                numTelephoneField.setStyle("");
            }
        });

        // Validation de l'email pendant la saisie
        mailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty() && !isValidEmail(newValue)) {
                mailField.setStyle("-fx-border-color: red;");
            } else {
                mailField.setStyle("");
            }
        });

        // Validation de la date de naissance
        dateNaissanceField.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !isValidAge(newValue)) {
                dateNaissanceField.setStyle("-fx-border-color: red;");
            } else {
                dateNaissanceField.setStyle("");
            }
        });
    }

    // Initialisation des ComboBox (structure et affichage)
    private void initializeComboBoxes() {
        // Configuration de l'affichage des véhicules
        vehiculeComboBox.setCellFactory(param -> new ListCell<Vehicule>() {
            @Override
            protected void updateItem(Vehicule item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("Matricule: " + item.getMatricule() + " - Type: " + item.getType());
                }
            }
        });

        vehiculeComboBox.setConverter(new StringConverter<Vehicule>() {
            @Override
            public String toString(Vehicule vehicule) {
                if (vehicule == null) {
                    return null;
                }
                return "Matricule: " + vehicule.getMatricule() + " - Type: " + vehicule.getType();
            }

            @Override
            public Vehicule fromString(String string) {
                return null; // Non nécessaire pour un ComboBox en lecture seule
            }
        });

        // Configuration de l'affichage des disponibilités - version simplifiée
        dispoComboBox.setCellFactory(param -> new ListCell<Disponibility>() {
            @Override
            protected void updateItem(Disponibility item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
            }
        });

        dispoComboBox.setConverter(new StringConverter<Disponibility>() {
            @Override
            public String toString(Disponibility dispo) {
                return dispo == null ? null : dispo.toString();
            }

            @Override
            public Disponibility fromString(String string) {
                return null; // Non nécessaire pour un ComboBox en lecture seule
            }
        });
    }

    // Méthode pour charger les véhicules dans le ComboBox
    private void loadVehicules() {
        List<Vehicule> vehicules = vehiculeDAO.getAllVehicules();
        vehiculeComboBox.getItems().clear();
        vehiculeComboBox.getItems().addAll(vehicules);
    }

    // Méthode pour charger les disponibilités dans le ComboBox
    private void loadDisponibilities() {
        try {
            List<Disponibility> disponibilities = disponibilityDAO.getAllDisponibilities();
            dispoComboBox.getItems().clear();
            dispoComboBox.getItems().addAll(disponibilities);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des disponibilités : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace(); // Afficher la trace complète pour le débogage
        }
    }

    // Méthode utilitaire pour traduire les jours de la semaine en français
    private String translateDayOfWeek(DayOfWeek day) {
        switch (day) {
            case MONDAY:
                return "Lundi";
            case TUESDAY:
                return "Mardi";
            case WEDNESDAY:
                return "Mercredi";
            case THURSDAY:
                return "Jeudi";
            case FRIDAY:
                return "Vendredi";
            case SATURDAY:
                return "Samedi";
            case SUNDAY:
                return "Dimanche";
            default:
                return day.toString();
        }
    }

    // Méthode pour remplir le formulaire avec les données d'un moniteur sélectionné
    private void fillFieldsWithMoniteur(Moniteur moniteur) {
        cinField.setText(String.valueOf(moniteur.getCIN()));
        nomField.setText(moniteur.getNom());
        prenomField.setText(moniteur.getPrenom());
        adresseField.setText(moniteur.getAdresse());
        mailField.setText(moniteur.getMail());
        numTelephoneField.setText(String.valueOf(moniteur.getNumTelephone()));

        if (moniteur.getDateNaissance() != null) {
            // Conversion de java.sql.Date en LocalDate compatible avec toutes les versions de Java
            dateNaissanceField.setValue(LocalDate.parse(moniteur.getDateNaissance().toString()));
        } else {
            dateNaissanceField.setValue(null);
        }

        vehiculeComboBox.setValue(moniteur.getVehicule());

        // Find the matching disponibility in the combobox items
        Disponibility moniteurDispo = moniteur.getDisponibilite();
        if (moniteurDispo != null) {
            for (Disponibility dispo : dispoComboBox.getItems()) {
                if (dispo.getId() == moniteurDispo.getId()) {
                    dispoComboBox.setValue(dispo);
                    break;
                }
            }
        } else {
            dispoComboBox.setValue(null);
        }
    }

    /**
     * Méthode de validation du CIN (8 chiffres)
     */
    private boolean isValidCIN(String cin) {
        return NUMERIC_PATTERN.matcher(cin).matches() && cin.length() == 8;
    }

    /**
     * Méthode de validation du nom/prénom (alphabétique)
     */
    private boolean isValidName(String name) {
        return ALPHA_PATTERN.matcher(name).matches();
    }

    /**
     * Méthode de validation du numéro de téléphone (numérique)
     */
    private boolean isValidPhoneNumber(String number) {
        return NUMERIC_PATTERN.matcher(number).matches() && number.length() == 8;
    }

    /**
     * Méthode de validation de l'email
     */
    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Méthode de validation de l'âge (≥ 18 ans)
     */
    private boolean isValidAge(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears() >= 18;
    }

    /**
     * Valide toutes les entrées du formulaire
     * @return true si toutes les validations passent, false sinon
     */
    private boolean validateAllInputs() {
        StringBuilder errorMessage = new StringBuilder();

        // Validation du CIN
        if (cinField.getText().isEmpty()) {
            errorMessage.append("Le CIN est obligatoire.\n");
        } else if (!isValidCIN(cinField.getText())) {
            errorMessage.append("Le CIN doit contenir exactement 8 chiffres.\n");
        }

        // Validation du nom
        if (nomField.getText().isEmpty()) {
            errorMessage.append("Le nom est obligatoire.\n");
        } else if (!isValidName(nomField.getText())) {
            errorMessage.append("Le nom doit contenir uniquement des caractères alphabétiques.\n");
        }

        // Validation du prénom
        if (prenomField.getText().isEmpty()) {
            errorMessage.append("Le prénom est obligatoire.\n");
        } else if (!isValidName(prenomField.getText())) {
            errorMessage.append("Le prénom doit contenir uniquement des caractères alphabétiques.\n");
        }

        // Validation de l'adresse
        if (adresseField.getText().isEmpty()) {
            errorMessage.append("L'adresse est obligatoire.\n");
        }

        // Validation de l'email
        if (mailField.getText().isEmpty()) {
            errorMessage.append("L'email est obligatoire.\n");
        } else if (!isValidEmail(mailField.getText())) {
            errorMessage.append("Format d'email invalide.\n");
        }

        // Validation du numéro de téléphone
        if (numTelephoneField.getText().isEmpty()) {
            errorMessage.append("Le numéro de téléphone est obligatoire.\n");
        } else if (!isValidPhoneNumber(numTelephoneField.getText())) {
            errorMessage.append("Le numéro de téléphone doit contenir exactement 8 chiffres.\n");
        }

        // Validation de la date de naissance
        if (dateNaissanceField.getValue() == null) {
            errorMessage.append("La date de naissance est obligatoire.\n");
        } else if (!isValidAge(dateNaissanceField.getValue())) {
            errorMessage.append("Le moniteur doit être âgé d'au moins 18 ans.\n");
        }

        // Validation des sélections dans les combobox
        if (vehiculeComboBox.getValue() == null) {
            errorMessage.append("Veuillez sélectionner un véhicule.\n");
        }

        if (dispoComboBox.getValue() == null) {
            errorMessage.append("Veuillez sélectionner une disponibilité.\n");
        }

        // Si des erreurs ont été détectées, afficher l'alerte et retourner false
        if (errorMessage.length() > 0) {
            showAlert("Erreur de validation", errorMessage.toString(), Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    @FXML
    private void ajouterMoniteur(MouseEvent event) {
        try {
            // Validation de toutes les entrées
            if (!validateAllInputs()) {
                return;
            }

            // Convertir la date sélectionnée en java.sql.Date
            Date dateNaissance = Date.valueOf(dateNaissanceField.getValue());

            Moniteur moniteur = new Moniteur(
                    Integer.parseInt(cinField.getText()), nomField.getText(), prenomField.getText(),
                    adresseField.getText(), mailField.getText(), Integer.parseInt(numTelephoneField.getText()),
                    dateNaissance, vehiculeComboBox.getValue(), dispoComboBox.getValue()
            );

            moniteurDAO.ajouterMoniteur(moniteur);
            // Afficher tous les moniteurs après l'ajout
            afficherMoniteurs();
            showAlert("Succès", "Moniteur ajouté avec succès.", Alert.AlertType.INFORMATION);
            clearFields();
        } catch (NumberFormatException | SQLException e) {
            showAlert("Erreur", "Veuillez entrer des valeurs valides: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void supprimerMoniteur(MouseEvent event) throws SQLException {
        try {
            // Si le champ CIN n'est pas vide, utiliser cette valeur
            if (!cinField.getText().isEmpty()) {
                if (!isValidCIN(cinField.getText())) {
                    showAlert("Erreur", "Le CIN doit contenir exactement 8 chiffres.", Alert.AlertType.ERROR);
                    return;
                }

                int cin = Integer.parseInt(cinField.getText());
                // Vérifier d'abord si le moniteur existe
                Moniteur moniteur = moniteurDAO.chercherMoniteur(cin);
                if (moniteur != null) {
                    moniteurDAO.supprimerMoniteur(cin);
                    // Afficher tous les moniteurs après la suppression
                    afficherMoniteurs();
                    showAlert("Succès", "Moniteur avec CIN " + cin + " supprimé avec succès.", Alert.AlertType.INFORMATION);
                    clearFields();
                } else {
                    showAlert("Erreur", "Aucun moniteur trouvé avec le CIN " + cin, Alert.AlertType.WARNING);
                }
            } else {
                // Sinon, essayer d'utiliser le moniteur sélectionné dans le tableau
                Moniteur selectedMoniteur = moniteurTableView.getSelectionModel().getSelectedItem();
                if (selectedMoniteur != null) {
                    moniteurDAO.supprimerMoniteur(selectedMoniteur.getCIN());
                    // Afficher tous les moniteurs après la suppression
                    afficherMoniteurs();
                    showAlert("Succès", "Moniteur supprimé avec succès.", Alert.AlertType.INFORMATION);
                    clearFields();
                } else {
                    showAlert("Erreur", "Veuillez saisir un CIN ou sélectionner un moniteur.", Alert.AlertType.WARNING);
                }
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer un CIN valide (nombre entier).", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la suppression : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void rechercherMoniteurParId(MouseEvent event) {
        try {
            if (cinField.getText().isEmpty()) {
                showAlert("Erreur", "Veuillez entrer un CIN pour la recherche.", Alert.AlertType.WARNING);
                return;
            }

            if (!isValidCIN(cinField.getText())) {
                showAlert("Erreur", "Le CIN doit contenir exactement 8 chiffres.", Alert.AlertType.ERROR);
                return;
            }

            int cin = Integer.parseInt(cinField.getText());
            Moniteur moniteur = moniteurDAO.chercherMoniteur(cin);
            if (moniteur != null) {
                moniteurTableView.getItems().clear();
                moniteurTableView.getItems().add(moniteur);
                // Mettre à jour le compteur
                updateCounterLabel();
            } else {
                showAlert("Information", "Aucun moniteur trouvé avec le CIN " + cin, Alert.AlertType.INFORMATION);
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer un CIN valide (nombre entier).", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la recherche : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // Méthode principal pour afficher tous les moniteurs
    @FXML
    public void afficherMoniteurs() {
        try {
            List<Moniteur> moniteurs = moniteurDAO.afficherTousLesMoniteurs();
            moniteurTableView.getItems().clear();
            moniteurTableView.getItems().addAll(moniteurs);

            // Mettre à jour le compteur
            updateCounterLabel();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'affichage des moniteurs : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // Méthode pour générer un PDF pour tous les moniteurs
    @FXML
    private void genererPDF(MouseEvent event) {
        try {
            List<Moniteur> moniteurs = moniteurDAO.afficherTousLesMoniteurs();
            if (moniteurs.isEmpty()) {
                showAlert("Information", "Aucun moniteur à inclure dans le rapport.", Alert.AlertType.INFORMATION);
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le rapport PDF");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
            fileChooser.setInitialFileName("liste_moniteurs.pdf");

            File file = fileChooser.showSaveDialog(new Stage());
            if (file != null) {
                pdfGenerator.generatePDF(moniteurs, file.getAbsolutePath());
                showAlert("Succès", "Le rapport PDF a été généré avec succès.", Alert.AlertType.INFORMATION);
            }
        } catch (SQLException | IOException | DocumentException e) {
            showAlert("Erreur", "Erreur lors de la génération du PDF : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // Méthode pour générer un PDF pour le moniteur sélectionné
    @FXML
    private void genererPDFSelection(MouseEvent event) {
        Moniteur selectedMoniteur = moniteurTableView.getSelectionModel().getSelectedItem();
        if (selectedMoniteur == null) {
            showAlert("Erreur", "Veuillez sélectionner un moniteur pour générer le rapport.", Alert.AlertType.WARNING);
            return;
        }

        try {
            List<Moniteur> moniteurs = new ArrayList<>();
            moniteurs.add(selectedMoniteur);

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le rapport PDF");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
            fileChooser.setInitialFileName("moniteur_" + selectedMoniteur.getCIN() + ".pdf");

            File file = fileChooser.showSaveDialog(new Stage());
            if (file != null) {
                pdfGenerator.generatePDF(moniteurs, file.getAbsolutePath());
                showAlert("Succès", "Le rapport PDF pour le moniteur sélectionné a été généré avec succès.", Alert.AlertType.INFORMATION);
            }
        } catch (SQLException | IOException | DocumentException e) {
            showAlert("Erreur", "Erreur lors de la génération du PDF : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // Mettre à jour le label affichant le nombre de moniteurs
    private void updateCounterLabel() {
        if (nbMoniteursLabel != null) {
            int count = moniteurTableView.getItems().size();
            nbMoniteursLabel.setText("Nombre de moniteurs : " + count);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void clearFields() {
        cinField.clear();
        nomField.clear();
        prenomField.clear();
        adresseField.clear();
        mailField.clear();
        numTelephoneField.clear();
        dateNaissanceField.setValue(null);
        vehiculeComboBox.setValue(null);
        dispoComboBox.setValue(null);

        // Réinitialiser les styles pour retirer les indications d'erreur
        cinField.setStyle("");
        nomField.setStyle("");
        prenomField.setStyle("");
        adresseField.setStyle("");
        mailField.setStyle("");
        numTelephoneField.setStyle("");
        dateNaissanceField.setStyle("");
    }
}