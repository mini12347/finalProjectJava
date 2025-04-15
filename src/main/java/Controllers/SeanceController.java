package Controllers;

import DAO.CandidateDAO;
import DAO.MoniteurDAO;
import Entities.*;
import Service.AutoEcoleInfosS;
import Service.SeanceS;
import com.sothawo.mapjfx.Coordinate;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class SeanceController implements Initializable {
    @FXML public Button chooseLocationBtn;
    @FXML public TableColumn<Seance, Void> colAction;
    @FXML public TableColumn<Seance, String> ColCandidat;
    @FXML public TableColumn<Seance, Integer> colId;
    @FXML public TableColumn<Seance, TypeP> colTypeP;
    @FXML private ComboBox<Candidat> CandidatComboBox;
    @FXML private ComboBox<Moniteur> moniteurComboBox;
    @FXML private ComboBox<String> typeSeanceBox;
    @FXML private ComboBox<Integer> salleComboBox;
    @FXML private DatePicker datePicker;
    @FXML private Button timeButton;
    @FXML private Label locationLabel;
    @FXML private TableView<Seance> tableSeances;
    @FXML private TableColumn<Seance, String> colType;
    @FXML private TableColumn<Seance, LocalDate> colDate;
    @FXML private TableColumn<Seance, Time> colHeure;
    @FXML private TableColumn<Seance, String> ColMoniteur;
    @FXML private TableColumn<Seance, String> colLieu;
    @FXML private Button btnAdd, btnCancel;
    @FXML private VBox salleSelectionBox;

    private final ObservableList<Seance> observableSeances = FXCollections.observableArrayList();
    private final Map<Integer, Moniteur> moniteurCache = new HashMap<>();
    private final Map<Integer, Candidat> candidatCache = new HashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final SeanceS seanceService = new SeanceS();

    private Coordinate selectedCoordinate;
    private Seance selectedSeance;
    private LocalTime selectedTime;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeUIComponents();
        loadInitialDataAsync();
    }

    private void initializeUIComponents() {
        configureColumns();
        setupComboBoxConverters();
        addEventListeners();

        typeSeanceBox.getItems().addAll("Conduite", "Code");
        timeButton.setDisable(true);
        chooseLocationBtn.setDisable(true);
        timeButton.setOnAction(e -> showTimePicker());
    }
    @FXML
    private void showTimePicker() {
        // Vérifie qu'une date est sélectionnée
        if (datePicker.getValue() == null) {
            showAlert("Erreur", "Veuillez d'abord sélectionner une date.");
            return;
        }

        try {
            List<String> times = generateTimeList();
            if (times.isEmpty()) {
                showAlert("Info", "Aucune heure disponible pour ce jour.");
                return;
            }

            ChoiceDialog<String> dialog = new ChoiceDialog<>(times.get(0), times);
            dialog.setTitle("Heure de la séance");
            dialog.setHeaderText("Choisissez une heure");
            dialog.setContentText("Heure :");

            dialog.showAndWait().ifPresent(timeStr -> {
                timeButton.setText(timeStr);
                selectedTime = LocalTime.parse(timeStr);
            });
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors du chargement des heures : " + e.getMessage());
        }
    }
    private List<String> generateTimeList() throws SQLException {
        List<String> timeOptions = new ArrayList<>();

        var timeTable = new AutoEcoleInfosS()
                .getTimeTable()
                .getDaysOfWeek()
                .get(datePicker.getValue().getDayOfWeek());

        LocalTime start = LocalTime.of(timeTable.getStarthour(), 0);
        LocalTime end = LocalTime.of(timeTable.getEndhour(), 0);

        while (!start.isAfter(end)) {
            timeOptions.add(start.toString());
            start = start.plusMinutes(30);
        }

        return timeOptions;
    }
    private void addEventListeners() {
        // Quand le type de séance est sélectionné (Code ou Conduite)
        typeSeanceBox.valueProperty().addListener((obs, oldType, newType) -> {
            boolean isCode = "Code".equalsIgnoreCase(newType);
            boolean isConduite = "Conduite".equalsIgnoreCase(newType);

            salleSelectionBox.setVisible(isCode);
            chooseLocationBtn.setDisable(!isConduite);

            if (isCode) {
                loadAvailableRoomsAsync(); // Charge les salles si nécessaire
            }
        });

        // Quand une séance est sélectionnée dans la table
        tableSeances.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            selectedSeance = selected;

            boolean exists = selectedSeance != null;
            btnAdd.setDisable(!exists);
            btnCancel.setDisable(!exists);

            if (exists) {
                // Pré-remplir les champs si besoin
                typeSeanceBox.setValue(selectedSeance.getType());
                datePicker.setValue(selectedSeance.getDate());
                selectedTime = selectedSeance.getTime().toLocalTime();
                timeButton.setText(selectedTime.toString());
                moniteurComboBox.setValue(moniteurCache.get(selectedSeance.getIdMoniteur()));
                CandidatComboBox.setValue(candidatCache.get(selectedSeance.getIdCandidat()));

                if ("Code".equalsIgnoreCase(selectedSeance.getType())) {
                    salleComboBox.setValue(((SeanceCode)selectedSeance).getNumSalle());
                } else {
                    if (selectedSeance.getInfoSpecifique() != null && selectedSeance.getInfoSpecifique().contains(",")) {
                        String[] parts = selectedSeance.getInfoSpecifique().split(",");
                        selectedCoordinate = new Coordinate(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
                        locationLabel.setText(MapChooserController.getStreetAndAreaFromCoordinates(selectedCoordinate));
                    }
                }
            }
        });

        // Quand une date est sélectionnée : activer le bouton pour choisir l'heure
        datePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            timeButton.setDisable(newDate == null);
        });
    }
    private void loadAvailableRoomsAsync() {
        Task<List<Integer>> roomTask = new Task<>() {
            @Override
            protected List<Integer> call() throws Exception {
                // Récupère la liste des salles disponibles depuis la base de données
                return seanceService.getSalles();
            }

            @Override
            protected void succeeded() {
                // Remplit la ComboBox avec les salles
                salleComboBox.getItems().setAll(getValue());
            }

            @Override
            protected void failed() {
                // En cas d’erreur, affiche une alerte
                showAlert("Erreur", "Impossible de charger les salles : " + getException().getMessage());
            }
        };

        executor.execute(roomTask);
    }

    private void setupComboBoxConverters() {
        moniteurComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Moniteur moniteur) {
                return moniteur == null ? "" : moniteur.getNom() + " " + moniteur.getPrenom() + " — " + moniteur.getCIN();
            }

            @Override
            public Moniteur fromString(String s) {
                return moniteurComboBox.getItems().stream()
                        .filter(m -> (m.getNom() + " " + m.getPrenom() + " — " + m.getCIN()).equals(s))
                        .findFirst()
                        .orElse(null);
            }
        });

        CandidatComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Candidat candidat) {
                return candidat == null ? "" : candidat.getNom() + " " + candidat.getPrenom() + " — " + candidat.getCIN();
            }

            @Override
            public Candidat fromString(String s) {
                return CandidatComboBox.getItems().stream()
                        .filter(c -> (c.getNom() + " " + c.getPrenom() + " — " + c.getCIN()).equals(s))
                        .findFirst()
                        .orElse(null);
            }
        });
    }

    private void configureColumns() {
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colHeure.setCellValueFactory(new PropertyValueFactory<>("time"));
        colId.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getId()));
        colTypeP.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getTypePermis()));

        ColMoniteur.setCellValueFactory(cell -> {
            Moniteur m = moniteurCache.get(cell.getValue().getIdMoniteur());
            return new SimpleStringProperty(m != null ? m.getNom() + " " + m.getPrenom() : "Erreur");
        });

        ColCandidat.setCellValueFactory(cell -> {
            Candidat c = candidatCache.get(cell.getValue().getIdCandidat());
            return new SimpleStringProperty(c != null ? c.getNom() + " " + c.getPrenom() : "Erreur");
        });

        colLieu.setCellValueFactory(cell -> {
            String info = cell.getValue().getInfoSpecifique();
            if (info == null) return new SimpleStringProperty("Lieu inconnu");

            try {
                if (info.contains(",")) {
                    String[] parts = info.split(",");
                    Coordinate coord = new Coordinate(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
                    return new SimpleStringProperty(MapChooserController.getStreetAndAreaFromCoordinates(coord));
                } else if (info.toLowerCase().contains("salle")) {
                    return new SimpleStringProperty(info);
                }
            } catch (Exception e) {
                return new SimpleStringProperty("Erreur");
            }
            return new SimpleStringProperty("Lieu inconnu");
        });

        colAction.setCellFactory(col -> createActionCell());
    }

    private TableCell<Seance, Void> createActionCell() {
        return new TableCell<>() {
            private final Button viewButton = new Button("\uD83D\uDD0D");
            private final Button deleteButton = new Button("\u274C");
            private final HBox container = new HBox(10, viewButton, deleteButton);

            {
                String style = "-fx-font-size: 8px; -fx-background-color: rgba(62,72,84,0.75); -fx-text-fill: white; -fx-font-weight: bold;";
                viewButton.setStyle(style);
                deleteButton.setStyle(style);
                container.setStyle("-fx-alignment: center;");
                viewButton.setOnAction(event -> showSessionDetails(getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(event -> deleteSession(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        };
    }
    private void showSessionDetails(Seance session) {
        if (session == null) {
            showAlert("Info", "Veuillez sélectionner une séance à afficher.");
            return;
        }

        Task<Void> detailTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(() -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SeanceInfo.fxml"));
                        Parent root = loader.load();

                        SeanceInfoController controller = loader.getController();
                        controller.initialize(session, moniteurCache, candidatCache);

                        Stage stage = new Stage();
                        stage.setScene(new Scene(root, 600, 750));
                        stage.setTitle("Détails de la Séance");
                        stage.initModality(Modality.APPLICATION_MODAL);
                        stage.showAndWait();
                    } catch (IOException e) {
                        showAlert("Erreur", "Impossible d'afficher les détails : " + e.getMessage());
                    }
                });
                return null;
            }
        };

        executor.execute(detailTask);
    }

    @FXML
    private void addSession() {
        String type = typeSeanceBox.getValue();
        LocalDate date = datePicker.getValue();
        Moniteur moniteur = moniteurComboBox.getValue();
        Candidat candidat = CandidatComboBox.getValue();
        Integer salle = salleComboBox.getValue();

        if (selectedTime == null) {
            showAlert("Erreur", "Veuillez sélectionner une heure.");
            return;
        }

        validateSessionBeforeAdd(type, date, selectedTime, moniteur, candidat, salle, selectedCoordinate, isValid -> {
            if (!isValid) return;

            Task<Boolean> addTask = new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    TypeP typePermis = moniteur.getVehicule().getType();
                    Time time = Time.valueOf(selectedTime);

                    if ("Code".equalsIgnoreCase(type)) {
                        return seanceService.ajouterSeance(new SeanceCode(0, date, time, typePermis, salle, moniteur.getCIN(), candidat.getCIN()));
                    } else {
                        String localisation = selectedCoordinate.getLatitude() + "," + selectedCoordinate.getLongitude();
                        return seanceService.ajouterSeance(new SeanceConduite(0, date, time, localisation, typePermis, moniteur.getCIN(), candidat.getCIN()));
                    }
                }

                @Override
                protected void succeeded() {
                    if (getValue()) {
                        showAlert("Succès", "Séance ajoutée !");
                        loadInitialDataAsync();
                        clearFields();
                    } else {
                        showAlert("Erreur", "Échec de l'ajout.");
                    }
                }

                @Override
                protected void failed() {
                    showAlert("Exception", getException().getMessage());
                }
            };

            executor.execute(addTask);
        });
    }

    private void validateSessionBeforeAdd(String type, LocalDate date, LocalTime selectedTime,
                                          Moniteur moniteur, Candidat candidat,
                                          Integer salle, Coordinate coord,
                                          Consumer<Boolean> onResult) {
        if (type == null || date == null || selectedTime == null || moniteur == null || candidat == null) {
            showAlert("Erreur", "Veuillez remplir tous les champs obligatoires.");
            onResult.accept(false);
            return;
        }

        if (!moniteur.getDisponibilite().getDaysOfWeek().containsKey(date.getDayOfWeek())) {
            showAlert("Erreur", "Moniteur non disponible pour cette date");
            onResult.accept(false);
            return;
        }

        Hours availableHours = moniteur.getDisponibilite().getDaysOfWeek().get(date.getDayOfWeek());
        LocalTime start = LocalTime.of(availableHours.getStarthour(), 0);
        LocalTime end = LocalTime.of(availableHours.getEndhour(), 0);

        if (selectedTime.isBefore(start) || selectedTime.isAfter(end)) {
            showAlert("Erreur", "Moniteur non disponible pour cet horaire");
            onResult.accept(false);
            return;
        }

        if (date.isBefore(LocalDate.now()) || (date.isEqual(LocalDate.now()) && selectedTime.isBefore(LocalTime.now()))) {
            showAlert("Erreur", "Impossible d'ajouter une séance dans le passé.");
            onResult.accept(false);
            return;
        }

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                try {
                    if (seanceService.ValidateSeassion(moniteur.getCIN(), date, Time.valueOf(selectedTime))) {
                        Platform.runLater(() -> showAlert("Erreur", "Le moniteur est déjà occupé à cette heure."));
                        return false;
                    }

                    if (seanceService.hasCandidateConflict(candidat.getCIN(), date, Time.valueOf(selectedTime))) {
                        Platform.runLater(() -> showAlert("Erreur", "Le candidat est déjà occupé à cette heure."));
                        return false;
                    }

                    if (seanceService.isDuplicateSession(moniteur.getCIN(), candidat.getCIN(), date, Time.valueOf(selectedTime))) {
                        Platform.runLater(() -> showAlert("Erreur", "Une séance identique existe déjà."));
                        return false;
                    }

                    if ("Code".equalsIgnoreCase(type)) {
                        if (salle == null) {
                            Platform.runLater(() -> showAlert("Erreur", "Veuillez sélectionner une salle."));
                            return false;
                        }
                        if (seanceService.isSalleFull(salle, date, Time.valueOf(selectedTime))) {
                            Platform.runLater(() -> showAlert("Erreur", "La salle est pleine (limite de 10 candidats) à cette heure."));
                            return false;
                        }
                    }

                    if ("Conduite".equalsIgnoreCase(type) && coord == null) {
                        Platform.runLater(() -> showAlert("Erreur", "Veuillez choisir une localisation pour la séance de conduite."));
                        return false;
                    }

                    Hours schedule = new AutoEcoleInfosS().getTimeTable().getDaysOfWeek().get(date.getDayOfWeek());
                    LocalTime schoolStart = LocalTime.of(schedule.getStarthour(), 0);
                    LocalTime schoolEnd = LocalTime.of(schedule.getEndhour(), 0);
                    if (selectedTime.isBefore(schoolStart) || selectedTime.isAfter(schoolEnd)) {
                        Platform.runLater(() -> showAlert("Erreur", "L'heure de la séance est en dehors des horaires de l'auto-école."));
                        return false;
                    }

                    return true;

                } catch (Exception e) {
                    Platform.runLater(() -> showAlert("Erreur", "Erreur lors de la validation: " + e.getMessage()));
                    return false;
                }
            }

            @Override
            protected void succeeded() {
                onResult.accept(getValue());
            }

            @Override
            protected void failed() {
                showAlert("Erreur", "Erreur interne lors de la validation.");
                onResult.accept(false);
            }
        };

        executor.execute(task);
    }

    @FXML
    private void clearFields() {
        typeSeanceBox.setValue(null);
        datePicker.setValue(null);
        moniteurComboBox.setValue(null);
        CandidatComboBox.setValue(null);
        salleComboBox.setValue(null);
        selectedCoordinate = null;
        locationLabel.setText("Aucune localisation choisie");
        timeButton.setText("Choisir l'heure");
        selectedTime = null;
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    private void loadInitialDataAsync() {
        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // 1. Chargement des caches
                MoniteurDAO moniteurDAO = new MoniteurDAO();
                CandidateDAO candidatDAO = new CandidateDAO();

                List<Moniteur> moniteurs = moniteurDAO.afficherTousLesMoniteurs();
                List<Candidat> candidats = candidatDAO.findAll();
                List<Seance> seances = seanceService.getAllSeances();

                // 2. Mise à jour du cache
                moniteurCache.clear();
                for (Moniteur m : moniteurs) {
                    moniteurCache.put(m.getCIN(), m);
                }

                candidatCache.clear();
                for (Candidat c : candidats) {
                    candidatCache.put(c.getCIN(), c);
                }

                // 3. Mise à jour de l'interface graphique
                Platform.runLater(() -> {
                    observableSeances.setAll(seances);
                    tableSeances.setItems(observableSeances);
                    moniteurComboBox.getItems().setAll(moniteurs);
                    CandidatComboBox.getItems().setAll(candidats);
                });

                return null;
            }

            @Override
            protected void failed() {
                Platform.runLater(() ->
                        showAlert("Erreur", "Erreur lors du chargement initial : " + getException().getMessage()));
            }
        };

        executor.execute(loadTask);
    }
    private void deleteSession(Seance session) {
        if (session == null) {
            showAlert("Info", "Veuillez d'abord sélectionner une séance à supprimer.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Êtes-vous sûr de vouloir supprimer cette séance ?");
        alert.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Task<Void> deleteTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    seanceService.supprimerSeance(session);
                    return null;
                }

                @Override
                protected void succeeded() {
                    observableSeances.remove(session);
                    tableSeances.refresh();
                    showAlert("Succès", "Séance supprimée avec succès.");
                    clearFields();
                }

                @Override
                protected void failed() {
                    showAlert("Erreur", "Erreur lors de la suppression : " + getException().getMessage());
                }
            };

            executor.execute(deleteTask);
        }
    }
    @FXML
    private void handleChooseLocation() {
        Task<Void> locationTask = new Task<Void>() {
            @Override
            protected Void call() {
                Platform.runLater(() -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MapChooser.fxml"));
                        Parent root = loader.load();

                        MapChooserController controller = loader.getController();

                        Stage stage = new Stage();
                        stage.setTitle("Choisir une localisation");
                        stage.setScene(new Scene(root));
                        stage.initModality(Modality.APPLICATION_MODAL);
                        stage.showAndWait();

                        // Récupération de la coordonnée choisie
                        Optional<Coordinate> selected = controller.getSelectedCoordinate();
                        selected.ifPresent(coord -> {
                            selectedCoordinate = coord;
                            locationLabel.setText(MapChooserController.getStreetAndAreaFromCoordinates(coord));
                        });

                    } catch (IOException e) {
                        showAlert("Erreur", "Impossible de charger la carte : " + e.getMessage());
                    }
                });
                return null;
            }
        };

        executor.execute(locationTask);
    }

    @FXML
    private void displayAllSessions() {
        Task<List<Seance>> loadTask = new Task<>() {
            @Override
            protected List<Seance> call() throws Exception {
                return seanceService.getAllSeances(); // appel à la base via le service
            }

            @Override
            protected void succeeded() {
                List<Seance> sessions = getValue();
                observableSeances.setAll(sessions);
                tableSeances.setItems(observableSeances);
            }

            @Override
            protected void failed() {
                showAlert("Erreur", "Impossible de charger les séances : " + getException().getMessage());
            }
        };

        executor.execute(loadTask);
    }
    @FXML
    private void displayThisWeekSessions() {
        Task<List<Seance>> loadTask = new Task<>() {
            @Override
            protected List<Seance> call() throws Exception {
                return seanceService.getSeanceThisWeek(); // méthode à définir dans ton service
            }

            @Override
            protected void succeeded() {
                List<Seance> sessions = getValue();
                observableSeances.setAll(sessions);
                tableSeances.setItems(observableSeances);
            }

            @Override
            protected void failed() {
                showAlert("Erreur", "Erreur lors du chargement des séances de la semaine : " + getException().getMessage());
            }
        };

        executor.execute(loadTask);
    }
}
