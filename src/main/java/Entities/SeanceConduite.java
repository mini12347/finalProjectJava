package Entities;

import java.sql.Time;
import java.time.LocalDate;

public class SeanceConduite extends Seance {
    private String localisation;

    public SeanceConduite(int id, LocalDate date, Time time, String localisation, TypeP typePermis, int idMoniteur, int idCandidat) {
        super(id, date, time, typePermis, idMoniteur, idCandidat);
        this.localisation = localisation;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    @Override
    public String getType() {
        return "Conduite";
    }

    @Override
    public String getInfoSpecifique() {
        return localisation;
    }

    @Override
    public String toString() {
        return "SeanceConduite{" +
                super.toString() +
                ", localisation='" + localisation + '\'' +
                '}';
    }

    @Override
    public void setInfoSpecifique(String s) {
        this.localisation = s;
    }
}
