package Controllers;
import DAO.VehiculesDAO;
import Entities.TypeP;
import Entities.Vehicule;
import Service.VehiculesS;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class VehiculesController {

    @FXML public Label indication;
    @FXML private TableView<Vehicule> vehiculesTable;
    @FXML private TableColumn<Vehicule, String> matriculeColumn, datemColumn, typeColumn;
    @FXML private TableColumn<Vehicule, Integer> kilometrageColumn;
    @FXML private TableColumn<Vehicule, Void> actionsColumn;
    @FXML private TextField matricule, amatricule, datem, kilo, type, searchField;
    private final VehiculesS vehiculesS = new VehiculesS();
    private ObservableList<Vehicule> vehiculesList;

    public void initialize() {
        try {
            List<Vehicule> vehicules = vehiculesS.getVehicules();
            vehiculesList = FXCollections.observableArrayList(vehicules);
            vehiculesTable.setItems(vehiculesList);

            matriculeColumn.setCellValueFactory(new PropertyValueFactory<>("matricule"));
            datemColumn.setCellValueFactory(cellData -> {
                Date date = cellData.getValue().getDatem();
                return new SimpleStringProperty(date != null ? new SimpleDateFormat("dd-MM-yyyy").format(date) : "N/A");
            });
            kilometrageColumn.setCellValueFactory(cellData ->
                    new SimpleIntegerProperty(cellData.getValue().getKilometrage()).asObject());
            typeColumn.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getType() != null
                            ? cellData.getValue().getType().toString()
                            : "N/A"));

            actionsColumn.setCellFactory(col -> new TableCell<>() {
                private final Button viewButton = new Button("🔍");
                private final Button deleteButton = new Button("❌");
                private final Button advancedButton = new Button("🔧");
                private final HBox container = new HBox(10, viewButton, deleteButton, advancedButton);

                {
                    viewButton.setOnAction(event -> {
                        int index = getIndex();
                        if (index >= 0 && index < getTableView().getItems().size()) {
                            Vehicule vehicule = getTableView().getItems().get(index);
                            matricule.setText(vehicule.getMatricule().substring(0,vehicule.getMatricule().indexOf("ت")));
                            amatricule.setText(vehicule.getMatricule().substring(vehicule.getMatricule().indexOf("س")+1,vehicule.getMatricule().length()));
                            datem.setText(new SimpleDateFormat("dd-MM-yyyy").format(vehicule.getDatem()));
                            kilo.setText(String.valueOf(vehicule.getKilometrage()));
                            type.setText(vehicule.getType() != null ? vehicule.getType().toString() : "N/A");
                            disableInputs(true);
                        }
                    });

                    advancedButton.setOnAction(event -> {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/VehiculesAdvancedInfo.fxml"));
                            Parent root = loader.load();
                            VehiculeAdvancedInfoController controller = loader.getController();
                            controller.initialize(getTableView().getItems().get(getIndex()));
                            Stage stage = new Stage();
                            stage.setScene(new Scene(root));
                            stage.setTitle("Advanced Vehicle Info");
                            stage.show();
                        } catch (IOException e) {
                            e.printStackTrace();
                            showError("Erreur lors du chargement de la fenêtre avancée.");
                        }
                    });


                    deleteButton.setOnAction(event -> {
                        int index = getIndex();
                        if (index >= 0 && index < getTableView().getItems().size()) {
                            Vehicule vehicule = getTableView().getItems().get(index);
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cette véhicule ?", ButtonType.YES, ButtonType.NO);
                            alert.showAndWait().ifPresent(response -> {
                                if (response == ButtonType.YES) {
                                    try {
                                        vehiculesS.deleteVehicule(vehicule.getMatricule());
                                        Platform.runLater(() -> vehiculesList.remove(vehicule));
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                        Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Error deleting vehicle: " + e.getMessage());
                                        errorAlert.show();
                                    }
                                }
                            });
                        }
                    });

                    String buttonStyle = "-fx-font-size: 12px; -fx-background-color: rgba(62,72,84,0.75); -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px; -fx-border-radius: 5px; -fx-cursor: hand;";
                    viewButton.setStyle(buttonStyle);
                    deleteButton.setStyle(buttonStyle);
                    advancedButton.setStyle(buttonStyle);
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : container);
                }
            });


        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setContentText("Erreur lors du chargement des données ");
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    private void disableInputs(boolean b) {
        matricule.setDisable(b);
        amatricule.setDisable(b);
        datem.setDisable(b);
        kilo.setDisable(b);
        type.setDisable(b);
    }

    public void filterVehicles() {
        String query = searchField.getText().toLowerCase();
        ObservableList<Vehicule> filteredList = FXCollections.observableArrayList();
        for (Vehicule vehicule : vehiculesList) {
            if ((vehicule.getMatricule() != null && vehicule.getMatricule().toLowerCase().contains(query)) ||
                    (vehicule.getType() != null && vehicule.getType().toString().toLowerCase().contains(query))) {
                filteredList.add(vehicule);
            }
        }
        vehiculesTable.setItems(filteredList);
    }

    public void sortByMatricule() { sortVehicules("matricule"); }
    public void sortByDate() { sortVehicules("datem"); }
    public void sortByKilometrage() { sortVehicules("kilometrage"); }
    public void sortByType() { sortVehicules("type"); }
    private void sortVehicules(String column) {
        try {
            List<Vehicule> vehicules = vehiculesS.getVehiculeOrdered(column, "DESC");
            vehiculesList.setAll(vehicules);
            vehiculesTable.setItems(vehiculesList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void addvehicule() {
        disableInputs(false);
        matricule.setText("");
        amatricule.setText("");
        datem.setText("");
        type.setText("");
        kilo.setText("");
        indication.setText("Entrer les nouvelles informations ");
    }
    public void update() {
        disableInputs(false);
    }
    public void save() throws SQLException {
        if (!matricule.isDisable()) {
            if (isValidInput()) {
                try {
                    Vehicule newVehicule = createVehicule();

                    if (vehiculesS.getVehicule(newVehicule.getMatricule()) != null) {
                        vehiculesS.updateVehicule(newVehicule);
                    } else {
                        vehiculesS.addVehicule(newVehicule);
                    }
                    cancel();
                } catch (ParseException e) {
                    showError("Format de date invalide. Veuillez utiliser dd-MM-yyyy.");
                } catch (NumberFormatException e) {
                    showError("Valeur numérique invalide. Veuillez entrer des nombres valides.");
                }
            } else {
                showError("Données invalides");
            }
        }
    }
    private boolean isValidInput() {
        if (matricule.getText().isEmpty() || kilo.getText().isEmpty() || amatricule.getText().isEmpty()) {
            return false;
        }
        if (!matricule.getText().strip().matches("[0-9]{3,5}") || !amatricule.getText().strip().matches("[0-9]{2}")) {
            return false;
        }
        int year = Integer.parseInt(amatricule.getText().trim());
        int currentYear = LocalDate.now().getYear() % 100;
        if (year > currentYear) {
            return false;
        }
        return isValidType(type.getText()) && LocalDate.parse(datem.getText(),DateTimeFormatter.ofPattern("dd-MM-yyyy")).isBefore(LocalDate.now()) && isValidDate(datem.getText());
    }
    private boolean isValidType(String vehicleType) {
        List<String> validTypes = Arrays.asList("moto", "camion", "voiture");
        return validTypes.contains(vehicleType.toLowerCase());
    }
    private Vehicule createVehicule() throws ParseException {
        String fullMatricule = matricule.getText() + "تونس" + amatricule.getText();
        Date dateMiseEnService = new SimpleDateFormat("dd-MM-yyyy").parse(datem.getText());
        int kilometrage = Integer.parseInt(kilo.getText());
        TypeP typeVehicule = VehiculesDAO.typeOf(type.getText());
        return new Vehicule(fullMatricule, dateMiseEnService, kilometrage, typeVehicule);
    }
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(message);
        alert.showAndWait();
    }
    public void cancel() {
        initialize();
        matricule.setText("matricule");
        amatricule.setText("AN");
        datem.setText("dd-mm-yyyy");
        kilo.setText("***** kilomètres");
        type.setText("MOTO/VOITURE/CAMION");
        disableInputs(true);
    }
    private boolean isValidDate(String dateText) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        try {
            LocalDate.parse(dateText, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

}
