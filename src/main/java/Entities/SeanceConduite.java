package Entities;

import java.sql.Time;
import java.time.LocalDate;

public class SeanceConduite extends Seance {
    private Moniteur moniteur;
    private String Localisation;
    public SeanceConduite(int id, LocalDate date, Time time, String Localisation,TypeP t,Moniteur moniteur) {
        super(id, date, time,t,moniteur);
        this.Localisation = Localisation;
    }
    public Moniteur getMoniteur() {
        return moniteur;
    }
    public void setMoniteur(Moniteur moniteur) {
        this.moniteur = moniteur;
    }
    public String getLocalisation() {
        return Localisation;
    }
    public void setLocalisation(String Localisation) {
        this.Localisation = Localisation;
    }

    @Override
    public String toString() {
        return "SeanceConduite{" +
                super.toString()+
                "moniteur=" + moniteur +
                ", Localisation='" + Localisation + '\'' +
                "} "  ;
    }
}
