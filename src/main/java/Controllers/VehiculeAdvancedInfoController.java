package Controllers;
import Entities.TypeP;
import Entities.Vehicule;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.time.LocalDate;
import java.util.Date;

public class VehiculeAdvancedInfoController {
    @FXML public Label indication;
    @FXML public TextField matricule, datem, type, kilo;
    @FXML public Label kilor, datev, datelv, datea;
    public void initialize(Vehicule v) {
        matricule.setText(v.getMatricule());
        datem.setText(v.getDatem().toString());
        type.setText(v.getType().toString());
        kilo.setText(String.valueOf(v.getKilometrage()));

        int currentYear = LocalDate.now().getYear();
        try {
            int numMatricule = Integer.parseInt(v.getMatricule().substring(0, v.getMatricule().indexOf("س")));
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

            LocalDate dateAssurance = dateMiseEnService.plusYears(1);
            datea.setText(dateAssurance.toString());

            int prochaineVidange = 10000 - (v.getKilometrage() % 10000);
            kilor.setText(String.valueOf(prochaineVidange));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void update() {
        new VehiculesController().update();
    }

    public void save() {
        new VehiculesController().update();
    }

    public void cancel() {
        new VehiculesController().cancel();
    }

    public void retour() {
        LandingPController lc = new LandingPController();
        lc.initialize();
        lc.loadFXML("/fxml/Vehicules.fxml");
    }
}
