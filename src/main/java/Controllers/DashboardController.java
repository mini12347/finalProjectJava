package Controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML
    private Label totalCandidatsLabel;

    @FXML
    private Label tauxReussiteLabel;

    @FXML
    private Label tauxReussiteConduiteLabel;

    @FXML
    private Label revenusLabel;

    @FXML
    private Label moniteursLabel;

    @FXML
    private Label vehiculesLabel;

    @FXML
    private Label candidatsTypeALabel;

    @FXML
    private Label candidatsTypeBLabel;

    @FXML
    private Label candidatsTypeCLabel;

    @FXML
    private VBox alertsBox;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        chargerStatistiques();
        // Si vous souhaitez charger les alertes dynamiquement, décommentez la ligne suivante
        // chargerAlertes();
    }

    private void chargerStatistiques() {
        // Ici vous devez remplacer ces valeurs par des données réelles de votre base de données
        // en utilisant vos classes DAO

        // Exemple:
        // int totalCandidats = candidatDAO.getTotalCandidats();

        totalCandidatsLabel.setText("125");
        tauxReussiteLabel.setText("68.5%");
        tauxReussiteConduiteLabel.setText("72.8%");
        revenusLabel.setText("15750.50 DT");
        moniteursLabel.setText("8");
        vehiculesLabel.setText("12");
        candidatsTypeALabel.setText("20");
        candidatsTypeBLabel.setText("95");
        candidatsTypeCLabel.setText("10");
    }

    private void chargerAlertes() {
        // Vider les alertes existantes
        alertsBox.getChildren().clear();

        // Charger les alertes depuis la base de données
        // Exemple avec vos classes DAO:
        // List<Alerte> alertes = alerteDAO.getAlertesActives();
        // for (Alerte alerte : alertes) {
        //     ajouterAlerte(alerte.getMessage(), alerte.getType());
        // }

        // Exemples d'alertes
        ajouterAlerte("Véhicule TUN 8234 doit passer la visite technique dans 7 jours", "warning");
        ajouterAlerte("Assurance de Véhicule TUN 5643 expire dans 14 jours", "danger");
        ajouterAlerte("Vidange de Véhicule TUN 9876 prévue dans 200 km", "info");
    }

    private void ajouterAlerte(String message, String type) {
        HBox alertBox = new HBox();
        alertBox.getStyleClass().addAll("alert", "alert-" + type);
        alertBox.setPadding(new javafx.geometry.Insets(15, 15, 15, 15));

        Label alertLabel = new Label(message);
        alertBox.getChildren().add(alertLabel);

        alertsBox.getChildren().add(alertBox);
    }
}