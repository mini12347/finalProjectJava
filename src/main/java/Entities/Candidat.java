package Entities;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;


public class Candidat extends Personne {
    private byte[] cinImage;
    private int id;


    public Candidat(int id,int CIN, String nom, String prenom, String mail, int numTelephone, Date dateNaissance, byte[] cinImage) {

        super(CIN, nom,prenom,"",mail, numTelephone,dateNaissance);
        this.cinImage = cinImage;
        this.id = id;
    }
    public Candidat() {
        super();
    }


    public byte[] getCinImage() {
        return cinImage;
    }

    public void setCinImage(byte[] cinImage) {
        this.cinImage = cinImage;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Candidat{" + super.toString() + ", cinImage=" + (cinImage != null ? Arrays.toString(cinImage) : "no image") + "}";
    }

}
