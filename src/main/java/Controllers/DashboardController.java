package Controllers;

import Service.DashboardService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

    private DashboardService dashboardService;
    private ScheduledExecutorService scheduledExecutorService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dashboardService = new DashboardService();

        // Charger les données immédiatement au démarrage
        chargerStatistiques();

        // Mettre en place un rafraîchissement périodique des données
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
                chargerStatistiques();
            });
        }, 30, 30, TimeUnit.SECONDS); // Rafraîchir toutes les 30 secondes
    }

    /**
     * Méthode appelée lorsque le contrôleur est détruit (par exemple, lors de la fermeture de la fenêtre)
     */
    public void shutdown() {
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
        }
    }

    /**
     * Charge toutes les statistiques depuis la base de données
     */
    private void chargerStatistiques() {
        try {
            totalCandidatsLabel.setText(dashboardService.getTotalCandidats());
            tauxReussiteLabel.setText(dashboardService.getTauxReussiteCode());
            tauxReussiteConduiteLabel.setText(dashboardService.getTauxReussiteConduite());
            revenusLabel.setText(dashboardService.getRevenusMensuels());
            moniteursLabel.setText(dashboardService.getNombreMoniteurs());
            vehiculesLabel.setText(dashboardService.getNombreVehicules());
            candidatsTypeALabel.setText(dashboardService.getCandidatsTypeA());
            candidatsTypeBLabel.setText(dashboardService.getCandidatsTypeB());
            candidatsTypeCLabel.setText(dashboardService.getCandidatsTypeC());
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des statistiques: " + e.getMessage());
            e.printStackTrace();
        }
    }
}