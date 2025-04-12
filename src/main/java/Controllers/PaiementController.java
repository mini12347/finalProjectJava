package Controllers;

import Entities.Paiement;
import Entities.ParFacilite;
import Service.PaiementService;
import Service.PaiementPdfGenerator;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.util.List;
import java.util.stream.Collectors;

public class PaiementController {
    @FXML
    private TextField cinSearchField;

    @FXML
    private TableView<Paiement> paiementTableView;

    @FXML
    private TableColumn<Paiement, Integer> idPaiementColumn;
    @FXML
    private TableColumn<Paiement, Date> dateColumn;
    @FXML
    private TableColumn<Paiement, Time> heureColumn;
    @FXML
    private TableColumn<Paiement, Integer> idClientColumn;
    @FXML
    private TableColumn<Paiement, String> descriptionColumn;
    @FXML
    private TableColumn<Paiement, Double> montantColumn;
    @FXML
    private TableColumn<Paiement, String> etatColumn;
    @FXML
    private TableColumn<Paiement, Double> accompteColumn;
    @FXML
    private TableColumn<Paiement, String> montantsColumn;
    @FXML
    private TableColumn<Paiement, Void> choixColumn;
    @FXML
    private TableColumn<Paiement, Void> pdfColumn;  // New column for PDF generation

    private PaiementService paiementService;
    private PaiementPdfGenerator pdfGenerator;

    public PaiementController() throws SQLException {
        paiementService = new PaiementService();
        pdfGenerator = new PaiementPdfGenerator();
    }

    @FXML
    public void initialize() {
        // Initialize table columns
        idPaiementColumn.setCellValueFactory(new PropertyValueFactory<>("idPaiement"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        heureColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        idClientColumn.setCellValueFactory(new PropertyValueFactory<>("idClient"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        montantColumn.setCellValueFactory(new PropertyValueFactory<>("montant"));
        etatColumn.setCellValueFactory(new PropertyValueFactory<>("etat"));

        // Setup Par Facilite columns
        accompteColumn.setCellValueFactory(cellData -> {
            Paiement paiement = cellData.getValue();
            ParFacilite parFacilite = paiement.getParFacilite();
            return parFacilite != null ?
                    new SimpleObjectProperty<>(parFacilite.getAccompte()) :
                    new SimpleObjectProperty<>(null);
        });

        montantsColumn.setCellValueFactory(cellData -> {
            Paiement paiement = cellData.getValue();
            ParFacilite parFacilite = paiement.getParFacilite();
            return parFacilite != null ?
                    new SimpleStringProperty(formatMontants(parFacilite.getMontans())) :
                    new SimpleStringProperty("");
        });

        // Setup Choix column with a "Par Facilité" button
        choixColumn.setCellFactory(createPaymentButtonCell());

        // Setup PDF column with "Générer PDF" button
        pdfColumn.setCellFactory(createPdfButtonCell());
    }

    // Helper method to format montants for display
    private String formatMontants(List<Double> montants) {
        if (montants == null || montants.isEmpty()) {
            return "Aucune tranche";
        }
        return montants.stream()
                .map(m -> String.format("%.2f", m))
                .collect(Collectors.joining(", "));
    }

    // Method to create the cell factory for the choice column
    private Callback<TableColumn<Paiement, Void>, TableCell<Paiement, Void>> createPaymentButtonCell() {
        return param -> new TableCell<>() {
            private final Button parFaciliteButton = new Button("Par Facilité");

            {
                parFaciliteButton.setOnAction(event -> {
                    Paiement paiement = getTableView().getItems().get(getIndex());
                    handleParFaciliteInitiation(paiement);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    // Get the current Paiement from the table view
                    Paiement paiement = getTableView().getItems().get(getIndex());

                    // Only show the button if the payment is in 'en attente' state
                    setGraphic(paiement.getEtat().equals("en attente") ? parFaciliteButton : null);
                }
            }
        };
    }

    // Method to create the cell factory for the PDF column
    private Callback<TableColumn<Paiement, Void>, TableCell<Paiement, Void>> createPdfButtonCell() {
        return param -> new TableCell<>() {
            private final Button pdfButton = new Button("Générer PDF");

            {
                pdfButton.setOnAction(event -> {
                    Paiement paiement = getTableView().getItems().get(getIndex());
                    generatePdfForPayment(paiement);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pdfButton);
                }
            }
        };
    }

    // Method to handle PDF generation
    private void generatePdfForPayment(Paiement paiement) {
        try {
            String generatedPdfPath = pdfGenerator.generatePaiementRecu(paiement);

            if (generatedPdfPath != null) {
                // Ask user where to save the PDF
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Enregistrer le reçu de paiement");
                fileChooser.setInitialFileName("paiement_" + paiement.getIdPaiement() + ".pdf");
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
                );

                File selectedFile = fileChooser.showSaveDialog(new Stage());

                if (selectedFile != null) {
                    // Copy the generated PDF to the selected location
                    Files.copy(Paths.get(generatedPdfPath), selectedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    // Show success message
                    showAlert("Succès", "Le reçu de paiement a été généré avec succès à: " + selectedFile.getAbsolutePath());

                    // Clean up the temporary file
                    Files.delete(Paths.get(generatedPdfPath));
                }
            } else {
                showAlert("Erreur", "Impossible de générer le PDF pour ce paiement.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Une erreur s'est produite lors de la génération du PDF: " + e.getMessage());
        }
    }

    // Method to handle par facilité payment initiation
    private void handleParFaciliteInitiation(Paiement paiement) {
        if (!"en attente".equals(paiement.getEtat())) {
            showAlert("Erreur", "Ce paiement ne peut pas être transformé en par facilité.");
            return;
        }

        // If payment already has par facilité details, show message
        if (paiement.getParFacilite() != null) {
            showAlert("Erreur", "Ce paiement est déjà en mode par facilité.");
            return;
        }

        // Initiate par facilité payment
        boolean success = paiementService.initiateParFacilitePayment(paiement);

        if (success) {
            showAlert("Succès", "Paiement par facilité initié avec succès.");
            // Refresh the table to show updated details
            rechercherPaiements();
        } else {
            showAlert("Erreur", "Échec de l'initiation du paiement par facilité.");
        }
    }

    @FXML
    public void rechercherPaiements() {
        try {
            int cin = Integer.parseInt(cinSearchField.getText());
            List<Paiement> paiements = paiementService.getPaiementsByCIN(cin);
            paiementTableView.getItems().setAll(paiements);
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer un CIN valide.");
        }
    }

    @FXML
    public void effectuerPaiement() {
        Paiement selectedPaiement = paiementTableView.getSelectionModel().getSelectedItem();

        if (selectedPaiement == null) {
            showAlert("Erreur", "Veuillez sélectionner un paiement.");
            return;
        }

        // Check payment state
        if ("effectué".equals(selectedPaiement.getEtat())) {
            showAlert("Erreur", "Vous avez déjà payé !!");
            return;
        }

        // Show password dialog
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PasswordDialog.fxml"));
            Parent root = loader.load();
            PasswordDialogController dialogController = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initStyle(StageStyle.UNDECORATED);
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

            // Check if user confirmed with password
            if (dialogController.isConfirmed()) {
                String password = dialogController.getPassword();

                // Determine if it's a par facilité payment
                boolean parFacilite = selectedPaiement.getParFacilite() != null;

                // Try to process payment with password
                boolean success = paiementService.effectuerPaiement(selectedPaiement, parFacilite, password);

                if (success) {
                    showAlert("Succès", "Paiement effectué avec succès.");
                    // Refresh the table
                    rechercherPaiements();
                } else {
                    showAlert("Erreur", "Mot de passe incorrect. Échec du paiement.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'ouverture de la boîte de dialogue de mot de passe.");
        }
    }

    @FXML
    public void reinitialiserSelection() {
        paiementTableView.getSelectionModel().clearSelection();
        paiementTableView.getItems().clear();
        cinSearchField.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}