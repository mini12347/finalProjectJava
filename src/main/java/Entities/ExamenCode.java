package Entities;

import java.sql.Time;
import java.time.LocalDate;

public class ExamenCode {
    private int id;
    private LocalDate date;
    private Time time;
    private Candidat candidat;
    private Res resultat;
    private TypeP type;
    private double cout;
    public ExamenCode(LocalDate date, Time time, Candidat candidat,  Res resultat, TypeP type , double cout) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.candidat = candidat;
        this.resultat = resultat;
        this.type = type;
        this.cout = cout;
    }
    public TypeP getType() {
        return type;
    }
    public void setType(TypeP type) {
        this.type = type;
    }
    public LocalDate getDate() {
        return date;
    }
    public void setDate(LocalDate date) {
        this.date = date;
    }
    public Time getTime() {
        return time;
    }
    public void setTime(Time time) {
        this.time = time;
    }
    public Candidat getCandidat() {
        return candidat;
    }
    public void setCandidat(Candidat candidat) {
        this.candidat = candidat;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public Res getResultat() {
        return resultat;
    }
    public void setResultat(Res resultat) {
        this.resultat = resultat;
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
                ",cout="+cout
                ;
    }
}