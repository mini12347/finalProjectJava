package Controllers;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ExamenController {
    @FXML
    private Button btnCodeExamen;

    @FXML
    private Button btnCodeConduite;

    @FXML
    private void ouvrirExamenCode(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ExamenCode.fxml"));
            AnchorPane page = loader.load();

            // Création d'une nouvelle fenêtre modale
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Examen Code");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(((Node) event.getSource()).getScene().getWindow());

            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Vous pouvez récupérer le contrôleur si nécessaire
            // ExamenCodeController controller = loader.getController();
            // controller.setDialogStage(dialogStage);

            // Afficher la fenêtre et attendre qu'elle soit fermée
            dialogStage.showAndWait();

            // Ici, vous pouvez ajouter du code pour traiter les résultats après fermeture

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de ExamenCode.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void ouvrirExamenConduite(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ExamenConduite.fxml"));
            AnchorPane page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Examen Conduite");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(((Node) actionEvent.getSource()).getScene().getWindow());

            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Vous pouvez récupérer le contrôleur si nécessaire
            // ExamenConduiteController controller = loader.getController();
            // controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de ExamenConduite.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }
}