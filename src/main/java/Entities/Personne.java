package Entities;

import java.time.LocalDate;
import java.util.Date;

public class Personne {
    protected int CIN ;
    protected String nom ;
    protected String prenom ;
    protected String adresse ;
    protected String mail ;
    protected int numTelephone ;
    protected Date dateNaissance ;
    public Personne(int cin,String nom,String prenom,String adresse,String mail,int numTelephone,Date dateNaissance) {
        this.CIN = cin;
        this.nom = nom;
        this.prenom = prenom;
        this.adresse = adresse;
        this.mail = mail;
        this.numTelephone = numTelephone;
        this.dateNaissance = dateNaissance;
    }

    public Personne() {

    }

    public int getCIN() {
        return CIN;
    }
    public void setCIN(int CIN) {
        this.CIN = CIN;
    }
    public String getNom() {
        return nom;
    }
    public void setNom(String nom) {
        this.nom = nom;
    }
    public String getPrenom() {
        return prenom;
    }
    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }
    public String getAdresse() {
        return adresse;
    }
    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }
    public String getMail() {
        return mail;
    }
    public void setMail(String mail) {
        this.mail = mail;
    }
    public int getNumTelephone() {
        return numTelephone;
    }
    public void setNumTelephone(int numTelephone) {
        this.numTelephone = numTelephone;
    }
    public Date getDateNaissance() {
        return dateNaissance;
    }
    public void setDateNaissance(Date dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    @Override
    public String   toString() {
        return "CIN=" + CIN +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", adresse='" + adresse + '\'' +
                ", mail='" + mail + '\'' +
                ", numTelephone=" + numTelephone +
                ", dateNaissance=" + dateNaissance +
                '}';
    }
}