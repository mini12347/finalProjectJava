package Controllers;

import Entities.TypeP;
import Entities.Vehicule;
import Service.VehiculePDFGenerator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

public class VehiculeAdvancedInfoController {
    @FXML public Label indication;
    @FXML public TextField matricule, amatricule, datem, type, kilo;
    @FXML public Label kilor, datev, datelv, datea;
    @FXML public TableView reparationsTable;
    @FXML public TableColumn desColumn, datemColumn, MontantColumn, FactureColumn;
    @FXML public Button generatePdfButton; // Nouveau bouton pour générer le PDF

    private Vehicule currentVehicule; // Pour stocker la référence du véhicule actuel

    public void initialize(Vehicule v) {
        this.currentVehicule = v; // Enregistrer le véhicule pour l'utiliser plus tard

        matricule.setText(v.getMatricule().substring(0,v.getMatricule().indexOf("ت")));
        amatricule.setText(v.getMatricule().substring(v.getMatricule().indexOf("س")+1,v.getMatricule().length()));
        datem.setText(new SimpleDateFormat("dd-MM-yyyy").format(v.getDatem()));
        kilo.setText(String.valueOf(v.getKilometrage()));
        type.setText(v.getType() != null ? v.getType().toString() : "N/A");
        int currentYear = LocalDate.now().getYear();
        try {
            int numMatricule = Integer.parseInt(v.getMatricule().substring(0, v.getMatricule().indexOf("ت")).trim());
            if (numMatricule % 2 == 0 && v.getType() != TypeP.MOTO) {
                datelv.setText("5 mars " + currentYear);
            } else {
                datelv.setText("5 avril " + currentYear);
            }
        } catch (NumberFormatException e) {
            datelv.setText("Erreur immatriculation");
        }

        try {
            Date dateMiseEnServiceDate = v.getDatem();
            LocalDate dateMiseEnService = dateMiseEnServiceDate.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();

            int ageVehicule = currentYear - dateMiseEnService.getYear();
            LocalDate dateProchaineVisite;

            if (ageVehicule < 4) {
                dateProchaineVisite = dateMiseEnService.plusYears(4);
            } else if (ageVehicule < 10) {
                dateProchaineVisite = dateMiseEnService.plusYears((ageVehicule / 2) * 2 + 2);
            } else {
                dateProchaineVisite = LocalDate.now().plusMonths(6);
            }
            datev.setText(dateProchaineVisite.toString());
            LocalDate dateAssurance = null;
            if(LocalDate.of(LocalDate.now().getYear(), dateMiseEnService.getMonthValue(), dateMiseEnService.getDayOfMonth()).isAfter(LocalDate.now())){
                dateAssurance = LocalDate.of(LocalDate.now().getYear(), dateMiseEnService.getMonthValue(), dateMiseEnService.getDayOfMonth());
            } else {
                dateAssurance = LocalDate.of(LocalDate.now().getYear()+1, dateMiseEnService.getMonthValue(), dateMiseEnService.getDayOfMonth());
            }
            datea.setText(dateAssurance.toString());
            int prochaineVidange = 10000 - (v.getKilometrage() % 10000);
            kilor.setText(String.valueOf(prochaineVidange));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleGeneratePDF(ActionEvent event) {
        try {
            // Créer un sélecteur de fichier
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le PDF");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf")
            );
            fileChooser.setInitialFileName("vehicule_" + currentVehicule.getMatricule().replace(" ", "_").replace("ت", "").replace("س", "") + ".pdf");

            // Afficher la boîte de dialogue de sauvegarde
            Stage stage = (Stage) generatePdfButton.getScene().getWindow();
            File selectedFile = fileChooser.showSaveDialog(stage);

            if (selectedFile != null) {
                // Générer le PDF à l'emplacement sélectionné
                VehiculePDFGenerator.generatePDF(currentVehicule, selectedFile.getAbsolutePath());

                // Afficher une confirmation
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Génération PDF");
                alert.setHeaderText("PDF créé avec succès");
                alert.setContentText("Le fichier a été enregistré à: " + selectedFile.getAbsolutePath());
                alert.showAndWait();
            }
        } catch (Exception e) {
            // Gérer les erreurs
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur lors de la génération du PDF");
            alert.setContentText("Détails: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }
}