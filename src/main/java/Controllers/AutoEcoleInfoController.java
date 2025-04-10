package Controllers;
import Entities.AutoEcole;
import Entities.Disponibility;
import Entities.Hours;
import Service.AutoEcoleInfosS;
import Service.PDFGenerator;
import com.lowagie.text.DocumentException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;

public class AutoEcoleInfoController {
    @FXML public CheckBox mondayCheck, tuesdayCheck, wedCheck, thurCheck, FriCheck, SatCheck, SunCheck;
    @FXML private TextField mondayStart, mondayEnd, tuesdayStart, tuesdayEnd,
            wedStart, wedEnd, thurStart, thurEnd,
            FriStart, FriEnd, SatStart, SatEnd, SunStart, SunEnd;
    @FXML private TextField nomField, numtelField, emailField, adresseField;
    @FXML private Label indication;
    @FXML private Button generatePdfBtn;

    private AutoEcoleInfosS autoEcoleService = new AutoEcoleInfosS();
    private AutoEcole autoEcole;

    public AutoEcoleInfoController() throws SQLException {
    }

    public void initialize() {
        try {
            autoEcole = autoEcoleService.getAutoEcole();
            if (autoEcole != null) {
                nomField.setText(autoEcole.getNom());
                numtelField.setText(String.valueOf(autoEcole.getNumtel()));
                emailField.setText(autoEcole.getEmail());
                adresseField.setText(autoEcole.getAdresse());
                deserializeDisponibility(autoEcole.getHoraire().toString());
            }
        } catch (SQLException e) {
            showErrorMessage("Erreur lors du chargement des données.");
            e.printStackTrace();
        }
    }

    private String serializeDisponibility() {
        StringBuilder sb = new StringBuilder();

        if (mondayCheck.isSelected()) sb.append("MONDAY:").append(mondayStart.getText()).append("-").append(mondayEnd.getText()).append(";");
        if (tuesdayCheck.isSelected()) sb.append("TUESDAY:").append(tuesdayStart.getText()).append("-").append(tuesdayEnd.getText()).append(";");
        if (wedCheck.isSelected()) sb.append("WEDNESDAY:").append(wedStart.getText()).append("-").append(wedEnd.getText()).append(";");
        if (thurCheck.isSelected()) sb.append("THURSDAY:").append(thurStart.getText()).append("-").append(thurEnd.getText()).append(";");
        if (FriCheck.isSelected()) sb.append("FRIDAY:").append(FriStart.getText()).append("-").append(FriEnd.getText()).append(";");
        if (SatCheck.isSelected()) sb.append("SATURDAY:").append(SatStart.getText()).append("-").append(SatEnd.getText()).append(";");
        if (SunCheck.isSelected()) sb.append("SUNDAY:").append(SunStart.getText()).append("-").append(SunEnd.getText()).append(";");

        return sb.toString();
    }

    private void deserializeDisponibility(String data) {
        if (data == null || data.isEmpty()) return;

        String[] entries = data.split(";");
        for (String entry : entries) {
            String[] parts = entry.split(":");
            if (parts.length < 2) continue;

            String day = parts[0];
            String[] hours = parts[1].split("-");

            if (hours.length < 2) continue;

            switch (day) {
                case "MONDAY":
                    mondayCheck.setSelected(true);
                    mondayStart.setText(hours[0]);
                    mondayEnd.setText(hours[1]);
                    break;
                case "TUESDAY":
                    tuesdayCheck.setSelected(true);
                    tuesdayStart.setText(hours[0]);
                    tuesdayEnd.setText(hours[1]);
                    break;
                case "WEDNESDAY":
                    wedCheck.setSelected(true);
                    wedStart.setText(hours[0]);
                    wedEnd.setText(hours[1]);
                    break;
                case "THURSDAY":
                    thurCheck.setSelected(true);
                    thurStart.setText(hours[0]);
                    thurEnd.setText(hours[1]);
                    break;
                case "FRIDAY":
                    FriCheck.setSelected(true);
                    FriStart.setText(hours[0]);
                    FriEnd.setText(hours[1]);
                    break;
                case "SATURDAY":
                    SatCheck.setSelected(true);
                    SatStart.setText(hours[0]);
                    SatEnd.setText(hours[1]);
                    break;
                case "SUNDAY":
                    SunCheck.setSelected(true);
                    SunStart.setText(hours[0]);
                    SunEnd.setText(hours[1]);
                    break;
            }
        }
    }

    public void saveChanges() {
        try {
            String name = nomField.getText();
            String phone = numtelField.getText();
            String email = emailField.getText();
            String address = adresseField.getText();

            StringBuilder errorMessage = new StringBuilder();

            if (name.isEmpty()) errorMessage.append("Nom requis.\n");
            if (phone.isEmpty()) errorMessage.append("Téléphone requis.\n");
            if (!isValidPhone(phone)) errorMessage.append("Téléphone invalide.\n");
            if (email.isEmpty()) errorMessage.append("Email requis.\n");
            if (!isValidEmail(email)) errorMessage.append("Email invalide.\n");
            if (address.isEmpty()) errorMessage.append("Adresse requise.\n");

            if (!errorMessage.isEmpty()) {
                indication.setText("Erreurs:\n" + errorMessage);
                showErrorMessage("Erreur lors de la mise à jour.");
                return;
            }

            String disponibility = serializeDisponibility();
            AutoEcole newAutoEcole = new AutoEcole(
                    name,
                    Integer.parseInt(phone),
                    email,
                    address,
                    new Disponibility(parseDisponibility(disponibility))
            );

            autoEcoleService.addAutoEcole(newAutoEcole);
            showSuccessMessage("Informations mises à jour avec succès !");
            initialize();
        } catch (SQLException e) {
            showErrorMessage("Erreur lors de la mise à jour.");
            e.printStackTrace();
        }
    }

    private Map<DayOfWeek, Hours> parseDisponibility(String text) {
        Map<DayOfWeek, Hours> map = new HashMap<>();
        for (String entry : text.split(";")) {
            String[] parts = entry.split(":");
            if (parts.length < 2) continue;

            DayOfWeek day = DayOfWeek.valueOf(parts[0]);
            String[] hours = parts[1].split("-");
            if (hours.length < 2) continue;

            map.put(day, new Hours(Integer.parseInt(hours[0]), Integer.parseInt(hours[1])));
        }
        return map;
    }

    private boolean isValidPhone(String phone) {
        return phone.matches("^[0-9]{8}$");
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private void showSuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void load() {
        initialize();
        nomField.setDisable(true);
        numtelField.setDisable(true);
        emailField.setDisable(true);
        adresseField.setDisable(true);
        mondayCheck.setDisable(true);
        wedCheck.setDisable(true);
        thurCheck.setDisable(true);
        FriCheck.setDisable(true);
        SatCheck.setDisable(true);
        SunCheck.setDisable(true);
        mondayStart.setDisable(true);
        wedStart.setDisable(true);
        tuesdayCheck.setDisable(true);
        tuesdayStart.setDisable(true);
        wedEnd.setDisable(true);
        thurEnd.setDisable(true);
        FriEnd.setDisable(true);
        SatEnd.setDisable(true);
        thurStart.setDisable(true);
        FriStart.setDisable(true);
        SatStart.setDisable(true);
        SunStart.setDisable(true);
        mondayEnd.setDisable(true);
        tuesdayEnd.setDisable(true);
        thurEnd.setDisable(true);
        FriEnd.setDisable(true);
        SatEnd.setDisable(true);
        SunEnd.setDisable(true);
    }

    public void handleUpdate() {
        nomField.clear();
        nomField.setDisable(false);
        numtelField.clear();
        numtelField.setDisable(false);
        emailField.clear();
        emailField.setDisable(false);
        adresseField.clear();
        adresseField.setDisable(false);
        mondayCheck.setDisable(false);
        mondayStart.setDisable(false);
        mondayEnd.setDisable(false);
        wedStart.setDisable(false);
        wedEnd.setDisable(false);
        thurCheck.setDisable(false);
        thurStart.setDisable(false);
        thurEnd.setDisable(false);
        tuesdayCheck.setDisable(false);
        tuesdayStart.setDisable(false);
        tuesdayEnd.setDisable(false);
        wedCheck.setDisable(false);
        FriCheck.setDisable(false);
        FriStart.setDisable(false);
        FriEnd.setDisable(false);
        SatCheck.setDisable(false);
        SatStart.setDisable(false);
        SunCheck.setDisable(false);
        SunStart.setDisable(false);
        SunEnd.setDisable(false);
        SatEnd.setDisable(false);
        mondayCheck.setSelected(false);
        tuesdayCheck.setSelected(false);
        wedCheck.setSelected(false);
        thurCheck.setSelected(false);
        FriCheck.setSelected(false);
        SatCheck.setSelected(false);
        SunCheck.setSelected(false);
        mondayStart.clear();
        tuesdayStart.clear();
        wedStart.clear();
        thurStart.clear();
        FriStart.clear();
        SatStart.clear();
        SunStart.clear();
        mondayEnd.clear();
        tuesdayEnd.clear();
        wedEnd.clear();
        thurEnd.clear();
        FriEnd.clear();
        SatEnd.clear();
        SunEnd.clear();
        indication.setText("Entrer les nouvelles informations de votre auto école ");
    }

    @FXML
    public void generatePDF() {
        try {
            if (autoEcole == null) {
                autoEcole = autoEcoleService.getAutoEcole();
            }

            if (autoEcole == null) {
                showErrorMessage("Aucune information d'auto-école disponible.");
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le PDF");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
            fileChooser.setInitialFileName(autoEcole.getNom() + "_informations.pdf");

            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                PDFGenerator.generateAutoEcolePDF(autoEcole, file.getAbsolutePath());
                showSuccessMessage("PDF généré avec succès à l'emplacement : " + file.getAbsolutePath());
            }
        } catch (SQLException e) {
            showErrorMessage("Erreur lors de la récupération des données.");
            e.printStackTrace();
        } catch (IOException | DocumentException e) {
            showErrorMessage("Erreur lors de la génération du PDF : " + e.getMessage());
            e.printStackTrace();
        }
    }
}