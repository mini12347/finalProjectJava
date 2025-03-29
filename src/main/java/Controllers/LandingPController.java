package Controllers;
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
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private boolean menuVisible = false;
    private boolean notificationVisible = false;
    private Button selectedButton = null;
    @FXML
    public void initialize() {
        selectedButton = btnDashboard;
        updateButtonStyles();
        updateDateTime();
        showLandingP();
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
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), notificationPanel);
        transition.setToX(notificationVisible ? 420 : 0);
        transition.play();
        notificationVisible = !notificationVisible;
    }
    @FXML
    private void showAutoInfo() {
        loadFXML("/fxml/AutoEcoleInfos.fxml");
    }
    @FXML
    public void showLandingP() {
        loadFXML("/fxml/LandingPageContent.fxml" );
    }
    @FXML
    public void ShowVehicules() {
        loadFXML("/fxml/Vehicules.fxml");
    }
    public  void loadFXML(String fxmlPath) {
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
