package Entities;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Date;
import java.util.Objects;

public class Reparation {
    private int id;
    private String matriculeVehicule;
    private String description;
    private Date date;
    private double cout;
    private int kilometrage; // Kilométrage du véhicule au moment de la réparation
    private StringProperty facture_scan = new SimpleStringProperty(); // URL du fichier scanné

    // Updated constructor
    public Reparation(int id, String matriculeVehicule, String description, Date date, double cout, int kilometrage, String factureScan) {
        this.id = id; // Set id
        this.matriculeVehicule = matriculeVehicule;
        this.description = description;
        this.date = date;
        this.cout = cout;
        this.kilometrage = kilometrage;
        this.facture_scan.set(factureScan); // Set the value for facture_scan
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMatriculeVehicule() {
        return matriculeVehicule;
    }

    public void setMatriculeVehicule(String matriculeVehicule) {
        this.matriculeVehicule = matriculeVehicule;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getCout() {
        return cout;
    }

    public void setCout(double cout) {
        this.cout = cout;
    }

    public int getKilometrage() {
        return kilometrage;
    }

    public void setKilometrage(int kilometrage) {
        this.kilometrage = kilometrage;
    }

    public StringProperty factureScanProperty() {
        return facture_scan;
    }

    public String getFactureScan() {
        return facture_scan.get();
    }

    public void setFactureScan(String value) {
        facture_scan.set(value);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Reparation other = (Reparation) obj;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


    @Override
    public String toString() {
        return "Reparation{" +
                "id=" + id +
                ", matriculeVehicule='" + matriculeVehicule + '\'' +
                ", description='" + description + '\'' +
                ", date=" + date +
                ", cout=" + cout +
                ", kilometrage=" + kilometrage +
                ", facture_scan=" + facture_scan +
                '}';
    }


}
