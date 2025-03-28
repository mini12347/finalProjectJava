package Controllers;

import Entities.Paiement;
import Entities.ParFacilite;
import Service.PaiementService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.sql.Date;
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

    private PaiementService paiementService;

    public PaiementController() {
        paiementService = new PaiementService();
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
        if (!"par facilité".equals(selectedPaiement.getEtat()) &&
                !"acompte payé".equals(selectedPaiement.getEtat()) &&
                !"en cours".equals(selectedPaiement.getEtat())) {
            showAlert("Erreur", "Ce paiement ne peut pas être payé dans son état actuel.");
            return;
        }

        // Determine if it's a par facilité payment
        boolean parFacilite = selectedPaiement.getParFacilite() != null;

        boolean success = paiementService.effectuerPaiement(selectedPaiement, parFacilite);

        if (success) {
            showAlert("Succès", "Paiement effectué avec succès.");
            // Refresh the table
            rechercherPaiements();
        } else {
            showAlert("Erreur", "Échec du paiement.");
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