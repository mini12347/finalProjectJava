package Controllers;
import DAO.VehiculesDAO;
import Entities.Vehicule;
import Service.VehiculesS;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
                private final Button advancedButton = new Button("⚙️");
                private final HBox container = new HBox(10, viewButton, deleteButton, advancedButton);

                {
                    viewButton.setOnAction(event -> {
                        int index = getIndex();
                        if (index >= 0 && index < getTableView().getItems().size()) {
                            Vehicule vehicule = getTableView().getItems().get(index);
                            matricule.setText(vehicule.getMatricule());
                            datem.setText(new SimpleDateFormat("dd-MM-yyyy").format(vehicule.getDatem()));
                            kilo.setText(String.valueOf(vehicule.getKilometrage()));
                            type.setText(vehicule.getType() != null ? vehicule.getType().toString() : "N/A");
                            disableInputs(true);
                        }
                    });

                    advancedButton.setOnAction(event -> {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/VehiculesAdvancedInfo.fxml"));
                        try {
                            Parent root = loader.load();
                            VehiculeAdvancedInfoController controller = loader.getController();
                            controller.initialize(vehicules.get(getIndex()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

                    deleteButton.setOnAction(event -> {
                        int index = getIndex();
                        if (index >= 0 && index < getTableView().getItems().size()) {
                            Vehicule vehicule = getTableView().getItems().get(index);
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ce véhicule ?", ButtonType.YES, ButtonType.NO);
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
        if(!matricule.isDisable()) {
            if(valid() && (type.getText().equalsIgnoreCase("moto") || type.getText().equalsIgnoreCase("camion") || type.getText().equalsIgnoreCase("voiture") ) && (!Objects.equals(kilo.getText(), "")) && (!Objects.equals(matricule.getText(), ""))&&(matricule.getText().matches("[0-9]{3,5}")) && (amatricule.getText().matches("[0-9]{2}")) && (Integer.parseInt(amatricule.getText())<LocalDate.now().getYear())){
                Vehicule newVehicule = null;
                try {
                    newVehicule = new Vehicule(matricule.getText()+"تونس"+amatricule.getText(),new SimpleDateFormat("dd-MM-yyyy").parse(datem.getText()),Integer.parseInt(kilo.getText()), VehiculesDAO.typeOf(type.getText()));
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                if (vehiculesS.getVehicule(matricule.getText()) != null) {
                    vehiculesS.updateVehicule(newVehicule);
                } else {
                    vehiculesS.addVehicule(newVehicule);
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setContentText("Données invalides ");
                alert.showAndWait();
            }
        }

        cancel();
    }

    private boolean valid() {
        try {
            LocalDate parsedDate = LocalDate.parse(datem.getText(), DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            return LocalDate.now().isAfter(parsedDate);
        } catch (Exception e) {
            return false;
        }
    }

    public void cancel() {
        initialize();
        matricule.setText("matricule");
        amatricule.setText("annee matricule");
        datem.setText("dd-mm-yyyy");
        kilo.setText("***** kilomètres");
        type.setText("MOTO/VOITURE/CAMION");
        disableInputs(true);
    }
}
