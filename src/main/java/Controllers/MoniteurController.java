package Controllers;

import Entities.Moniteur;
import Entities.Vehicule;
import Entities.Disponibility;
import DAO.MoniteurDAO;
import DAO.VehiculeDAO;
import DAO.DisponibilityDAO;
import Connection.ConxDB;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.time.DayOfWeek;

public class MoniteurController {

    private MoniteurDAO moniteurDAO;
    private VehiculeDAO vehiculeDAO;
    private DisponibilityDAO disponibilityDAO;

    @FXML
    private TextField cinField, nomField, prenomField, adresseField, mailField, numTelephoneField;
    @FXML
    private DatePicker dateNaissanceField;
    @FXML
    private ComboBox<Vehicule> vehiculeComboBox;
    @FXML
    private ComboBox<Disponibility> dispoComboBox;
    @FXML
    private TableView<Moniteur> moniteurTableView;
    @FXML
    private TableColumn<Moniteur, Integer> cinColumn;
    @FXML
    private TableColumn<Moniteur, String> nomColumn, prenomColumn, adresseColumn, mailColumn, numTelephoneColumn, dateNaissanceColumn, vehiculeColumn, dispoColumn;
    @FXML
    private Label nbMoniteursLabel;

    public MoniteurController() {
        Connection connection = ConxDB.getInstance();
        this.moniteurDAO = new MoniteurDAO(connection);
        this.vehiculeDAO = new VehiculeDAO();
        this.disponibilityDAO = new DisponibilityDAO(connection);
    }

    public void initialize() {
        // Configuration des colonnes du tableau
        cinColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getCIN()).asObject());
        nomColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNom()));
        prenomColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPrenom()));

        // Assurez-vous que toutes les colonnes sont configurées
        if (adresseColumn != null) {
            adresseColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAdresse()));
        }
        if (mailColumn != null) {
            mailColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMail()));
        }
        if (numTelephoneColumn != null) {
            numTelephoneColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getNumTelephone())));
        }
        if (dateNaissanceColumn != null) {
            dateNaissanceColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                    cellData.getValue().getDateNaissance() != null ?
                            cellData.getValue().getDateNaissance().toString() : ""));
        }
        if (vehiculeColumn != null) {
            vehiculeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                    cellData.getValue().getVehicule() != null ?
                            String.valueOf(cellData.getValue().getVehicule().getMatricule()) : ""));
        }
        if (dispoColumn != null) {
            dispoColumn.setCellValueFactory(cellData -> {
                Disponibility dispo = cellData.getValue().getDisponibilite();
                if (dispo == null || dispo.getDaysOfWeek() == null || dispo.getDaysOfWeek().isEmpty()) {
                    return new SimpleStringProperty("Aucune");
                }

                StringBuilder sb = new StringBuilder();
                int count = 0;
                for (Map.Entry<DayOfWeek, Entities.Hours> entry : dispo.getDaysOfWeek().entrySet()) {
                    if (count > 0) sb.append(", ");
                    sb.append(translateDayOfWeek(entry.getKey())).append(" ")
                            .append(entry.getValue().getStarthour()).append("h-")
                            .append(entry.getValue().getEndhour()).append("h");
                    count++;
                    if (count >= 1) {
                        sb.append("...");
                        break;
                    }
                }
                return new SimpleStringProperty(sb.toString());
            });
        }

        // Définir les factories et l'affichage des ComboBox
        initializeComboBoxes();

        // Charger les données de référence
        loadVehicules();
        loadDisponibilities();

        // IMPORTANT: Configurer le tableau pour qu'il prenne tout l'espace vertical disponible
        VBox.setVgrow(moniteurTableView, Priority.ALWAYS);

        // Écouter les sélections dans le tableau pour remplir le formulaire
        moniteurTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                fillFieldsWithMoniteur(newSelection);
            }
        });
    }

    // Initialisation des ComboBox (structure et affichage)
    private void initializeComboBoxes() {
        // Configuration de l'affichage des véhicules
        vehiculeComboBox.setCellFactory(param -> new ListCell<Vehicule>() {
            @Override
            protected void updateItem(Vehicule item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("Matricule: " + item.getMatricule() + " - Type: " + item.getType());
                }
            }
        });

        vehiculeComboBox.setConverter(new StringConverter<Vehicule>() {
            @Override
            public String toString(Vehicule vehicule) {
                if (vehicule == null) {
                    return null;
                }
                return "Matricule: " + vehicule.getMatricule() + " - Type: " + vehicule.getType();
            }

            @Override
            public Vehicule fromString(String string) {
                return null; // Non nécessaire pour un ComboBox en lecture seule
            }
        });

        // Configuration de l'affichage des disponibilités
        dispoComboBox.setCellFactory(param -> new ListCell<Disponibility>() {
            @Override
            protected void updateItem(Disponibility item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    StringBuilder sb = new StringBuilder();
                    Map<DayOfWeek, Entities.Hours> schedule = item.getDaysOfWeek();
                    if (schedule != null && !schedule.isEmpty()) {
                        sb.append("Disponibilité: ");
                        int count = 0;
                        for (Map.Entry<DayOfWeek, Entities.Hours> entry : schedule.entrySet()) {
                            if (count > 0) sb.append(", ");
                            sb.append(translateDayOfWeek(entry.getKey())).append(" ")
                                    .append(entry.getValue().getStarthour()).append("h-")
                                    .append(entry.getValue().getEndhour()).append("h");
                            count++;
                            // Limiter l'affichage pour éviter de trop longues chaînes
                            if (count >= 2) {
                                sb.append("...");
                                break;
                            }
                        }
                    } else {
                        sb.append("Aucune disponibilité");
                    }
                    setText(sb.toString());
                }
            }
        });

        dispoComboBox.setConverter(new StringConverter<Disponibility>() {
            @Override
            public String toString(Disponibility dispo) {
                if (dispo == null || dispo.getDaysOfWeek() == null || dispo.getDaysOfWeek().isEmpty()) {
                    return null;
                }

                StringBuilder sb = new StringBuilder("Dispo: ");
                int count = 0;
                for (Map.Entry<DayOfWeek, Entities.Hours> entry : dispo.getDaysOfWeek().entrySet()) {
                    if (count > 0) sb.append(", ");
                    sb.append(translateDayOfWeek(entry.getKey())).append(" ")
                            .append(entry.getValue().getStarthour()).append("h-")
                            .append(entry.getValue().getEndhour()).append("h");
                    count++;
                    if (count >= 2) {
                        sb.append("...");
                        break;
                    }
                }
                return sb.toString();
            }

            @Override
            public Disponibility fromString(String string) {
                return null; // Non nécessaire pour un ComboBox en lecture seule
            }
        });
    }

    // Méthode pour charger les véhicules dans le ComboBox
    private void loadVehicules() {
        try {
            List<Vehicule> vehicules = vehiculeDAO.getAllVehicules();
            vehiculeComboBox.getItems().clear();
            vehiculeComboBox.getItems().addAll(vehicules);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des véhicules : " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Méthode pour charger les disponibilités dans le ComboBox
    private void loadDisponibilities() {
        try {
            List<Disponibility> disponibilities = disponibilityDAO.getAllDisponibilities();
            dispoComboBox.getItems().clear();
            dispoComboBox.getItems().addAll(disponibilities);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des disponibilités : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace(); // Afficher la trace complète pour le débogage
        }
    }

    // Méthode utilitaire pour traduire les jours de la semaine en français
    private String translateDayOfWeek(DayOfWeek day) {
        switch (day) {
            case MONDAY: return "Lundi";
            case TUESDAY: return "Mardi";
            case WEDNESDAY: return "Mercredi";
            case THURSDAY: return "Jeudi";
            case FRIDAY: return "Vendredi";
            case SATURDAY: return "Samedi";
            case SUNDAY: return "Dimanche";
            default: return day.toString();
        }
    }

    // Méthode pour remplir le formulaire avec les données d'un moniteur sélectionné
    // Méthode pour remplir le formulaire avec les données d'un moniteur sélectionné
    private void fillFieldsWithMoniteur(Moniteur moniteur) {
        cinField.setText(String.valueOf(moniteur.getCIN()));
        nomField.setText(moniteur.getNom());
        prenomField.setText(moniteur.getPrenom());
        adresseField.setText(moniteur.getAdresse());
        mailField.setText(moniteur.getMail());
        numTelephoneField.setText(String.valueOf(moniteur.getNumTelephone()));

        if (moniteur.getDateNaissance() != null) {
            // Conversion de java.sql.Date en LocalDate compatible avec toutes les versions de Java
            dateNaissanceField.setValue(moniteur.getDateNaissance().toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate());
        } else {
            dateNaissanceField.setValue(null);
        }

        vehiculeComboBox.setValue(moniteur.getVehicule());
        dispoComboBox.setValue(moniteur.getDisponibilite());
    }
    @FXML
    private void ajouterMoniteur(MouseEvent event) {
        try {
            if (cinField.getText().isEmpty() || nomField.getText().isEmpty() || prenomField.getText().isEmpty() ||
                    adresseField.getText().isEmpty() || mailField.getText().isEmpty() || numTelephoneField.getText().isEmpty() ||
                    dateNaissanceField.getValue() == null || vehiculeComboBox.getValue() == null || dispoComboBox.getValue() == null) {
                showAlert("Erreur", "Veuillez remplir tous les champs.", Alert.AlertType.ERROR);
                return;
            }

            // Convertir la date sélectionnée en java.sql.Date
            Date dateNaissance = Date.valueOf(dateNaissanceField.getValue());

            Moniteur moniteur = new Moniteur(
                    Integer.parseInt(cinField.getText()), nomField.getText(), prenomField.getText(),
                    adresseField.getText(), mailField.getText(), Integer.parseInt(numTelephoneField.getText()),
                    dateNaissance, vehiculeComboBox.getValue(), dispoComboBox.getValue()
            );

            moniteurDAO.ajouterMoniteur(moniteur);
            // Afficher tous les moniteurs après l'ajout
            afficherMoniteurs();
            showAlert("Succès", "Moniteur ajouté avec succès.", Alert.AlertType.INFORMATION);
            clearFields();
        } catch (NumberFormatException | SQLException e) {
            showAlert("Erreur", "Veuillez entrer des valeurs valides: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void supprimerMoniteur(MouseEvent event) throws SQLException {
        try {
            // Si le champ CIN n'est pas vide, utiliser cette valeur
            if (!cinField.getText().isEmpty()) {
                int cin = Integer.parseInt(cinField.getText());
                // Vérifier d'abord si le moniteur existe
                Moniteur moniteur = moniteurDAO.chercherMoniteur(cin);
                if (moniteur != null) {
                    moniteurDAO.supprimerMoniteur(cin);
                    // Afficher tous les moniteurs après la suppression
                    afficherMoniteurs();
                    showAlert("Succès", "Moniteur avec CIN " + cin + " supprimé avec succès.", Alert.AlertType.INFORMATION);
                    clearFields();
                } else {
                    showAlert("Erreur", "Aucun moniteur trouvé avec le CIN " + cin, Alert.AlertType.WARNING);
                }
            } else {
                // Sinon, essayer d'utiliser le moniteur sélectionné dans le tableau
                Moniteur selectedMoniteur = moniteurTableView.getSelectionModel().getSelectedItem();
                if (selectedMoniteur != null) {
                    moniteurDAO.supprimerMoniteur(selectedMoniteur.getCIN());
                    // Afficher tous les moniteurs après la suppression
                    afficherMoniteurs();
                    showAlert("Succès", "Moniteur supprimé avec succès.", Alert.AlertType.INFORMATION);
                    clearFields();
                } else {
                    showAlert("Erreur", "Veuillez saisir un CIN ou sélectionner un moniteur.", Alert.AlertType.WARNING);
                }
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer un CIN valide (nombre entier).", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la suppression : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void rechercherMoniteurParId(MouseEvent event) {
        try {
            if (cinField.getText().isEmpty()) {
                showAlert("Erreur", "Veuillez entrer un CIN pour la recherche.", Alert.AlertType.WARNING);
                return;
            }

            int cin = Integer.parseInt(cinField.getText());
            Moniteur moniteur = moniteurDAO.chercherMoniteur(cin);
            if (moniteur != null) {
                moniteurTableView.getItems().clear();
                moniteurTableView.getItems().add(moniteur);
                // Mettre à jour le compteur
                updateCounterLabel();
            } else {
                showAlert("Information", "Aucun moniteur trouvé avec le CIN " + cin, Alert.AlertType.INFORMATION);
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer un CIN valide (nombre entier).", Alert.AlertType.ERROR);
        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la recherche : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // Méthode principal pour afficher tous les moniteurs
    @FXML
    public void afficherMoniteurs() {
        try {
            List<Moniteur> moniteurs = moniteurDAO.afficherTousLesMoniteurs();
            moniteurTableView.getItems().clear();
            moniteurTableView.getItems().addAll(moniteurs);

            // Mettre à jour le compteur
            updateCounterLabel();
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de l'affichage des moniteurs : " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // Mettre à jour le label affichant le nombre de moniteurs
    private void updateCounterLabel() {
        if (nbMoniteursLabel != null) {
            int count = moniteurTableView.getItems().size();
            nbMoniteursLabel.setText("Nombre de moniteurs : " + count);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void clearFields() {
        cinField.clear();
        nomField.clear();
        prenomField.clear();
        adresseField.clear();
        mailField.clear();
        numTelephoneField.clear();
        dateNaissanceField.setValue(null);
        vehiculeComboBox.setValue(null);
        dispoComboBox.setValue(null);
    }
}