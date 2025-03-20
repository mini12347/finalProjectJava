package Controllers;
import Entities.AutoEcole;
import Entities.Disponibility;
import Entities.Hours;
import DAO.AutoEcoleDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;

public class AutoEcoleInfoController {
    @FXML private TextField nomField, numtelField, emailField, adresseField, horaireField;
    @FXML private Label indication;
    private AutoEcoleDAO autoEcoleDAO = new AutoEcoleDAO();
    private ObservableList<AutoEcole> autoEcoleList = FXCollections.observableArrayList();
    public void initialize() {
        try {
            autoEcoleList.addAll(autoEcoleDAO.getAllAutoEcoles());
            nomField.setText(autoEcoleList.get(0).getNom());
            numtelField.setText(String.valueOf(autoEcoleList.get(0).getNumtel()));
            emailField.setText(autoEcoleList.get(0).getEmail());
            adresseField.setText(autoEcoleList.get(0).getAdresse());
            horaireField.setText(autoEcoleList.get(0).getHoraire().toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Disponibility parseDisponibility(String text) {
        Map<DayOfWeek, Hours> map = new HashMap<>();
        for (String entry : text.split(";")) {
            String[] parts = entry.split(":");
            DayOfWeek day = DayOfWeek.valueOf(parts[0]);
            String[] hours = parts[1].split("-");
            map.put(day, new Hours(Integer.parseInt(hours[0]), Integer.parseInt(hours[1])));
        }
        return new Disponibility(map);
    }

    private void showSuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void handleUpdate() {
        nomField.setText("");
        numtelField.setText("");
        emailField.setText("");
        adresseField.setText("");
        horaireField.setText("");
        indication.setText("entrer les nouveaux informations de l'auto ecole ");
    }

    public void saveChanges() {
        try {
            autoEcoleDAO.addAutoEcole(new AutoEcole(nomField.getText(),Integer.parseInt(numtelField.getText()), emailField.getText(), adresseField.getText(),parseDisponibility( horaireField.getText())));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

