package Controllers;

import com.sothawo.mapjfx.*;
import com.sothawo.mapjfx.event.MapViewEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class MapChooserController implements Initializable {

    @FXML private MapView mapView;
    @FXML private Button confirmButton;

    private Coordinate selectedCoordinate;

    private final double MIN_LAT = 36.70;
    private final double MAX_LAT = 36.90;
    private final double MIN_LON = 10.05;
    private final double MAX_LON = 10.25;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        mapView.initialize(Configuration.builder()
                .showZoomControls(true)
                .projection(Projection.WEB_MERCATOR)
                .build());

        Coordinate tunis = new Coordinate(36.8065, 10.1815);
        selectedCoordinate = tunis;

        Marker marker = Marker.createProvided(Marker.Provided.BLUE).setVisible(true).setPosition(tunis);
        mapView.setCenter(tunis);
        mapView.setZoom(13);
        mapView.addMarker(marker);

        mapView.addEventHandler(MapViewEvent.MAP_CLICKED, event -> {
            Coordinate coord = event.getCoordinate().normalize();

            if (isInTunis(coord)) {
                selectedCoordinate = coord;
                mapView.removeMarker(marker);
                marker.setPosition(coord);
                mapView.addMarker(marker);
            } else {
                showAlert("Zone invalide", "Veuillez choisir un emplacement dans la zone de Tunis.");
            }
        });

        confirmButton.setOnAction(e -> {
            Stage stage = (Stage) confirmButton.getScene().getWindow();
            stage.close();
        });
    }

    private boolean isInTunis(Coordinate coord) {
        return coord.getLatitude() >= MIN_LAT && coord.getLatitude() <= MAX_LAT &&
                coord.getLongitude() >= MIN_LON && coord.getLongitude() <= MAX_LON;
    }

    private void showAlert(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Optional<Coordinate> getSelectedCoordinate() {
        return Optional.ofNullable(selectedCoordinate);
    }
    public static String getStreetAndAreaFromCoordinates(Coordinate coordinate) {
        try {
            double lat = coordinate.getLatitude();
            double lon = coordinate.getLongitude();
            String urlStr = "https://nominatim.openstreetmap.org/reverse?format=json&lat="
                    + lat + "&lon=" + lon + "&zoom=18&addressdetails=1";

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "JavaFXMapApp");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject json = new JSONObject(response.toString());
            JSONObject address = json.getJSONObject("address");

            String road = address.optString("road", "");
            String suburb = address.optString("suburb", "");
            String city = address.optString("city", address.optString("town", ""));
            String country = address.optString("country", "");

            return String.join(", ",
                    road.isEmpty() ? null : road,
                    suburb.isEmpty() ? null : suburb,
                    city.isEmpty() ? null : city,
                    country.isEmpty() ? null : country
            ).replaceAll(", null|^null, ", "").trim();
        } catch (Exception e) {
            e.printStackTrace();
            return "Adresse non trouvée";
        }
    }
}
