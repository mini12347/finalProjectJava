package Entities;

import java.util.Date;

public class Vehicule {
    private String matricule;
    private Date datem;
    private int kilometrage;
    private TypeP type;
    public Vehicule(String matricule, Date datem, int kilometrage, TypeP type) {
        this.matricule = matricule;
        this.datem = datem;
        this.kilometrage = kilometrage;
        this.type = type;
    }

    public Vehicule() {}

    public String getMatricule() {
        return matricule;
    }
    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }
    public Date getDatem() {
        return datem;
    }
    public void setDatem(Date datem) {
        this.datem = datem;
    }
    public int getKilometrage() {
        return kilometrage;
    }
    public void setKilometrage(int kilometrage) {
        this.kilometrage = kilometrage;
    }
    public TypeP getType() {
        return type;
    }
    public void setType(TypeP type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Vehicule{" +
                "matricule='" + matricule + '\'' +
                ", datem=" + datem +
                ", kilometrage=" + kilometrage +
                ", type=" + type +
                '}';
    }
}
