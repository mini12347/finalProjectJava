package Entities;

import java.sql.Time;
import java.time.LocalDate;

public class Paiement {
    protected int idPaiement;
    protected LocalDate date;
    protected Time time;
    protected int idClient;
    private String description;
    private Double montant;
    public Paiement(int idPaiement, LocalDate date, Time time, int idClient, String description, Double montant) {
        this.idPaiement = idPaiement;
        this.date = date;
        this.time = time;
        this.idClient = idClient;
        this.description = description;
        this.montant = montant;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Double getMontant() {
        return montant;
    }
    public void setMontant(Double montant) {
        this.montant = montant;
    }
    public int getIdPaiement() {
        return idPaiement;
    }
    public void setIdPaiement(int idPaiement) {
        this.idPaiement = idPaiement;
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
    public int getIdClient() {
        return idClient;
    }
    public void setIdClient(int idClient) {
        this.idClient = idClient;
    }

    @Override
    public String toString() {
        return "idPaiement=" + idPaiement +
                ", date=" + date +
                ", time=" + time +
                ", idClient=" + idClient +
                ", description='" + description + '\'' +
                ", montant=" + montant;
    }
}
