package Controllers;

import com.sothawo.mapjfx.Coordinate;
import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import netscape.javascript.JSObject;

public class JavaConnector {
    private final WebEngine webEngine;
    private Coordinate selectedCoordinate;
    private String selectedAddress;

    public JavaConnector(WebEngine webEngine) {
        this.webEngine = webEngine;
    }

    /**
     * Cette méthode sera appelée depuis JavaScript
     */
    public void setLocation(double lat, double lng, String address) {
        // Enregistrer les coordonnées et l'adresse
        this.selectedCoordinate = new Coordinate(lat, lng);
        this.selectedAddress = address;

        // Afficher dans la console pour déboguer
        System.out.println("Position sélectionnée: " + lat + ", " + lng);
        System.out.println("Adresse: " + address);
    }

    public Coordinate getSelectedCoordinate() {
        return selectedCoordinate;
    }

    public String getSelectedAddress() {
        return selectedAddress;
    }
}