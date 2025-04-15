package Controllers;

import Service.DashboardService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Arc;

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

    @FXML
    private Arc codeReussiteArc;

    @FXML
    private Arc conduiteReussiteArc;

    @FXML
    private ProgressBar codeReussiteBar;

    @FXML
    private ProgressBar conduiteReussiteBar;

    @FXML
    private ProgressBar typeAProgressBar;

    @FXML
    private ProgressBar typeBProgressBar;

    @FXML
    private ProgressBar typeCProgressBar;

    @FXML
    private StackPane distributionGraphContainer;

    private DashboardService dashboardService;
    private ScheduledExecutorService scheduledExecutorService;
    private PieChart distributionPieChart;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dashboardService = new DashboardService();

        // Initialiser les arcs avec les bonnes valeurs de départ
        codeReussiteArc.setStartAngle(90);  // Commence à midi (90 degrés)
        conduiteReussiteArc.setStartAngle(90);

        // S'assurer que la longueur initiale est à 0
        codeReussiteArc.setLength(0);
        conduiteReussiteArc.setLength(0);

        // Initialiser le graphique pour la distribution des permis
        distributionPieChart = new PieChart();
        distributionPieChart.setLabelsVisible(true);
        distributionPieChart.setLegendVisible(true);
        distributionPieChart.setStyle("-fx-font-size: 10px;");
        distributionGraphContainer.getChildren().add(distributionPieChart);

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
            // Chargement des données textuelles
            totalCandidatsLabel.setText(dashboardService.getTotalCandidats());
            tauxReussiteLabel.setText(dashboardService.getTauxReussiteCode());
            tauxReussiteConduiteLabel.setText(dashboardService.getTauxReussiteConduite());
            revenusLabel.setText(dashboardService.getRevenusMensuels());
            moniteursLabel.setText(dashboardService.getNombreMoniteurs());
            vehiculesLabel.setText(dashboardService.getNombreVehicules());

            // Chargement des données de distribution par type de permis
            String typeA = dashboardService.getCandidatsTypeA();
            String typeB = dashboardService.getCandidatsTypeB();
            String typeC = dashboardService.getCandidatsTypeC();

            candidatsTypeALabel.setText(typeA);
            candidatsTypeBLabel.setText(typeB);
            candidatsTypeCLabel.setText(typeC);

            // Mise à jour des représentations graphiques pour les taux de réussite
            updateReussiteGraphique(tauxReussiteLabel.getText(), codeReussiteArc, codeReussiteBar);
            updateReussiteGraphique(tauxReussiteConduiteLabel.getText(), conduiteReussiteArc, conduiteReussiteBar);

            // Mise à jour des graphiques de distribution par type de permis
            updateDistributionGraphique(typeA, typeB, typeC);

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des statistiques: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Met à jour les représentations graphiques pour les taux de réussite
     * Méthode corrigée pour afficher correctement l'arc de progression
     */
    private void updateReussiteGraphique(String tauxTexte, Arc arc, ProgressBar progressBar) {
        try {
            // Extraire le pourcentage du texte
            String tauxNormalise = tauxTexte.replace("%", "").replace(",", ".").trim();
            double pourcentage = Double.parseDouble(tauxNormalise);

            // Calculer l'angle en degrés pour l'arc (sens anti-horaire)
            // La valeur maximale est -360 degrés pour un cercle complet
            double arcLength = -(pourcentage * 3.6); // 3.6 = 360/100

            // Mettre à jour l'arc avec animation
            // Pour le debug: afficher les valeurs calculées
            System.out.println("Taux: " + pourcentage + "%, Arc length: " + arcLength);

            // Appliquer la nouvelle longueur à l'arc
            arc.setLength(arcLength);

            // Mettre à jour la barre de progression
            progressBar.setProgress(pourcentage / 100.0);

        } catch (NumberFormatException e) {
            System.err.println("Erreur lors de la conversion du taux: " + e.getMessage());
            System.err.println("Valeur problématique: '" + tauxTexte + "'");
            arc.setLength(0);
            progressBar.setProgress(0);
        }
    }

    /**
     * Met à jour les graphiques de distribution par type de permis
     */
    private void updateDistributionGraphique(String typeA, String typeB, String typeC) {
        try {
            int nbTypeA = Integer.parseInt(typeA);
            int nbTypeB = Integer.parseInt(typeB);
            int nbTypeC = Integer.parseInt(typeC);

            // Calculer le total pour les pourcentages
            int total = nbTypeA + nbTypeB + nbTypeC;

            // Mettre à jour les barres de progression
            if (total > 0) {
                typeAProgressBar.setProgress((double) nbTypeA / total);
                typeBProgressBar.setProgress((double) nbTypeB / total);
                typeCProgressBar.setProgress((double) nbTypeC / total);
            } else {
                typeAProgressBar.setProgress(0);
                typeBProgressBar.setProgress(0);
                typeCProgressBar.setProgress(0);
            }

            // Mettre à jour le graphique en camembert
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
                    new PieChart.Data("Moto (" + nbTypeA + ")", nbTypeA),
                    new PieChart.Data("Voiture (" + nbTypeB + ")", nbTypeB),
                    new PieChart.Data("Camion (" + nbTypeC + ")", nbTypeC)
            );

            distributionPieChart.setData(pieChartData);

        } catch (NumberFormatException e) {
            System.err.println("Erreur lors de la conversion des valeurs: " + e.getMessage());
        }
    }
}