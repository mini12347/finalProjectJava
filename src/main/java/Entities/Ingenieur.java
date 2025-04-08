package Entities;

import java.time.LocalDate;
import java.util.Date;

public class Ingenieur extends Personne{

    public Ingenieur(int id,String cin, String nom, String prenom, String adresse, String mail, int numTelephone, Date dateNaissance) {
        super(id,cin, nom, prenom,  mail, numTelephone, dateNaissance);
    }
}