package Entities;

import java.sql.Time;
import java.time.LocalDate;

public class Examen<T> {
    protected LocalDate date;
    protected Time time;
    protected Candidat candidat;
    protected T resultat;
    protected TypeP type;
    public Examen(LocalDate date, Time time, Candidat candidat,  T resultat, TypeP type) {
        this.date = date;
        this.time = time;
        this.candidat = candidat;
        this.resultat = resultat;
        this.type = type;
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

    public T getResultat() {
        return resultat;
    }
    public void setResultat(T resultat) {
        this.resultat = resultat;
    }

    @Override
    public String toString() {
        return "date=" + date +
                ", time=" + time +
                ", candidat=" + candidat +
                ",type permis ="+type+
                ", resultat=" + resultat;
    }
}
