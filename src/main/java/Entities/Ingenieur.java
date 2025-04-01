package Entities;

import java.time.LocalDate;
import java.util.Date;

public class Ingenieur extends Personne{

    public Ingenieur(int id,int cin, String nom, String prenom, String adresse, String mail, int numTelephone, LocalDate dateNaissance) {
        super(id,cin, nom, prenom,  mail, numTelephone, dateNaissance);
    }
}
