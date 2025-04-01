package Entities;

import java.time.LocalDate;
import java.util.Arrays;



public class Candidat extends Personne {
    private byte[] cinImage;


    public Candidat(int id, int CIN, String nom, String prenom, String mail, int numTelephone, LocalDate dateNaissance, byte[] cinImage) {

        super(id, CIN, nom, prenom,  mail, numTelephone,dateNaissance);
        this.cinImage = cinImage;
    }
    public Candidat() {
        // Constructeur vide
    }


    public byte[] getCinImage() {
        return cinImage;
    }

    public void setCinImage(byte[] cinImage) {
        this.cinImage = cinImage;
    }

    @Override
    public String toString() {
        return "Candidat{" + super.toString() + ", cinImage=" + (cinImage != null ? Arrays.toString(cinImage) : "no image") + "}";
    }
}
