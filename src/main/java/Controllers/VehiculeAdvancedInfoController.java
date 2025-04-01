package Controllers;
import Entities.TypeP;
import Entities.Vehicule;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

public class VehiculeAdvancedInfoController {
    @FXML public Label indication;
    @FXML public TextField matricule,amatricule, datem, type, kilo;
    @FXML public Label kilor, datev, datelv, datea;
    @FXML public TableView reparationsTable;
    @FXML public TableColumn desColumn,datemColumn,MontantColumn,FactureColumn;

    public void initialize(Vehicule v) {
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
            LocalDate dateAssurance =null;
            if(LocalDate.of(LocalDate.now().getYear(), dateMiseEnService.getMonthValue(), dateMiseEnService.getDayOfMonth()).isAfter(LocalDate.now())){
                dateAssurance =LocalDate.of(LocalDate.now().getYear(), dateMiseEnService.getMonthValue(), dateMiseEnService.getDayOfMonth());
            }else{
                dateAssurance=LocalDate.of(LocalDate.now().getYear()+1, dateMiseEnService.getMonthValue(), dateMiseEnService.getDayOfMonth());
            }
            datea.setText(dateAssurance.toString());
            int prochaineVidange = 10000 - (v.getKilometrage() % 10000);
            kilor.setText(String.valueOf(prochaineVidange));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
