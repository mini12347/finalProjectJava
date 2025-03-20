package Entities;
import java.sql.Time;
import java.time.LocalDate;
public class Seance {
    protected int id;
    protected LocalDate date;
    protected Time time;
    protected TypeP typePermis;
    protected Moniteur moniteur;
    public Seance(int id, LocalDate date, Time time, TypeP typePermis, Moniteur moniteur) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.typePermis = typePermis;
        this.moniteur = moniteur;
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
    public void setTypePermis(TypeP typePermis) {
        this.typePermis = typePermis;
    }
    public Moniteur getMoniteur() {
        return moniteur;
    }
    public void setMoniteur(Moniteur moniteur) {
        this.moniteur = moniteur;
    }


    @Override
    public String toString() {
        return "Seance{" +
                "id=" + id +
                ", date=" + date +
                ", time=" + time +
                ", typePermis=" + typePermis +
                '}';
    }
}
