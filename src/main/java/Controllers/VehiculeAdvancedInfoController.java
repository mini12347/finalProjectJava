package Controllers;

import DAO.AutoEcoleDAO;
import DAO.ReparationDAO;
import DAO.VehiculesDAO;
import Entities.Paiement;
import Entities.Reparation;
import Entities.TypeP;
import Entities.Vehicule;
import Service.FacturePDFGenerator;
import Service.PaiementService;
import Service.ReparationS;
import Service.VehiculesPDFGenerator;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VehiculeAdvancedInfoController {
    private static final Logger LOGGER = Logger.getLogger(VehiculeAdvancedInfoController.class.getName());
    private static final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @FXML public Button btnGenerateFacture;
    @FXML public Button generatePdfButton;
    @FXML private TextField matricule, amatricule, kilo;
    @FXML private ComboBox<String> type;
    @FXML private DatePicker datem;
    @FXML private Label kilor, datev, datelv, datea;
    @FXML private TableView<Reparation> reparationsTable;
    @FXML private TableColumn<Reparation, String> desColumn, datemColumn, MontantColumn, FactureColumn;
    @FXML private TableColumn<Reparation, Integer> kilColumn;
    @FXML private TableColumn<Reparation, Void> actionsColumn;
    @FXML private TextField desc, montant, kilom;
    @FXML private DatePicker date;

    private Vehicule v;
    private final ReparationS reparationS = new ReparationS();
    private String lastGeneratedPath = "";

    public void initialize(Vehicule v) {
        this.v = v;
        try {
            type.setItems(FXCollections.observableArrayList("CAMION", "VOITURE", "MOTO"));
            displayVehicleInfo();

            int currentYear = LocalDate.now().getYear();
            handleVisiteTechnique(v.getMatricule(), v.getType(), currentYear);
            handleAssuranceAndVidange(v.getDatem(), v.getKilometrage(), currentYear);

            loadReparations();
            disableInputs(true);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error initializing advanced info controller", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des données : " + e.getMessage());
        }
    }

    private void displayVehicleInfo() {
        try {
            String fullMatricule = v.getMatricule();
            int tunisIndex = fullMatricule.indexOf("ت");

            if (tunisIndex > 0) {
                matricule.setText(fullMatricule.substring(0, tunisIndex));

                int sIndex = fullMatricule.indexOf("س");
                if (sIndex > 0 && sIndex < fullMatricule.length() - 1) {
                    amatricule.setText(fullMatricule.substring(sIndex + 1));
                } else {
                    amatricule.setText(fullMatricule.substring(fullMatricule.length() - 2));
                }
            } else {
                matricule.setText(fullMatricule);
                amatricule.setText("");
            }

            if (v.getDatem() != null) {
                LocalDate localDate = v.getDatem().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                datem.setValue(localDate);
            }

            kilo.setText(String.valueOf(v.getKilometrage()));

            if (v.getType() != null) {
                type.setValue(v.getType().toString());
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error displaying vehicle info", e);
            throw e;
        }
    }

    private void handleVisiteTechnique(String matricule, TypeP type, int currentYear) {
        try {
            String numericPart = matricule.replaceAll("[^0-9]", "");
            if (numericPart.isEmpty()) {
                datelv.setText("Format d'immatriculation invalide");
                return;
            }

            int lastDigit = Integer.parseInt(numericPart.substring(numericPart.length() - 1));
            boolean isEven = lastDigit % 2 == 0;

            if (isEven && type != TypeP.MOTO) {
                datelv.setText("5 mars " + currentYear);
                if (LocalDate.now().isAfter(LocalDate.of(currentYear, 3, 5))) {
                    datelv.setStyle("-fx-text-fill: rgba(109,22,22,0.75);");
                }
            } else {
                datelv.setText("5 avril " + currentYear);
                if (LocalDate.now().isAfter(LocalDate.of(currentYear, 4, 5))) {
                    datelv.setStyle("-fx-text-fill: rgba(109,22,22,0.75);");
                }
            }
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Error parsing matricule", e);
            datelv.setText("Erreur immatriculation");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error calculating technical visit date", e);
            datelv.setText("Erreur de calcul");
        }
    }

    private void handleAssuranceAndVidange(Date dateMiseEnServiceDate, int kilometrage, int currentYear) {
        try {
            LocalDate dateMiseEnService = dateMiseEnServiceDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            int ageVehicule = currentYear - dateMiseEnService.getYear();

            LocalDate prochaineVisite;
            if (ageVehicule < 4) {
                prochaineVisite = dateMiseEnService.plusYears(4);
            } else if (ageVehicule < 10) {
                prochaineVisite = dateMiseEnService.plusYears(((ageVehicule / 2) * 2) + 2);
            } else {
                prochaineVisite = LocalDate.now().plusMonths(6);
            }
            datev.setText(prochaineVisite.toString());

            int jour = dateMiseEnService.getDayOfMonth();
            int mois = dateMiseEnService.getMonthValue();
            int annee = LocalDate.now().getYear();

            int dernierJourMois = YearMonth.of(annee, mois).lengthOfMonth();
            jour = Math.min(jour, dernierJourMois);

            LocalDate dateAssurance = LocalDate.of(annee, mois, jour);
            if (dateAssurance.isBefore(LocalDate.now())) {
                dateAssurance = dateAssurance.plusYears(1);
            }
            datea.setText(dateAssurance.toString());

            int prochaineVidange = 10000 - (kilometrage % 10000);
            kilor.setText(String.valueOf(prochaineVidange));

            if (prochaineVisite.isBefore(LocalDate.now().plusMonths(3))) {
                datev.setStyle("-fx-text-fill: rgba(109,22,22,0.75);");
            }

            if (dateAssurance.isBefore(LocalDate.now().plusMonths(1))) {
                datea.setStyle("-fx-text-fill: rgba(109,22,22,0.75);");
            }

            if (prochaineVidange < 1000) {
                kilor.setStyle("-fx-text-fill: rgba(109,22,22,0.75);");
            }

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error calculating maintenance dates", e);
            datev.setText("Erreur de calcul");
            datea.setText("Erreur de calcul");
            kilor.setText("Erreur");
        }
    }

    private void loadReparations() {
        try {
            reparationsTable.setMinHeight(150);
            reparationsTable.setMinWidth(500);
            reparationsTable.setPrefHeight(150);

            List<Reparation> reparations;
            try {
                reparations = reparationS.getReparationsParIdVehicule(v);
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error loading repairs", e);
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des réparations: " + e.getMessage());
                reparations = new ArrayList<>();
            }
            ObservableList<Reparation> observableReparations = FXCollections.observableArrayList(reparations);

            setupTableColumns();
            reparationsTable.setItems(observableReparations);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading repairs", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des réparations: " + e.getMessage());
        }
    }

    private void setupTableColumns() {
        desColumn.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getDescription()));
        datemColumn.setCellValueFactory(cd -> {
            Date d = cd.getValue().getDate();
            return new SimpleStringProperty(d != null ? DISPLAY_DATE_FORMAT.format(d) : "N/A");
        });
        MontantColumn.setCellValueFactory(cd -> new SimpleStringProperty(String.format("%.2f", cd.getValue().getCout())));
        kilColumn.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().getKilometrage()).asObject());
        FactureColumn.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getFactureScan()));

        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button viewButton = new Button("👁️");
            private final Button deleteButton = new Button("❌");
            private final HBox container = new HBox(5, viewButton, deleteButton);

            {
                String buttonStyle = "-fx-font-size: 10px; -fx-background-color: #3E4854; -fx-text-fill: white;";
                viewButton.setStyle(buttonStyle);
                deleteButton.setStyle(buttonStyle);

                viewButton.setTooltip(new Tooltip("Voir les détails"));
                deleteButton.setTooltip(new Tooltip("Supprimer"));

                viewButton.setOnAction(event -> {
                    Reparation r = getCurrentItem();
                    if (r != null) {
                        displayRepairDetails(r);
                    }
                });

                deleteButton.setOnAction(event -> {
                    Reparation r = getCurrentItem();
                    if (r != null) {
                        confirmAndDeleteRepair(r);
                    }
                });
            }

            private Reparation getCurrentItem() {
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

    private void displayRepairDetails(Reparation r) {
        desc.setText(r.getDescription());

        if (r.getDate() != null) {
            // Fix for date conversion
            try {
                // If r.getDate() returns java.util.Date
                LocalDate localDate = r.getDate().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                date.setValue(localDate);
            } catch (Exception e) {
                // Handle possible date conversion errors
                System.err.println("Error converting date: " + e.getMessage());
                date.setValue(null); // Set to empty/default if conversion fails
            }
        } else {
            date.setValue(null); // Handle null date
        }

        montant.setText(String.valueOf(r.getCout()));
        kilom.setText(String.valueOf(r.getKilometrage()));
        disableInputs(true);
    }

    private void confirmAndDeleteRepair(Reparation r) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Êtes-vous sûr de vouloir supprimer cette réparation ?",
                ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la réparation");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    reparationS.supprimerReparation(v, r);
                    reparationsTable.getItems().remove(r);
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Réparation supprimée avec succès");
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error deleting repair", e);
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la suppression: " + e.getMessage());
                }
            }
        });
    }

    public void disableInputs(boolean disable) {
        desc.setDisable(disable);
        date.setDisable(disable);
        montant.setDisable(disable);
        kilom.setDisable(disable);
    }

    @FXML
    public void update() {
        if (desc.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Veuillez d'abord sélectionner une réparation à modifier.");
            return;
        }
        disableInputs(false);
        if (btnGenerateFacture != null) {
            btnGenerateFacture.setDisable(false);
        }
    }

    @FXML
    public void save() {
        try {
            String description = desc.getText().trim();
            LocalDate repairDate = date.getValue();
            String montantText = montant.getText().trim();
            String kilomText = kilom.getText().trim();

            if (description.isEmpty() || repairDate == null || montantText.isEmpty() || kilomText.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez remplir tous les champs.");
                return;
            }

            if (repairDate.isAfter(LocalDate.now())) {
                showAlert(Alert.AlertType.ERROR, "Erreur de date", "La date ne peut pas être dans le futur.");
                return;
            }

            double montantValue = Double.parseDouble(montantText);
            int kilomValue = Integer.parseInt(kilomText);

            if (montantValue <= 0 || kilomValue <= 0) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Le montant et le kilométrage doivent être positifs.");
                return;
            }

            if (kilomValue > v.getKilometrage()) {
                showAlert(Alert.AlertType.WARNING, "Validation", "Le kilométrage de la réparation ne peut pas être supérieur au kilométrage actuel du véhicule.");
                return;
            }

            Reparation selectedRepair = reparationsTable.getSelectionModel().getSelectedItem();
            Date repairJavaDate = Date.from(repairDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

            if (selectedRepair != null) {
                selectedRepair.setDescription(description);
                selectedRepair.setDate(repairJavaDate);
                selectedRepair.setCout(montantValue);
                selectedRepair.setKilometrage(kilomValue);

                reparationS.modifierReparation(v, selectedRepair);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Réparation mise à jour avec succès");
            } else {
                int newId = v.getReparations().isEmpty() ? 1 : v.getReparations().get(v.getReparations().size() - 1).getId() + 1;
                Reparation r = new Reparation(newId, v.getMatricule(), description, repairJavaDate, montantValue, kilomValue, "");

                try {
                    reparationS.ajouterReparation(v, r);
                    Paiement paiement = new Paiement(
                            0, // ID will be auto-generated
                            LocalDate.now(),
                            new Time(System.currentTimeMillis()),
                            0, // assuming Vehicule has a getIdClient()
                            "Paiement pour réparation ID " + r.getId(),
                            r.getCout(),
                            "en attente" // status
                    );

                    PaiementService paiementService = new PaiementService();
                    paiementService.ajouterPaiement(paiement);
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Réparation ajoutée avec succès");
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error adding repair", e);
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout: " + e.getMessage());
                    return;
                }
            }

            initialize(v);

        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Number format error", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Montant ou kilométrage invalide. Veuillez entrer des nombres valides.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error", e);
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'enregistrement : " + e.getMessage());
        }

        disableInputs(true);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void generateFact() {
        try {
            Reparation selectedRepair = reparationsTable.getSelectionModel().getSelectedItem();
            if (selectedRepair == null) {
                showAlert(Alert.AlertType.WARNING, "Aucune réparation sélectionnée",
                        "Veuillez sélectionner une réparation pour générer la facture.");
                return;
            }

            File factureDir = createInvoiceDirectory();
            if (factureDir == null) {
                return;
            }

            String outputPath = factureDir.getAbsolutePath() + File.separator +
                    "facture_reparation_" + selectedRepair.getId() + "_" +
                    v.getMatricule().replaceAll("[^a-zA-Z0-9]", "_") + ".pdf";

            String logoPath = getClass().getResource("/images/111-removebg-preview.png").toExternalForm();

            FacturePDFGenerator.generateFacture(
                    outputPath,
                    logoPath,
                    null,
                    selectedRepair.getMatriculeVehicule(),
                    selectedRepair.getDescription(),
                    DISPLAY_DATE_FORMAT.format(selectedRepair.getDate()),
                    selectedRepair.getCout(),
                    selectedRepair.getKilometrage()
            );

            selectedRepair.setFactureScan(outputPath);

            ReparationDAO reparationDAO = new ReparationDAO();
            reparationDAO.updateFacturePath(selectedRepair.getId(), outputPath);

            reparationsTable.refresh();

            lastGeneratedPath = outputPath;
            showAlert(Alert.AlertType.INFORMATION, "Succès",
                    "La facture a été générée avec succès :\n" + outputPath);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating invoice", e);
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Une erreur est survenue lors de la génération de la facture :\n" + e.getMessage());
        }
    }

    private File createInvoiceDirectory() {
        try {
            String userHome = System.getProperty("user.home");
            File factureDir = new File(userHome + File.separator + "Documents" +
                    File.separator + "AutoEcole" + File.separator + "Factures");

            if (!factureDir.exists()) {
                boolean dirCreated = factureDir.mkdirs();
                if (!dirCreated) {
                    showAlert(Alert.AlertType.ERROR, "Erreur",
                            "Impossible de créer le dossier de destination.");
                    return null;
                }
            }

            return factureDir;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating invoice directory", e);
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible de créer le dossier de destination: " + e.getMessage());
            return null;
        }
    }

    @FXML
    public void handleVisualizeFacture() {
        if (lastGeneratedPath.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Aucune facture",
                    "Aucune facture générée à visualiser.");
            return;
        }

        try {
            File file = new File(lastGeneratedPath);
            if (file.exists()) {
                Desktop.getDesktop().open(file);
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Le fichier n'existe pas: " + lastGeneratedPath);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error opening invoice", e);
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Impossible d'ouvrir le fichier : " + e.getMessage());
        }
    }

    @FXML
    public void cancel() {
        desc.clear();
        date.setValue(null);
        montant.clear();
        kilom.clear();
        disableInputs(true);

        if (btnGenerateFacture != null) {
            btnGenerateFacture.setDisable(true);
        }

        reparationsTable.getSelectionModel().clearSelection();
    }

    @FXML
    public void addReparation() {
        disableInputs(false);

        if (btnGenerateFacture != null) {
            btnGenerateFacture.setDisable(true);
        }

        date.setValue(LocalDate.now());
        desc.clear();
        montant.clear();
        kilom.clear();

        reparationsTable.getSelectionModel().clearSelection();
    }

    @FXML
    public void handleGeneratePDF(ActionEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le PDF");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
            );

            String safeMatricule = v.getMatricule().replaceAll("[^a-zA-Z0-9]", "_");
            fileChooser.setInitialFileName("vehicule_" + safeMatricule + ".pdf");

            Stage stage = (Stage) generatePdfButton.getScene().getWindow();
            File selectedFile = fileChooser.showSaveDialog(stage);

            if (selectedFile != null) {
                VehiculesPDFGenerator.generateSingleVehiculePDF(
                        v,
                        new AutoEcoleDAO().getLastModifiedAutoEcole(),
                        selectedFile.getAbsolutePath()
                );

                try {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(selectedFile);
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Could not open PDF automatically", e);
                }

                showAlert(Alert.AlertType.INFORMATION, "Génération PDF",
                        "Le fichier a été enregistré à: " + selectedFile.getAbsolutePath());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating PDF", e);
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Erreur lors de la génération du PDF: " + e.getMessage());
        }
    }
}