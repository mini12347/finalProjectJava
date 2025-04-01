package Entities;

import java.time.LocalDate;
import java.util.Date;

public class Personne {
    protected int id;
    protected int CIN ;
    protected String nom ;
    protected String prenom ;

    protected String mail ;
    protected int numTelephone ;
    protected LocalDate dateNaissance ;
    public Personne( int id,int cin,String nom,String prenom,String mail,int numTelephone,LocalDate dateNaissance) {
        this.id = id;
        this.CIN = cin;
        this.nom = nom;
        this.prenom = prenom;
        this.mail = mail;
        this.numTelephone = numTelephone;
        this.dateNaissance = null;
    }
    public Personne() {};
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
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
    public LocalDate getDateNaissance() {
        return dateNaissance;
    }
    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    @Override
    public String   toString() {
        return " id" + id+
                ",CIN=" + CIN +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", mail='" + mail + '\'' +
                ", numTelephone=" + numTelephone +
                ", dateNaissance=" + dateNaissance +
                '}';
    }
}
