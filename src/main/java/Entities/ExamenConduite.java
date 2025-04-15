package Entities;

import java.sql.Time;
import java.time.LocalDate;

public class ExamenConduite {
    private int id;
    private LocalDate date;
    private Time time;
    private Candidat candidat;
    private Res resultat;
    private TypeP type;
    private String localisation;
    private double cout;

    public ExamenConduite(int id,LocalDate date, Time time, Candidat candidat,  Res resultat, String localisation,TypeP t, double cout) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.candidat = candidat;
        this.resultat = resultat;
        this.localisation=localisation;
        this.type=t;
        this.cout=cout;
    }
    public int getId() {return id;}
    public void setId(int id) {this.id = id;}
    public LocalDate getDate() {return date;}
    public void setDate(LocalDate date) {this.date = date;}
    public Time getTime() {return time;}
    public void setTime(Time time) {this.time = time;}
    public Candidat getCandidat() {return candidat;}
    public void setCandidat(Candidat candidat) {this.candidat = candidat;}
    public Res getResultat() {return resultat;}
    public void setResultat(Res resultat) {this.resultat = resultat;}
    public TypeP getType() {return type;}
    public void setType(TypeP type) {this.type = type;}
    public String getLocalisation() {
        return localisation;
    }
    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }
    public double getCout() {
        return cout;
    }
    public void setCout(double cout) {
        this.cout = cout;
    }


    @Override
    public String toString() {
        return "date=" + date +
                ", time=" + time +
                ", candidat=" + candidat +
                ",type permis ="+type+
                ", resultat=" + resultat+
                ",id="+ id +
                ",localisation=" +localisation+
                ",cout=" +cout;


    }
}
