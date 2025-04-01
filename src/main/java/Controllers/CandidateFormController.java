package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import Entities.Candidat;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class CandidateFormController {


//Ce contrôleur gère le formulaire d'ajout et de modification d'un candidat.
// Il est utilisé dans la boîte de dialogue modale affichée lors de l'ajout ou de l'édition d'un candidat.



    @FXML
    private TextField nomField, prenomField, cinField, telephoneField, emailField;
    @FXML
    private DatePicker dateNaissancePicker;

    private File selectedImageFile;
    @FXML private ImageView cinImageView;

    private Stage dialogStage;
    private Candidat candidate;

    // Set the dialog stage to close the form later
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setCandidat(Candidat candidate) {
        this.candidate = candidate;

        if (candidate != null) {
            nomField.setText(candidate.getNom());
            prenomField.setText(candidate.getPrenom());
            cinField.setText(String.valueOf(candidate.getCIN()));
            telephoneField.setText(String.valueOf(candidate.getNumTelephone()));
            emailField.setText(candidate.getMail());
            dateNaissancePicker.setValue(candidate.getDateNaissance());


            if (candidate.getCinImage() != null) {
                try {
                    Image image = new Image(new ByteArrayInputStream(candidate.getCinImage()));
                    cinImageView.setImage(image);
                } catch (Exception e) {
                    System.err.println("Error displaying image: " + e.getMessage());
                }
            }
        }
    }


    @FXML
    private void handleSave() {
        // Create a new Candidate object based on the form fields
        candidate = new Candidat ();
        candidate.setNom(nomField.getText());
        candidate.setPrenom(prenomField.getText());
        candidate.setCIN(Integer.parseInt(cinField.getText()));
        candidate.setMail(emailField.getText());
        candidate.setNumTelephone(Integer.parseInt(telephoneField.getText()));
        candidate.setDateNaissance(dateNaissancePicker.getValue());


        if (selectedImageFile != null) {
            try {
                byte[] imageBytes = Files.readAllBytes(selectedImageFile.toPath());
                candidate.setCinImage(imageBytes);
            } catch (IOException e) {
                System.err.println("Error reading image file: " + e.getMessage());
            }
        }

        dialogStage.close();
    }

    // Method to handle the image upload button (you can implement your file upload logic here)
    @FXML
    private void handleImageUpload() { //Rôle : Permet à l'utilisateur de sélectionner une image à partir de son ordinateur.
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        selectedImageFile = fileChooser.showOpenDialog(cinImageView.getScene().getWindow());

        if (selectedImageFile != null) {
            cinImageView.setImage(new Image(selectedImageFile.toURI().toString()));
        }
    }

    // Getter to retrieve the Candidate object
    public Candidat getCandidat() {
        return candidate;
    }
}




