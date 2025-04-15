package Entities;
import java.sql.Time;
import java.time.LocalDate;

public abstract class Seance {
    protected int id;
    protected LocalDate date;
    protected Time time;
    protected TypeP typePermis;
    protected int idMoniteur;
    protected int idCandidat;

    public Seance(int id, LocalDate date, Time time, TypeP typePermis, int idMoniteur, int idCandidat) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.typePermis = typePermis;
        this.idMoniteur = idMoniteur;
        this.idCandidat = idCandidat;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public TypeP getTypePermis() {
        return typePermis;
    }

    public int getIdMoniteur() {
        return idMoniteur;
    }

    public void setIdMoniteur(int idMoniteur) {
        this.idMoniteur = idMoniteur;
    }

    public int getIdCandidat() {
        return idCandidat;
    }

    public void setIdCandidat(int idCandidat) {
        this.idCandidat = idCandidat;
    }

    public void setTypePermis(TypeP typePermis) {
        this.typePermis = typePermis;
    }

    public abstract String getType();

    public abstract String getInfoSpecifique();

    @Override
    public String toString() {
        return "Seance{" +
                "id=" + id +
                ", date=" + date +
                ", time=" + time +
                ", typePermis=" + typePermis +
                '}';
    }

    public abstract void setInfoSpecifique(String s);
}
