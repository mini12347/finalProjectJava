package Entities;

public class AutoEcole {
    private String nom;
    private int numtel;
    private String email;
    private String adresse;
    private Disponibility horaire;
    public AutoEcole(String nom, int numtel, String email, String adresse,Disponibility horaire) {
        this.nom = nom;
        this.numtel = numtel;
        this.email = email;
        this.adresse = adresse;
        this.horaire = horaire;
    }
    public String getNom() {
        return nom;
    }
    public void setNom(String nom) {
        this.nom = nom;
    }
    public int getNumtel() {
        return numtel;
    }
    public void setNumtel(int numtel) {
        this.numtel = numtel;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getAdresse() {
        return adresse;
    }
    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }
    public Disponibility getHoraire() {
        return horaire;
    }
    public void setHoraire(Disponibility horaire) {
        this.horaire = horaire;
    }
    @Override
    public String toString() {
        return "AutoEcole{" +
                "nom='" + nom + '\'' +
                ", numtel=" + numtel +
                ", email='" + email + '\'' +
                ", adresse='" + adresse + '\'' +
                ", horaire=" + horaire +
                '}';
    }
}
