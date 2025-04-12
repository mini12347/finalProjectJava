package Controllers;

import Service.PaiementService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.sql.SQLException;

public class PasswordDialogController {
    @FXML
    private PasswordField passwordField;

    @FXML
    private Button confirmButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Label errorLabel;

    private boolean isConfirmed = false;
    private String password = "";
    private PaiementService paiementService;

    public PasswordDialogController() {
        try {
            paiementService = new PaiementService();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        // Action when pressing Enter in the password field
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleConfirm();
            }
        });

        // Set focus on password field when dialog opens
        passwordField.requestFocus();

        // Add animation to error message display
        errorLabel.setOpacity(0);
    }

    @FXML
    public void handleConfirm() {
        String enteredPassword = passwordField.getText();
        if (enteredPassword == null || enteredPassword.trim().isEmpty()) {
            showError("Veuillez saisir un mot de passe");
            return;
        }

        this.password = enteredPassword;

        // Verify password with the service
        if (paiementService.verifierMotDePasse(password)) {
            this.isConfirmed = true;
            closeStage();
        } else {
            showError("Mot de passe incorrect");
            passwordField.clear();
            passwordField.requestFocus();
        }
    }

    @FXML
    public void handleCancel() {
        this.isConfirmed = false;
        closeStage();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);

        // Add a subtle animation to draw attention
        errorLabel.setOpacity(1);
    }

    private void closeStage() {
        Stage stage = (Stage) confirmButton.getScene().getWindow();
        stage.close();
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public String getPassword() {
        return password;
    }
}