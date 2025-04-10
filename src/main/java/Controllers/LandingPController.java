package Controllers;

import Service.NotificationService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.geometry.Insets;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class LandingPController {
    @FXML private Button btnDashboard;
    @FXML private Button btnAutoEcole;
    @FXML private Button btnCandidats;
    @FXML private Button btnVehicules;
    @FXML private Button btnMoniteurs;
    @FXML private Button btnSeances;
    @FXML private Button btnExamens;
    @FXML private Button btnPaiements;
    @FXML private Label currentDateTime;
    @FXML private StackPane contentArea;
    @FXML private VBox sideMenu, notificationPanel;
    @FXML private Button notificationButton;

    private boolean menuVisible = false;
    private boolean notificationVisible = false;
    private Button selectedButton = null;
    private NotificationService notificationService;

    @FXML
    public void initialize() {
        notificationService = new NotificationService();

        selectedButton = btnDashboard;
        updateButtonStyles();
        updateDateTime();
        showLandingP();

        // Configuration des gestionnaires d'événements pour les boutons spécifiques
        btnMoniteurs.setOnAction(event -> {
            if (selectedButton != null) {
                selectedButton.getStyleClass().remove("selected");
            }
            selectedButton = btnMoniteurs;
            updateButtonStyles();
            showMoniteurInterface();
        });

        btnPaiements.setOnAction(event -> {
            if (selectedButton != null) {
                selectedButton.getStyleClass().remove("selected");
            }
            selectedButton = btnPaiements;
            updateButtonStyles();
            showPaiementInterface();
        });

        btnCandidats.setOnAction(event -> {
            if (selectedButton != null) {
                selectedButton.getStyleClass().remove("selected");
            }
            selectedButton = btnCandidats;
            updateButtonStyles();
            showCandidatInterface();
        });
    }

    @FXML
    private void toggleMenu() {
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), sideMenu);
        transition.setToX(menuVisible ? -250 : 0);
        transition.play();
        menuVisible = !menuVisible;
    }

    @FXML
    private void handleNavigation(javafx.event.ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        if (selectedButton != null) {
            selectedButton.getStyleClass().remove("selected");
        }
        selectedButton = clickedButton;
        updateButtonStyles();

        // Determine which interface to show based on the clicked button
        if (clickedButton == btnDashboard) {
            showDashboard();
        } else if (clickedButton == btnCandidats) {
            showCandidatInterface();
        } else if (clickedButton == btnSeances) {
            showSeanceInterface();
        } else if (clickedButton == btnExamens) {
            showExamenInterface();
        }
    }

    /**
     * Affiche l'interface des moniteurs
     */
    @FXML
    public void showMoniteurInterface() {
        loadFXML("/fxml/Moniteur.fxml");
    }

    /**
     * Affiche l'interface des paiements
     */
    @FXML
    public void showPaiementInterface() {
        loadFXML("/fxml/Paiements.fxml");
    }

    /**
     * Affiche l'interface du tableau de bord
     */
    private void showDashboard() {
        loadFXML("/fxml/dashboard.fxml");
    }

    /**
     * Affiche l'interface des candidats
     */
    private void showCandidatInterface() {
        loadFXML("/fxml/MainView.fxml");
    }

    /**
     * Affiche l'interface des séances
     */
    private void showSeanceInterface() {
        loadFXML("/fxml/Seances.fxml");
    }

    /**
     * Affiche l'interface des examens
     */
    private void showExamenInterface() {
        loadFXML("/fxml/Examens.fxml");
    }

    private void updateDateTime() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            currentDateTime.setText(LocalDateTime.now().format(formatter));
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void updateButtonStyles() {
        Button[] buttons = {btnDashboard, btnAutoEcole, btnCandidats, btnVehicules, btnMoniteurs, btnSeances, btnExamens, btnPaiements};
        for (Button button : buttons) {
            button.getStyleClass().remove("selected");
        }
        if (selectedButton != null) {
            selectedButton.getStyleClass().add("selected");
        }
    }

    @FXML
    private void showNotifications() {
        // Afficher le panneau de notifications avec une animation
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), notificationPanel);
        transition.setToX(notificationVisible ? 420 : 0);
        transition.play();

        // Si on ouvre le panneau, charger les alertes
        if (!notificationVisible) {
            populateNotifications();
        }

        notificationVisible = !notificationVisible;
    }

    /**
     * Remplit le panneau des notifications avec les alertes
     */
    private void populateNotifications() {
        // Vider le panneau actuel
        notificationPanel.getChildren().clear();

        // Ajouter le titre
        Label titleLabel = new Label("Notifications urgentes");
        titleLabel.getStyleClass().add("notification-title");
        titleLabel.setPadding(new Insets(10, 10, 10, 10));
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        notificationPanel.getChildren().add(titleLabel);

        // Récupérer les alertes depuis le service
        List<Map<String, String>> alertes = notificationService.getAlertes();

        if (alertes.isEmpty()) {
            Label emptyLabel = new Label("Aucune notification pour le moment...");
            emptyLabel.setStyle("-fx-text-fill: #999999; -fx-font-style: italic;");
            emptyLabel.setPadding(new Insets(20, 10, 10, 10));
            notificationPanel.getChildren().add(emptyLabel);
        } else {
            // Ajouter chaque alerte au panneau
            for (Map<String, String> alerte : alertes) {
                VBox alertBox = createAlertBox(alerte);
                notificationPanel.getChildren().add(alertBox);
            }
        }
    }

    /**
     * Crée une boîte d'alerte formatée
     */
    private VBox createAlertBox(Map<String, String> alerte) {
        VBox alertBox = new VBox();
        alertBox.setPadding(new Insets(10));
        alertBox.setSpacing(5);
        alertBox.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

        // Définir l'icône en fonction du type d'alerte
        String icon;
        String alertStyle;

        switch (alerte.get("type")) {
            case "danger":
                icon = "⚠️";
                alertStyle = "-fx-text-fill: #D32F2F;";
                break;
            case "warning":
                icon = "⚠️";
                alertStyle = "-fx-text-fill: #F57C00;";
                break;
            default:
                icon = "ℹ️";
                alertStyle = "-fx-text-fill: #1976D2;";
                break;
        }

        // Créer l'en-tête avec l'icône
        HBox header = new HBox();
        header.setSpacing(10);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 16px;");

        Label messageLabel = new Label(alerte.get("message"));
        messageLabel.setStyle(alertStyle + "-fx-font-size: 14px; -fx-wrap-text: true;");
        messageLabel.setWrapText(true);
        HBox.setHgrow(messageLabel, Priority.ALWAYS);

        header.getChildren().addAll(iconLabel, messageLabel);
        alertBox.getChildren().add(header);

        return alertBox;
    }

    @FXML
    private void showAutoInfo() {
        loadFXML("/fxml/AutoEcoleInfos.fxml");
    }

    @FXML
    public void showLandingP() {
        loadFXML("/fxml/LandingPageContent.fxml");
    }

    @FXML
    public void ShowVehicules() {
        loadFXML("/fxml/Vehicules.fxml");
    }

    public void loadFXML(String fxmlPath) {
        try {
            URL resource = getClass().getResource(fxmlPath);
            if (resource == null) {
                System.err.println("FXML file not found: " + fxmlPath);
                return;
            }
            Parent view = FXMLLoader.load(resource);
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}