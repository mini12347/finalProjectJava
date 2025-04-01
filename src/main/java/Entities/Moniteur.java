package Entities;

import java.time.LocalDate;
import java.util.Date;

public class Moniteur extends Personne {
    private Vehicule vehicule;
    private Disponibility horaire;
    public Moniteur(int cin, String nom, String prenom, String adresse, String mail, int numTelephone, Date dateNaissance, Vehicule vehicule, Disponibility horaire) {
        super(cin, nom, prenom, adresse, mail, numTelephone, dateNaissance);
        this.vehicule = vehicule;
        this.horaire = horaire;
    }
    public Vehicule getVehicule() {
        return vehicule;
    }
    public void setVehicule(Vehicule vehicule) {
        this.vehicule = vehicule;
    }
    public Disponibility getHoraire() {
        return horaire;
    }
    public void setHoraire(Disponibility horaire) {
        this.horaire = horaire;
    }
    public Disponibility getDisponibilite() {
        return horaire;  // Retourne l'objet horaire
    }

    @Override
    public String toString() {
        return "Moniteur{" +
                super.toString()+horaire.toString()+"vehicule=" + vehicule +"} ";
    }
}
