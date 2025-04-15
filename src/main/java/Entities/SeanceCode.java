package Entities;

import java.sql.Time;
import java.time.LocalDate;

public class SeanceCode extends Seance {
    private int numSalle;

    public SeanceCode(int id, LocalDate date, Time time, TypeP typePermis, int numSalle, int idMoniteur, int idCandidat) {
        super(id, date, time, typePermis, idMoniteur, idCandidat);
        this.numSalle = numSalle;
    }

    public int getNumSalle() {
        return numSalle;
    }

    public void setNumSalle(int numSalle) {
        this.numSalle = numSalle;
    }

    @Override
    public String getType() {
        return "Code";
    }

    @Override
    public String getInfoSpecifique() {
        return "Salle " + numSalle;
    }

    @Override
    public String toString() {
        return "SeanceCode{" +
                super.toString() +
                ", numSalle=" + numSalle +
                '}';
    }

    @Override
    public void setInfoSpecifique(String s) {
        this.numSalle=Integer.parseInt(s.substring(s.indexOf("Salle ")).trim());
    }
}
