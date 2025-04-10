package org.openjfx.finalproject;
import Connection.ConxDB;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class App extends Application {
    @Override
    public  void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/landingPage.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/landingPage.css")).toExternalForm());
        primaryStage.setTitle("Gestion Auto-École");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void stop() throws Exception {
        // Fermer la connexion à la base de données lors de la fermeture de l'application
        ConxDB.closeConnection();

        // Appel à la méthode parente
        super.stop();
    }

}