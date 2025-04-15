package Controllers;

import DAO.AutoEcoleDAO;
import DAO.VehiculesDAO;
import Entities.TypeP;
import Entities.Vehicule;
import Service.VehiculesPDFGenerator;
import Service.VehiculesS;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VehiculesController {
    private static final Logger LOGGER = Logger.getLogger(VehiculesController.class.getName());

    private static final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final List<String> VALID_VEHICLE_TYPES = Arrays.asList("moto", "camion", "voiture");

    private static final String TUNIS_ARABIC = "تونس";
    private static final String SEEN_ARABIC = "س";
    private static final String TEH_ARABIC = "ت";

    @FXML public Label indication;
    @FXML public Button generatePdfButton;
    @FXML private TableView<Vehicule> vehiculesTable;
    @FXML private TableColumn<Vehicule, String> matriculeColumn, datemColumn, typeColumn;
    @FXML private TableColumn<Vehicule, Integer> kilometrageColumn;
    @FXML private TableColumn<Vehicule, Void> actionsColumn;
    @FXML private TextField matricule, amatricule, kilo, searchField;
    @FXML private DatePicker datem;
    @FXML private ComboBox<String> type;

    private final VehiculesS vehiculesS = new VehiculesS();
    private ObservableList<Vehicule> vehiculesList;
    private Vehicule currentVehicule;

    public void initialize() {
        try {
            List<Vehicule> vehicules = vehiculesS.getVehicules();
            vehiculesList = FXCollections.observableArrayList(vehicules);
            vehiculesTable.setItems(vehiculesList);

            setupTableColumns();

            type.setItems(FXCollections.observableArrayList("CAMION", "VOITURE", "MOTO"));

            resetFields();
            disableInputs(true);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error initializing vehicle controller", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des données: " + e.getMessage());
        }
    }

    private void setupTableColumns() {
        matriculeColumn.setCellValueFactory(new PropertyValueFactory<>("matricule"));

        datemColumn.setCellValueFactory(cellData -> {
            Date date = cellData.getValue().getDatem();
            return new SimpleStringProperty(date != null ? DISPLAY_DATE_FORMAT.format(date) : "N/A");
        });

        kilometrageColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getKilometrage()).asObject());

        typeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getType() != null
                        ? cellData.getValue().getType().toString()
                        : "N/A"));

        setupActionsColumn();
    }

    private void setupActionsColumn() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button viewButton = new Button("🔍");
            private final Button deleteButton = new Button("❌");
            private final Button advancedButton = new Button("🔧");
            private final Button pdfButton = new Button("📄");
            private final HBox container = new HBox(10, viewButton, deleteButton, advancedButton, pdfButton);

            {
                String buttonStyle = "-fx-font-size: 12px; -fx-background-color: rgba(62,72,84,0.75); " +
                        "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px; " +
                        "-fx-border-radius: 5px; -fx-cursor: hand;";
                viewButton.setStyle(buttonStyle);
                deleteButton.setStyle(buttonStyle);
                advancedButton.setStyle(buttonStyle);
                pdfButton.setStyle(buttonStyle);

                viewButton.setTooltip(new Tooltip("Voir les détails"));
                deleteButton.setTooltip(new Tooltip("Supprimer"));
                advancedButton.setTooltip(new Tooltip("Informations avancées"));
                pdfButton.setTooltip(new Tooltip("Générer PDF"));

                setupButtonActions();
            }

            private void setupButtonActions() {
                viewButton.setOnAction(event -> {
                    Vehicule vehicule = getCurrentItem();
                    if (vehicule != null) {
                        displayVehicleDetails(vehicule);
                    }
                });

                advancedButton.setOnAction(event -> {
                    Vehicule vehicule = getCurrentItem();
                    if (vehicule != null) {
                        openAdvancedInfoWindow(vehicule);
                    }
                });

                deleteButton.setOnAction(event -> {
                    Vehicule vehicule = getCurrentItem();
                    if (vehicule != null) {
                        confirmAndDeleteVehicle(vehicule);
                    }
                });

                pdfButton.setOnAction(event -> {
                    Vehicule vehicule = getCurrentItem();
                    if (vehicule != null) {
                        generateSingleVehiculePDF(vehicule);
                    }
                });
            }

            private Vehicule getCurrentItem() {
                int index = getIndex();
                return (index >= 0 && index < getTableView().getItems().size())
                        ? getTableView().getItems().get(index)
                        : null;
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    private void displayVehicleDetails(Vehicule vehicule) {
        try {
            currentVehicule = vehicule;

            String fullMatricule = vehicule.getMatricule();
            String numericPart = "";
            String yearPart = "";

            if (fullMatricule != null && !fullMatricule.isEmpty()) {
                int tunisIndex = fullMatricule.indexOf(TUNIS_ARABIC);
                if (tunisIndex > 0) {
                    numericPart = fullMatricule.substring(0, tunisIndex).replaceAll("[^0-9]", "").trim();
                }

                int seenIndex = fullMatricule.indexOf(SEEN_ARABIC);
                if (seenIndex >= 0 && seenIndex < fullMatricule.length() - 1) {
                    yearPart = fullMatricule.substring(seenIndex + 1).replaceAll("[^0-9]", "").trim();
                }

                if (yearPart.isEmpty() && tunisIndex > 0) {
                    String afterTunis = fullMatricule.substring(tunisIndex + TUNIS_ARABIC.length()).replaceAll("[^0-9]", "");
                    if (afterTunis.length() >= 2) {
                        yearPart = afterTunis.substring(afterTunis.length() - 2);
                    }
                }
            }

            matricule.setText(numericPart);
            amatricule.setText(yearPart);

            if (vehicule.getDatem() != null) {
                LocalDate localDate = vehicule.getDatem().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                datem.setValue(localDate);
            } else {
                datem.setValue(null);
            }
            kilo.setText(String.valueOf(vehicule.getKilometrage()));
            if (vehicule.getType() != null) {
                String vehicleType = vehicule.getType().toString().toUpperCase();
                if (type.getItems().contains(vehicleType)) {
                    type.setValue(vehicleType);
                } else {
                    type.setValue(null);
                }
            } else {
                type.setValue(null);
            }

            disableInputs(true);
            indication.setText("Détails du véhicule");

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error displaying vehicle details", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'affichage des détails du véhicule: " + e.getMessage());
        }
    }

    private void openAdvancedInfoWindow(Vehicule vehicule) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/VehiculesAdvancedInfo.fxml"));
            Parent root = loader.load();
            VehiculeAdvancedInfoController controller = loader.getController();
            controller.initialize(vehicule);

            Stage stage = new Stage();
            stage.setScene(new Scene(root, 1000, 600));
            stage.setTitle("Informations Avancées du Véhicule");
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error loading advanced info window", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement de la fenêtre d'informations avancées: " + e.getMessage());
        }
    }

    private void confirmAndDeleteVehicle(Vehicule vehicule) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Êtes-vous sûr de vouloir supprimer ce véhicule ? Cette action est irréversible.",
                ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le véhicule " + vehicule.getMatricule());

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    vehiculesS.deleteVehicule(vehicule.getMatricule());
                    Platform.runLater(() -> {
                        vehiculesList.remove(vehicule);
                        showAlert(Alert.AlertType.INFORMATION, "Succès", "Véhicule supprimé avec succès");

                        if (currentVehicule != null && currentVehicule.getMatricule().equals(vehicule.getMatricule())) {
                            resetFields();
                            disableInputs(true);
                            currentVehicule = null;
                        }
                    });
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error deleting vehicle", e);
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression du véhicule: " + e.getMessage());
                }
            }
        });
    }

    private void disableInputs(boolean disable) {
        matricule.setDisable(disable);
        amatricule.setDisable(disable);
        datem.setDisable(disable);
        kilo.setDisable(disable);
        type.setDisable(disable);
    }

    @FXML
    public void filterVehicles() {
        String query = searchField.getText().toLowerCase();

        if (query == null || query.isEmpty()) {
            vehiculesTable.setItems(vehiculesList);
            return;
        }

        ObservableList<Vehicule> filteredList = FXCollections.observableArrayList();
        for (Vehicule vehicule : vehiculesList) {
            if ((vehicule.getMatricule() != null && vehicule.getMatricule().toLowerCase().contains(query)) ||
                    (vehicule.getType() != null && vehicule.getType().toString().toLowerCase().contains(query))) {
                filteredList.add(vehicule);
            }
        }

        vehiculesTable.setItems(filteredList);
        indication.setText("Résultats de recherche pour: " + query);
    }

    @FXML
    public void sortByMatricule() {
        sortVehicules("matricule");
    }

    @FXML
    public void sortByDate() {
        sortVehicules("datem");
    }

    @FXML
    public void sortByKilometrage() {
        sortVehicules("kilometrage");
    }

    @FXML
    public void sortByType() {
        sortVehicules("type");
    }

    private void sortVehicules(String column) {
        try {
            List<Vehicule> vehicules = vehiculesS.getVehiculeOrdered(column, "DESC");
            vehiculesList.setAll(vehicules);
            vehiculesTable.setItems(vehiculesList);
            indication.setText("Trié par " + column);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error sorting vehicles", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du tri des véhicules: " + e.getMessage());
        }
    }

    @FXML
    public void addvehicule() {
        currentVehicule = null;
        disableInputs(false);
        clearFields();
        datem.setValue(LocalDate.now());
        amatricule.setText(String.format("%02d", LocalDate.now().getYear() % 100));
        indication.setText("Entrer les informations de la nouvelle véhicule");
    }

    @FXML
    public void update() {
        if (currentVehicule == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Veuillez d'abord sélectionner un véhicule à modifier.");
            return;
        }
        disableInputs(false);
        indication.setText("Modifier les informations du véhicule");
    }

    @FXML
    public void save() {
        if (matricule.isDisable()) {
            showAlert(Alert.AlertType.WARNING, "Action non autorisée", "Veuillez d'abord cliquer sur 'Ajouter' ou 'Modifier'.");
            return;
        }

        try {
            if (!validateFields()) {
                return;
            }

            Vehicule newVehicule = createVehicule();
            boolean isUpdate = currentVehicule != null;

            if (isUpdate) {
                vehiculesS.updateVehicule(newVehicule);
                int index = -1;
                for (int i = 0; i < vehiculesList.size(); i++) {
                    if (vehiculesList.get(i).getMatricule().equals(currentVehicule.getMatricule())) {
                        index = i;
                        break;
                    }
                }

                if (index >= 0) {
                    vehiculesList.set(index, newVehicule);
                }

                showAlert(Alert.AlertType.INFORMATION, "Succès", "Véhicule mis à jour avec succès");
            } else {
                vehiculesS.addVehicule(newVehicule);
                vehiculesList.add(newVehicule);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Véhicule ajouté avec succès");
            }

            resetFields();
            disableInputs(true);
            currentVehicule = null;
            vehiculesTable.refresh();

        } catch (ParseException e) {
            LOGGER.log(Level.WARNING, "Date parsing error", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Format de date invalide. Veuillez utiliser dd-MM-yyyy.");
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Number format error", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Valeur numérique invalide. Veuillez entrer des nombres valides.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de base de données: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur inattendue: " + e.getMessage());
        }
    }

    private boolean validateFields() {
        if (!areFieldsFilled()) {
            showAlert(Alert.AlertType.ERROR, "Champs incomplets", "Veuillez remplir tous les champs.");
            return false;
        }

        if (!isValidMatriculeFormat()) {
            showAlert(Alert.AlertType.ERROR, "Format invalide",
                    "Format du matricule invalide. 3 à 5 chiffres pour le matricule, 2 chiffres pour l'année.");
            return false;
        }

        if (datem.getValue() == null || datem.getValue().isAfter(LocalDate.now())) {
            showAlert(Alert.AlertType.ERROR, "Date invalide",
                    "La date ne peut pas être dans le futur.");
            return false;
        }

        if (!isValidType(type.getValue())) {
            showAlert(Alert.AlertType.ERROR, "Type invalide",
                    "Type de véhicule invalide. Utilisez : moto, camion ou voiture.");
            return false;
        }

        if (!isValidYear(amatricule.getText().trim())) {
            showAlert(Alert.AlertType.ERROR, "Année invalide",
                    "Année de matricule invalide. Elle ne peut pas être dans le futur.");
            return false;
        }

        try {
            int kilometrage = Integer.parseInt(kilo.getText().trim());
            if (kilometrage < 0) {
                showAlert(Alert.AlertType.ERROR, "Kilométrage invalide",
                        "Le kilométrage ne peut pas être négatif.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Kilométrage invalide",
                    "Le kilométrage doit être un nombre entier.");
            return false;
        }

        return true;
    }

    private boolean areFieldsFilled() {
        return !matricule.getText().trim().isEmpty()
                && !amatricule.getText().trim().isEmpty()
                && datem.getValue() != null
                && !kilo.getText().trim().isEmpty()
                && type.getValue() != null && !type.getValue().trim().isEmpty()
                && !"matricule".equals(matricule.getText())
                && !"AN".equals(amatricule.getText())
                && !"***** kilomètres".equals(kilo.getText());
    }

    private boolean isValidMatriculeFormat() {
        return matricule.getText().strip().matches("[0-9]{3,5}")
                && amatricule.getText().strip().matches("[0-9]{2}");
    }

    private boolean isValidYear(String yearText) {
        try {
            int year = Integer.parseInt(yearText);
            int currentYear = LocalDate.now().getYear() % 100;
            return year <= currentYear;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidType(String vehicleType) {
        if (vehicleType == null) return false;
        return VALID_VEHICLE_TYPES.contains(vehicleType.toLowerCase().trim());
    }

    private Vehicule createVehicule() throws ParseException {
        String baseMatricule = matricule.getText().trim();
        String anneeMatricule = amatricule.getText().trim();
        String fullMatricule;

        if (currentVehicule == null) {
            fullMatricule = baseMatricule + TUNIS_ARABIC + anneeMatricule;
        } else {
            fullMatricule = currentVehicule.getMatricule();
        }

        Date dateMiseEnService = Date.from(datem.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
        int kilometrage = Integer.parseInt(kilo.getText().trim());
        TypeP typeVehicule = VehiculesDAO.typeOf(type.getValue().trim());

        return new Vehicule(fullMatricule, dateMiseEnService, kilometrage, typeVehicule);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        showAlert(Alert.AlertType.ERROR, "Erreur", message);
    }

    @FXML
    public void cancel() {
        resetFields();
        disableInputs(true);
        currentVehicule = null;
        indication.setText("Opération annulée");
    }

    private void resetFields() {
        matricule.setText("matricule");
        amatricule.setText("AN");
        datem.setValue(null);
        kilo.setText("***** kilomètres");
        type.setValue(null);
    }

    private void clearFields() {
        matricule.clear();
        amatricule.clear();
        datem.setValue(null);
        kilo.clear();
        type.setValue(null);
    }

    @FXML
    public void generateAllVehiculesPDF() {
        try {
            if (vehiculesList.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Aucun véhicule",
                        "Il n'y a aucun véhicule à inclure dans le PDF.");
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le fichier PDF");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
            fileChooser.setInitialFileName("liste_vehicules.pdf");

            Stage stage = (Stage) vehiculesTable.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                VehiculesPDFGenerator.generateVehiculesPDF(vehiculesList,
                        new AutoEcoleDAO().getLastModifiedAutoEcole(),
                        file.getAbsolutePath());

                openPdfFile(file);
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Le fichier PDF a été généré avec succès!");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating PDF", e);
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur lors de la génération du PDF: " + e.getMessage());
        }
    }

    @FXML
    private void generateSingleVehiculePDF(Vehicule vehicule) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le fichier PDF");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));

            String safeMatricule = vehicule.getMatricule().replaceAll("[^a-zA-Z0-9]", "_");
            fileChooser.setInitialFileName("vehicule_" + safeMatricule + ".pdf");

            Stage stage = (Stage) vehiculesTable.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                VehiculesPDFGenerator.generateSingleVehiculePDF(vehicule,
                        new AutoEcoleDAO().getLastModifiedAutoEcole(),
                        file.getAbsolutePath());

                openPdfFile(file);
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "Le fichier PDF a été généré avec succès!");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating PDF", e);
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur lors de la génération du PDF: " + e.getMessage());
        }
    }

    private void openPdfFile(File file) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Information",
                        "Le fichier a été enregistré à: " + file.getAbsolutePath());
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error opening PDF file", e);
            showAlert(Alert.AlertType.INFORMATION, "Information",
                    "Le fichier a été enregistré, mais n'a pas pu être ouvert automatiquement: " + file.getAbsolutePath());
        }
    }
}