package Controllers;

import com.sothawo.mapjfx.*;
import com.sothawo.mapjfx.event.MapViewEvent;
import javafx.scene.layout.AnchorPane;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;
import Entities.*;
import Service.CandidateService;
import Service.ExamenCodeService;
import Service.ExamenConduiteService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ExamenConduiteController implements Initializable {
    @FXML
    private TextField idField;
    @FXML
    private DatePicker dateField;
    @FXML
    private TextField timeField;
    @FXML
    private ComboBox<String> candidatComboBox;
    @FXML
    private ComboBox<Res> resultatComboBox;
    @FXML
    private ComboBox<TypeP> typePermisComboBox;
    @FXML
    private TextField coutField;
    @FXML
    private TextField localisationField;
    @FXML
    private Button mapBtn;
    @FXML
    private TableView<ExamenConduite> examenTable;
    @FXML
    private TableColumn<ExamenConduite, Integer> idColumn;
    @FXML
    private TableColumn<ExamenConduite, LocalDate> dateColumn;
    @FXML
    private TableColumn<ExamenConduite, Time> timeColumn;
    @FXML
    private TableColumn<ExamenConduite, String> candidatColumn;
    @FXML
    private TableColumn<ExamenConduite, Res> resultatColumn;
    @FXML
    private TableColumn<ExamenConduite, TypeP> typePermisColumn;
    @FXML
    private TableColumn<ExamenConduite, String> localisationColumn;
    @FXML
    private TableColumn<ExamenCode, Double> coutColumn;
    @FXML
    private Label countLabel;
    @FXML
    private MapView mapView;
    @FXML
    private Button confirmButton;
    @FXML
    private AnchorPane mapContainer;

    private Coordinate selectedCoordinate;
    private Marker mapMarker;

    private ExamenConduiteService examenConduiteService;
    private CandidateService candidatService;
    private ExamenCodeService examenCodeService;
    private ObservableList<ExamenConduite> examenList;
    private List<Integer> eligibleCandidatCINs;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            examenConduiteService = new ExamenConduiteService();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            candidatService = new CandidateService();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            examenCodeService = new ExamenCodeService();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        examenList = FXCollections.observableArrayList();
        eligibleCandidatCINs = new ArrayList<>();

        // Initialisation des colonnes du tableau
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        candidatColumn.setCellValueFactory(cellData -> {
            Candidat candidat = cellData.getValue().getCandidat();
            return javafx.beans.binding.Bindings.createStringBinding(() -> String.valueOf(candidat.getCIN()));
        });
        resultatColumn.setCellValueFactory(new PropertyValueFactory<>("resultat"));
        typePermisColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        coutColumn.setCellValueFactory(new PropertyValueFactory<>("cout"));
        localisationColumn.setCellValueFactory(new PropertyValueFactory<>("localisation"));

        // Remplir le comboBox des types de permis
        typePermisComboBox.getItems().setAll(TypeP.values());

        // Remplir le ComboBox des résultats possibles
        resultatComboBox.getItems().setAll(Res.SUCCES, Res.ECHEC);

        // Désactiver le ComboBox des résultats quand la date est dans le futur
        dateField.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                boolean isPastDate = !isDateInFuture(newVal);
                resultatComboBox.setDisable(!isPastDate);

                // Si la date est dans le futur, on met automatiquement EN_ATTENTE
                if (!isPastDate) {
                    resultatComboBox.setValue(Res.EnATTENTE);
                } else {
                    // Si on revient à une date passée, on vide le ComboBox
                    resultatComboBox.setValue(null);
                }
            }
        });

        // Charger les candidats éligibles (ayant réussi l'examen de code)
        loadEligibleCandidats();

        // Charger tous les examens
        loadAllExamens();

        // Définir le listener pour la sélection dans le tableau
        examenTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                fillFormWithExamen(newVal);
            }
        });

        // Initialiser la carte si mapView est présent dans le FXML
        if (mapView != null) {
            initializeMap();
        }
    }

    @FXML
    private void handleOpenMap() {
        try {
            // Charger le fichier FXML pour la carte web
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/WebMap.fxml"));
            Parent root = loader.load();

            // Obtenir le contrôleur de la carte
            WebMapController mapController = loader.getController();

            // Créer une nouvelle fenêtre pour la carte
            Stage mapStage = new Stage();
            mapStage.setTitle("Choisir une localisation");
            mapStage.initModality(Modality.APPLICATION_MODAL);
            mapStage.setScene(new Scene(root));

            // Afficher la fenêtre et attendre qu'elle soit fermée
            mapStage.showAndWait();

            // Récupérer les coordonnées et l'adresse sélectionnées
            Optional<Coordinate> coordinate = mapController.getSelectedCoordinate();
            Optional<String> address = mapController.getSelectedAddress();

            if (coordinate.isPresent() && address.isPresent()) {
                selectedCoordinate = coordinate.get();
                localisationField.setText(address.get());
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la carte.");
        }
    }

    private void initializeMap() {
        try {
            // Configuration de base de la map
            mapView.initialize(Configuration.builder()
                    .showZoomControls(true)
                    .projection(Projection.WEB_MERCATOR)
                    .build());

            // Définir les coordonnées initiales (Tunis)
            Coordinate tunis = new Coordinate(36.8065, 10.1815);
            selectedCoordinate = tunis;

            // Créer et ajouter un marqueur
            mapMarker = Marker.createProvided(Marker.Provided.BLUE)
                    .setVisible(true)
                    .setPosition(tunis);
            mapView.addMarker(mapMarker);
            mapView.setCenter(tunis);
            mapView.setZoom(13);

            // Configurer le gestionnaire d'événements pour les clics sur la carte
            mapView.addEventHandler(MapViewEvent.MAP_CLICKED, event -> {
                Coordinate coord = event.getCoordinate().normalize();

                // Vérifier si les coordonnées sont dans la zone autorisée (Tunis)
                if (isInTunisArea(coord)) {
                    selectedCoordinate = coord;
                    mapView.removeMarker(mapMarker);
                    mapMarker.setPosition(coord);
                    mapView.addMarker(mapMarker);

                    // Mettre à jour le champ de localisation avec l'adresse correspondante
                    updateLocationField(coord);
                } else {
                    showAlert(Alert.AlertType.WARNING, "Zone invalide",
                            "Veuillez choisir un emplacement dans la zone de Tunis.");
                }
            });

            // Configurer le bouton de confirmation
            if (confirmButton != null) {
                confirmButton.setOnAction(e -> {
                    // Utiliser les coordonnées sélectionnées
                    if (selectedCoordinate != null) {
                        updateLocationField(selectedCoordinate);
                    }
                });
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur d'initialisation de la carte",
                    "Impossible d'initialiser la carte: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Vérifier si les coordonnées sont dans la zone de Tunis
    private boolean isInTunisArea(Coordinate coord) {
        final double MIN_LAT = 36.70;
        final double MAX_LAT = 36.90;
        final double MIN_LON = 10.05;
        final double MAX_LON = 10.25;

        return coord.getLatitude() >= MIN_LAT && coord.getLatitude() <= MAX_LAT &&
                coord.getLongitude() >= MIN_LON && coord.getLongitude() <= MAX_LON;
    }

    // Mettre à jour le champ de localisation avec l'adresse correspondante aux coordonnées
    private void updateLocationField(Coordinate coord) {
        try {
            String address = getStreetAndAreaFromCoordinates(coord);
            localisationField.setText(address);
        } catch (Exception e) {
            localisationField.setText(coord.getLatitude() + ", " + coord.getLongitude());
        }
    }

    // Méthode pour obtenir l'adresse à partir des coordonnées GPS
    private String getStreetAndAreaFromCoordinates(Coordinate coordinate) {
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

    // Méthode pour charger les candidats éligibles qui ont réussi l'examen de code
    private void loadEligibleCandidats() {
        try {
            // 1. Récupérer tous les candidats qui ont réussi l'examen de code
            List<ExamenCode> successfulCodeExams = examenCodeService.getExamensByResult(Res.SUCCES);
            List<Integer> eligibleFromCodeExams = new ArrayList<>();

            // Extraire les CINs des candidats qui ont réussi l'examen de code
            for (ExamenCode exam : successfulCodeExams) {
                int cin = exam.getCandidat().getCIN();
                if (!eligibleFromCodeExams.contains(cin)) {
                    eligibleFromCodeExams.add(cin);
                }
            }

            // 2. Récupérer les CINs des candidats qui ont déjà réussi l'examen de conduite
            List<Integer> successfulConduiteExams = examenConduiteService.getSuccessfulCandidateCins();

            // 3. Filtrer pour ne garder que les candidats qui ont réussi le code mais pas encore la conduite
            eligibleCandidatCINs.clear();
            for (Integer cin : eligibleFromCodeExams) {
                if (!successfulConduiteExams.contains(cin)) {
                    eligibleCandidatCINs.add(cin);
                }
            }

            // Mettre à jour le ComboBox avec uniquement les candidats éligibles
            ObservableList<String> cinList = FXCollections.observableArrayList();
            for (Integer cin : eligibleCandidatCINs) {
                cinList.add(String.valueOf(cin));
            }
            candidatComboBox.setItems(cinList);

            // Si la liste est vide, afficher un message d'information
            if (cinList.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Information",
                        "Aucun candidat éligible n'est disponible pour l'examen de conduite.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur de base de données",
                    "Impossible de charger les candidats éligibles: " + e.getMessage());
        }
    }

    private void loadAllExamens() {
        examenList.clear();
        examenList.addAll(examenConduiteService.getAllExamens());
        examenTable.setItems(examenList);
        updateCountLabel();
    }

    private void updateCountLabel() {
        countLabel.setText("Nombre d'examens: " + examenList.size());
    }

    private void fillFormWithExamen(ExamenConduite examen) {
        try {
            idField.setText(String.valueOf(examen.getId()));
            dateField.setValue(examen.getDate());
            timeField.setText(examen.getTime().toString());
            candidatComboBox.setValue(String.valueOf(examen.getCandidat().getCIN()));

            // Adapter le comportement selon la date
            LocalDate examDate = examen.getDate();
            boolean isPastDate = !isDateInFuture(examDate);
            resultatComboBox.setDisable(!isPastDate);
            resultatComboBox.setValue(examen.getResultat());

            typePermisComboBox.setValue(examen.getType());
            coutField.setText(String.valueOf(examen.getCout()));
            localisationField.setText(examen.getLocalisation());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de remplir le formulaire: " + e.getMessage());
        }
    }

    private void clearForm() {
        idField.clear();
        dateField.setValue(null);
        timeField.clear();
        candidatComboBox.setValue(null);
        resultatComboBox.setValue(null);
        resultatComboBox.setDisable(false);
        typePermisComboBox.setValue(null);
        coutField.clear();
        localisationField.clear();
        examenTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleAjouter(ActionEvent event) {
        try {
            if (!validateForm()) {
                showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Veuillez remplir tous les champs correctement");
                return;
            }

            // Vérification supplémentaire que le candidat a réussi l'examen de code
            String cinStr = candidatComboBox.getValue();
            int cin = Integer.parseInt(cinStr);

            if (!isCandidatEligible(cin)) {
                showAlert(Alert.AlertType.ERROR, "Candidat non éligible",
                        "Ce candidat n'a pas encore réussi l'examen de code.");
                return;
            }

            ExamenConduite examen = createExamenFromForm(false);

            // Vous pouvez ajouter une option pour le paiement par facilité si nécessaire
            boolean parFacilite = false; // Par défaut, pas de facilité

            // Appel de la méthode modifiée avec création de paiement
            boolean success = examenConduiteService.addExamen(examen, true, parFacilite);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Examen ajouté avec succès");
                clearForm();
                loadAllExamens();
                loadEligibleCandidats();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'ajout de l'examen");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue: " + e.getMessage());
        }
    }

    // Méthode pour vérifier si un candidat est éligible (a réussi l'examen de code)
    private boolean isCandidatEligible(int cin) {
        return eligibleCandidatCINs.contains(cin);
    }

    @FXML
    private void handleModifier(ActionEvent event) {
        try {
            if (!validateForm() || idField.getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Veuillez sélectionner un examen et remplir tous les champs");
                return;
            }

            // Pour la modification, on vérifie si le candidat est différent de l'original
            ExamenConduite selectedExamen = examenTable.getSelectionModel().getSelectedItem();
            String newCinStr = candidatComboBox.getValue();
            int newCin = Integer.parseInt(newCinStr);

            // Si le candidat a changé, vérifier qu'il est éligible
            if (selectedExamen != null && selectedExamen.getCandidat().getCIN() != newCin) {
                if (!isCandidatEligible(newCin)) {
                    showAlert(Alert.AlertType.ERROR, "Candidat non éligible",
                            "Ce candidat n'a pas encore réussi l'examen de code.");
                    return;
                }
            }

            ExamenConduite examen = createExamenFromForm(true);
            boolean success = examenConduiteService.updateExamen(examen);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Examen modifié avec succès");
                clearForm();
                loadAllExamens();
                loadEligibleCandidats();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la modification de l'examen");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue: " + e.getMessage());
        }
    }

    @FXML
    private void handleSupprimer(ActionEvent event) {
        if (idField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner un examen à supprimer");
            return;
        }

        try {
            int id = Integer.parseInt(idField.getText());
            boolean success = examenConduiteService.deleteExamen(id);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Examen modifié avec succès");
                clearForm();
                loadAllExamens();
                loadEligibleCandidats();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la suppression de l'examen");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue: " + e.getMessage());
        }
    }

    @FXML
    private void handleRechercher(ActionEvent event) {
        if (candidatComboBox.getValue() != null && !candidatComboBox.getValue().isEmpty()) {
            try {
                String cinStr = candidatComboBox.getValue();
                int cin = Integer.parseInt(cinStr);
                List<ExamenConduite> examens = examenConduiteService.getExamensByCandidatCin(cin);
                examenList.clear();
                examenList.addAll(examens);
                examenTable.setItems(examenList);
                updateCountLabel();
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Format de CIN invalide: " + e.getMessage());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur",
                        "Une erreur est survenue: " + e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un CIN de candidat pour la recherche");
        }
    }

    @FXML
    private void handleAfficherTout(ActionEvent event) {
        loadAllExamens();
    }

    @FXML
    private void handleClear(ActionEvent event) {
        clearForm();
    }

    private boolean validateForm() {
        // Vérification des champs obligatoires
        boolean formValid = dateField.getValue() != null &&
                !timeField.getText().isEmpty() &&
                candidatComboBox.getValue() != null &&
                typePermisComboBox.getValue() != null &&
                !coutField.getText().isEmpty()&&
                localisationField.getText() != null && !localisationField.getText().isEmpty();
        if (formValid && !coutField.getText().isEmpty()) {
            try {
                Double.parseDouble(coutField.getText().replace(',', '.'));
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur de saisie", "Le coût doit être un nombre valide");
                return false;
            }
        }

        // Pour le résultat, vérifier selon la date
        if (formValid) {
            LocalDate examDate = dateField.getValue();
            boolean isFutureDate = isDateInFuture(examDate);

            // Si date future, résultat doit être EN_ATTENTE et est automatiquement défini
            if (isFutureDate) {
                // Le résultat sera automatiquement EN_ATTENTE, pas besoin de validation
            }
            // Si date passée, résultat doit être soit SUCCES soit ECHEC
            else if (resultatComboBox.getValue() == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur de saisie",
                        "Pour une date passée, veuillez sélectionner un résultat (Succès ou Échec)");
                return false;
            }
        }

        return formValid;
    }

    private boolean isDateInFuture(LocalDate date) {
        LocalDate today = LocalDate.now();
        return date.isAfter(today);
    }

    private ExamenConduite createExamenFromForm(boolean withId) {
        LocalDate date = dateField.getValue();

        Time time;
        try {
            LocalTime localTime = LocalTime.parse(timeField.getText(), DateTimeFormatter.ofPattern("HH:mm:ss"));
            time = Time.valueOf(localTime);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Format d'heure invalide. Utilisez HH:mm:ss");
        }

        String cinStr = candidatComboBox.getValue();
        Candidat candidat;
        try {
            candidat = candidatService.getCandidateByCin(Integer.parseInt(cinStr));
            if (candidat == null) {
                throw new IllegalArgumentException("Candidat introuvable");
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException("Erreur lors de la recherche du candidat: " + e.getMessage());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Le CIN doit être un nombre entier");
        }

        // Déterminer le résultat en fonction de la date
        Res resultat;
        if (isDateInFuture(date)) {
            resultat = Res.EnATTENTE;
        } else {
            resultat = resultatComboBox.getValue();
            if (resultat == null) {
                throw new IllegalArgumentException("Pour une date passée, veuillez sélectionner un résultat");
            }
        }

        TypeP typePermis = typePermisComboBox.getValue();
        String localisation = localisationField.getText();

        // Parse the cost value
        double cout;
        try {
            cout = Double.parseDouble(coutField.getText().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Le coût doit être un nombre valide");
        }

        // Create the ExamenConduite object with all parameters including cout
        ExamenConduite examen = new ExamenConduite(0, date, time, candidat, resultat, localisation, typePermis, cout);

        if (withId && !idField.getText().isEmpty()) {
            examen.setId(Integer.parseInt(idField.getText()));
        }

        return examen;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}