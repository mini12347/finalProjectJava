package Controllers;

import com.sothawo.mapjfx.Coordinate;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Scanner;

public class WebMapController implements Initializable {

    @FXML private WebView webView;
    @FXML private Button confirmButton;

    private JavaConnector javaConnector;
    private Coordinate selectedCoordinate;
    private String selectedAddress;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        WebEngine webEngine = webView.getEngine();

        // Charger le fichier HTML de la carte
        String htmlContent = loadHtmlFromResource("/fxml/map.html");
        webEngine.loadContent(htmlContent);

        // Attendre que la page soit chargée
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == javafx.concurrent.Worker.State.SUCCEEDED) {
                // Créer et connecter le JavaConnector
                javaConnector = new JavaConnector(webEngine);
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaConnector", javaConnector);
            }
        });

        // Configurer le bouton de confirmation
        confirmButton.setOnAction(e -> {
            if (javaConnector != null) {
                selectedCoordinate = javaConnector.getSelectedCoordinate();
                selectedAddress = javaConnector.getSelectedAddress();
            }

            // Fermer la fenêtre
            Stage stage = (Stage) confirmButton.getScene().getWindow();
            stage.close();
        });
    }

    private String loadHtmlFromResource(String resourcePath) {
        try (InputStream is = getClass().getResourceAsStream(resourcePath);
             Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name())) {
            return scanner.useDelimiter("\\A").next();
        } catch (Exception e) {
            e.printStackTrace();
            return "<html><body>Erreur lors du chargement de la carte</body></html>";
        }
    }

    public Optional<Coordinate> getSelectedCoordinate() {
        return Optional.ofNullable(selectedCoordinate);
    }

    public Optional<String> getSelectedAddress() {
        return Optional.ofNullable(selectedAddress);
    }
}