package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javafx.application.Platform;
import java.awt.Desktop;
import java.io.File;

import Entities.Candidat;
import Service.CandidateService;
import Service.PdfService;

public class CandidateController {

    @FXML
    private TableView<Candidat> candidatesTable;
    @FXML private TableColumn<Candidat, String> nomColumn, prenomColumn, cinColumn, telephoneColumn, emailColumn;
    @FXML private TableColumn<Candidat, Void> colActions;
    @FXML private TextField searchField;


    private final CandidateService service = new CandidateService();
    private final PdfService pdfService = new PdfService();
    private final List<Candidat> candidates = new java.util.ArrayList<>();

    @FXML
    private void initialize() {
        // Configurez les colonnes
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        cinColumn.setCellValueFactory(new PropertyValueFactory<>("CIN"));
        telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("numTelephone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("mail"));

        // Configurez la colonne des actions
        colActions.setCellFactory(getActionColumnFactory());

        // Chargez les candidats
        loadCandidates();
        Platform.runLater(() -> {
            try {
                loadCandidates();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    //Rôle : Créer une colonne personnalisée avec des boutons d'actions pour chaque ligne du tableau
    private Callback<TableColumn<Candidat, Void>, TableCell<Candidat, Void>> getActionColumnFactory() {
        return param -> new TableCell<>() {
            private final Button btnEdit = new Button("Edit");
            private final Button btnDelete = new Button("Delete");
            private final Button btnShow = new Button("Show");
            private final Button btnPdf = new Button("PDF");

            {
                btnEdit.setOnAction(e -> handleEditAction(getTableRow().getItem()));
                btnDelete.setOnAction(e -> handleDeleteAction(getTableRow().getItem()));
                btnShow.setOnAction(e -> handleShowAction(getTableRow().getItem()));
                btnPdf.setOnAction(e -> handlePdfAction(getTableRow().getItem()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox hBox = new HBox(5, btnEdit, btnDelete, btnShow, btnPdf);
                    setGraphic(hBox);
                }
            }
        };
    }

    private void loadCandidates() {
        try {
            // Utilisez directement le service pour obtenir tous les candidats
            candidates.clear();
            List<Candidat> allCandidates = service.getAllCandidates();
            candidates.addAll(allCandidates);

            // Utilisez Platform.runLater pour s'assurer que la mise à jour de l'UI se fait sur le thread JavaFX
            Platform.runLater(() -> {
                candidatesTable.getItems().setAll(candidates);
                System.out.println("Nombre de candidats chargés : " + candidates.size());
            });
        } catch (SQLException e) {
            showAlert("Database Error", "Could not load candidates:\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleAddCandidate() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DialogView.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add New Candidate");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(candidatesTable.getScene().getWindow());

            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            CandidateFormController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            dialogStage.showAndWait();

            Candidat candidate = controller.getCandidat();
            if (candidate != null) {
                try {
                    int savedId = service.saveCandidate(candidate);

                    // Rechargez explicitement les candidats
                    loadCandidates();

                    // Message de succès
                    showAlert("Succès", "Candidat ajouté avec succès !", Alert.AlertType.INFORMATION);
                } catch (SQLException e) {
                    if (e.getMessage().contains("email")) {
                        showAlert("Erreur", "Un candidat avec cet email existe déjà.", Alert.AlertType.ERROR);
                    } else {
                        showAlert("Erreur de sauvegarde", e.getMessage(), Alert.AlertType.ERROR);
                    }
                }
            }
        } catch (IOException e) {
            showAlert("Erreur", "Erreur lors de l'ouverture du formulaire : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleDeleteAction(Candidat candidate) {
        if (candidate != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this candidate?", ButtonType.YES, ButtonType.NO);
            alert.setTitle("Confirm Deletion");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    try {
                        service.deleteCandidate(candidate.getCIN());
                        candidates.remove(candidate);
                        candidatesTable.getItems().setAll(candidates);
                    } catch (SQLException e) {
                        showAlert("Delete Error", "Could not delete candidate:\n" + e.getMessage(), Alert.AlertType.ERROR);
                    }
                }
            });
        } else {
            showAlert("Warning", "Please select a candidate to delete.", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText();
        if (!keyword.isEmpty()) {
            try {
                candidatesTable.getItems().setAll(service.searchCandidates(keyword));
            } catch (SQLException e) {
                showAlert("Search Error", "Search failed:\n" + e.getMessage(), Alert.AlertType.ERROR);
            }
        } else {
            loadCandidates();
        }
    }

    /**
     * Génère un PDF pour un candidat sélectionné
     */
    private void handlePdfAction(Candidat candidate) {
        if (candidate != null) {
            try {
                String filePath = pdfService.generateCandidatePdf(candidate);

                // Demander à l'utilisateur s'il souhaite ouvrir le PDF généré
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("PDF Généré");
                alert.setHeaderText("Le PDF a été généré avec succès");
                alert.setContentText("Voulez-vous ouvrir le fichier PDF ?");

                ButtonType buttonTypeYes = new ButtonType("Oui");
                ButtonType buttonTypeNo = new ButtonType("Non");

                alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

                alert.showAndWait().ifPresent(buttonType -> {
                    if (buttonType == buttonTypeYes) {
                        try {
                            Desktop.getDesktop().open(new File(filePath));
                        } catch (IOException e) {
                            showAlert("Erreur", "Impossible d'ouvrir le fichier PDF :\n" + e.getMessage(), Alert.AlertType.ERROR);
                        }
                    }
                });

            } catch (IOException e) {
                showAlert("Erreur PDF", "Impossible de générer le PDF :\n" + e.getMessage(), Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Attention", "Veuillez sélectionner un candidat.", Alert.AlertType.WARNING);
        }
    }

    /**
     * Génère un PDF contenant la liste de tous les candidats
     */
    @FXML
    private void handleGenerateListPdf() {
        try {
            if (candidates.isEmpty()) {
                showAlert("Attention", "Aucun candidat à exporter.", Alert.AlertType.WARNING);
                return;
            }

            String filePath = pdfService.generateCandidatesListPdf(candidates);

            // Demander à l'utilisateur s'il souhaite ouvrir le PDF généré
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("PDF Généré");
            alert.setHeaderText("Le PDF de la liste des candidats a été généré avec succès");
            alert.setContentText("Voulez-vous ouvrir le fichier PDF ?");

            ButtonType buttonTypeYes = new ButtonType("Oui");
            ButtonType buttonTypeNo = new ButtonType("Non");

            alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

            alert.showAndWait().ifPresent(buttonType -> {
                if (buttonType == buttonTypeYes) {
                    try {
                        Desktop.getDesktop().open(new File(filePath));
                    } catch (IOException e) {
                        showAlert("Erreur", "Impossible d'ouvrir le fichier PDF :\n" + e.getMessage(), Alert.AlertType.ERROR);
                    }
                }
            });

        } catch (IOException e) {
            showAlert("Erreur PDF", "Impossible de générer le PDF de la liste :\n" + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }

    private void handleEditAction(Candidat candidate) {
        if (candidate != null) {
            try {
                // Updated FXML path
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DialogView.fxml"));
                AnchorPane page = loader.load();

                Stage dialogStage = new Stage();
                dialogStage.setTitle("Edit Candidate");
                dialogStage.initModality(Modality.WINDOW_MODAL);
                dialogStage.initOwner(candidatesTable.getScene().getWindow());

                Scene scene = new Scene(page);
                dialogStage.setScene(scene);

                CandidateFormController controller = loader.getController();
                controller.setDialogStage(dialogStage);
                controller.setCandidat(candidate);

                dialogStage.showAndWait();
                Candidat updatedCandidate = controller.getCandidat();
                if (updatedCandidate != null) {
                    updatedCandidate.setCIN(candidate.getCIN());
                    service.updateCandidate(updatedCandidate);
                    int index = candidates.indexOf(candidate);
                    if (index != -1) {
                        candidates.set(index, updatedCandidate);
                        candidatesTable.getItems().setAll(candidates);
                    }
                }
            } catch (IOException | SQLException e) {
                showAlert("Error", "Error updating candidate:\n" + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void handleShowAction(Candidat candidate) {
        if (candidate != null) {
            try {
                // Updated FXML path
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DetailedView.fxml"));
                AnchorPane page = loader.load();

                Stage dialogStage = new Stage();
                dialogStage.initModality(Modality.WINDOW_MODAL);
                dialogStage.initOwner(candidatesTable.getScene().getWindow());

                Scene scene = new Scene(page);
                dialogStage.setScene(scene);

                DisplayCandidateController controller = loader.getController();
                controller.setDialogStage(dialogStage);
                controller.setCandidate(candidate);

                dialogStage.showAndWait();
            } catch (IOException e) {
                showAlert("Error", "Error opening the form:\n" + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }
}