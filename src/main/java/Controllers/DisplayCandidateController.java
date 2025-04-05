package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import Entities.Candidat;
import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Optional;

public class DisplayCandidateController {
    @FXML
    public Label dateNaissance;
    @FXML
    private Label cin;

    @FXML
    private Label email;

    @FXML
    private Label nom;

    @FXML
    private Label prenom;

    @FXML
    private Label tel;

    @FXML
    private ImageView cinImage;


    private Stage dialogStage;
    private Candidat candidate;
    //Permet de lier la fenêtre à ce contrôleur pour la gestion de la fenêtre modale.
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }


    //Remplie les champs d'interface avec les informations du candidat (nom, prénom, CIN, téléphone, email et l'image de la CIN).
    public void setCandidate(Candidat candidate) {
        this.candidate = candidate;

        if (candidate != null) {
            nom.setText(candidate.getNom());
            prenom.setText(candidate.getPrenom());
            cin.setText(String.valueOf(candidate.getCIN()));
            tel.setText(String.valueOf(candidate.getNumTelephone()));
            email.setText(candidate.getMail());

            // Fix for date formatting - use SimpleDateFormat for java.util.Date objects
            dateNaissance.setText(
                    Optional.ofNullable(candidate.getDateNaissance())
                            .map(date -> {
                                SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy", Locale.FRENCH);
                                return formatter.format(date);
                            })
                            .orElse("Date de naissance non renseignée")
            );

            // Convert byte[] to Image
            if (candidate.getCinImage() != null) {
                Image image = new Image(new ByteArrayInputStream(candidate.getCinImage()));
                cinImage.setImage(image);
            }
        }
    }
}