package Controllers;

import Entities.Candidat;
import Entities.Moniteur;
import Entities.Seance;
import Entities.SeanceConduite;
import Service.AutoEcoleInfosS;
import com.sothawo.mapjfx.Coordinate;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SeanceInfoController {

    @FXML private VBox rootVBox;
    @FXML private VBox secondVBOX;
    @FXML private Label typef, datef, heuref, candidatf, ingenieurf, lieuf, typePf;

    private Seance currentSeance;
    private Map<Integer, Moniteur> moniteurCache;
    private Map<Integer, Candidat> candidatCache;

    private ComboBox<String> typeComboBox;
    private DatePicker datePicker;
    private Button timeButton, locationButton;
    private Label heureLabel, lieuLabel;
    private VBox editableBox;
    private LocalTime selectedTime;
    private Coordinate selectedCoordinate;

    private List<javafx.scene.Node> originalContent;

    public void initialize(Seance s, Map<Integer, Moniteur> moniteurCache, Map<Integer, Candidat> candidatCache) {
        this.currentSeance = s;
        this.moniteurCache = moniteurCache;
        this.candidatCache = candidatCache;

        typef.setText(s.getType());
        datef.setText(s.getDate().toString());
        heuref.setText(s.getTime().toString());

        Moniteur m = moniteurCache.get(s.getIdMoniteur());
        Candidat c = candidatCache.get(s.getIdCandidat());

        candidatf.setText(c.getNom() + " " + c.getPrenom());
        ingenieurf.setText(m.getNom() + " " + m.getPrenom());

        if (s instanceof SeanceConduite) {
            try {
                String[] parts = s.getInfoSpecifique().split(",");
                Coordinate coord = new Coordinate(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
                lieuf.setText(MapChooserController.getStreetAndAreaFromCoordinates(coord));
                selectedCoordinate = coord;
            } catch (Exception e) {
                lieuf.setText("Coordonnées invalides");
            }
        } else {
            lieuf.setText(s.getInfoSpecifique());
        }

        typePf.setText(s.getTypePermis().toString());

        originalContent = new ArrayList<>(rootVBox.getChildren());
    }

    @FXML
    public void update() {
        typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("code", "conduite");
        typeComboBox.setValue(currentSeance.getType());

        datePicker = new DatePicker(currentSeance.getDate());

        selectedTime = currentSeance.getTime().toLocalTime();
        timeButton = new Button("⏰ Choisir l'heure");
        timeButton.setOnAction(event -> showTimePicker());

        locationButton = new Button("📍 Choisir un lieu");
        locationButton.setOnAction(event -> handleChooseLocation());

        heureLabel = new Label("Heure actuelle : " + currentSeance.getTime());
        lieuLabel = new Label("Lieu actuel : " + lieuf.getText());

        editableBox = new VBox(10,
                new Label("Type de séance"), typeComboBox,
                new Label("Date"), datePicker,
                new Label("Heure"), timeButton, heureLabel,
                new Label("Localisation (pour conduite)"), locationButton, lieuLabel,
                secondVBOX
        );

        rootVBox.getChildren().setAll(editableBox);
    }

    @FXML
    public void cancel() {
        if (originalContent != null) {
            rootVBox.getChildren().setAll(originalContent);
            initialize(currentSeance, moniteurCache, candidatCache);
        } else {
            showAlert("Erreur", "Impossible de réinitialiser l'interface.");
        }
    }

    @FXML
    public void save() {
        String type = typeComboBox.getValue();
        LocalDate date = datePicker.getValue();
        LocalTime time = selectedTime != null ? selectedTime : currentSeance.getTime().toLocalTime();

        currentSeance.setDate(date);
        currentSeance.setTime(Time.valueOf(time));

        if ("code".equals(type)) {
            currentSeance.setInfoSpecifique("Salle n°X");
        } else if (selectedCoordinate != null) {
            currentSeance.setInfoSpecifique(selectedCoordinate.getLatitude() + "," + selectedCoordinate.getLongitude());
        }

        if (originalContent != null) {
            rootVBox.getChildren().setAll(originalContent);
            initialize(currentSeance, moniteurCache, candidatCache);
        } else {
            showAlert("Erreur", "Impossible de réinitialiser l'interface.");
        }
    }

    private void showTimePicker() {
        try {
            List<String> times = generateTimeList();
            ChoiceDialog<String> dialog = new ChoiceDialog<>(times.get(0), times);
            dialog.setTitle("Heure de la séance");
            dialog.setHeaderText("Choisissez une heure");
            dialog.setContentText("Heure :");
            dialog.showAndWait().ifPresent(timeStr -> {
                timeButton.setText(timeStr);
                selectedTime = LocalTime.parse(timeStr);
                heureLabel.setText("Heure sélectionnée : " + timeStr);
            });
        } catch (Exception e) {
            showAlert("Erreur", "Veuillez sélectionner une date valide.");
        }
    }

    private List<String> generateTimeList() throws SQLException {
        List<String> timeOptions = new ArrayList<>();
        var daySchedule = new AutoEcoleInfosS().getTimeTable().getDaysOfWeek().get(datePicker.getValue().getDayOfWeek());

        LocalTime start = LocalTime.of(daySchedule.getStarthour(), 0);
        LocalTime end = LocalTime.of(daySchedule.getEndhour(), 0);

        while (!start.isAfter(end)) {
            timeOptions.add(start.toString());
            start = start.plusMinutes(30);
        }
        return timeOptions;
    }

    private void handleChooseLocation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MapChooser.fxml"));
            Parent root = loader.load();
            MapChooserController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Choisir une localisation");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            controller.getSelectedCoordinate().ifPresent(coord -> {
                selectedCoordinate = coord;
                lieuLabel.setText(MapChooserController.getStreetAndAreaFromCoordinates(coord));
            });

        } catch (IOException e) {
            showAlert("Erreur", "Impossible de charger la carte.");
        }
    }

    private void showAlert(String titre, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
