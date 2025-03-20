package Entities;

import java.util.Date;
import java.util.Map;

public class Candidat extends Personne{
    private Map<String,String> dossier;
    public Candidat(int cin, String nom, String prenom, String email, String adresse, int nume, Date date, Map<String,String> dossier){
        super(cin,nom,prenom,adresse,email,nume,date);
        this.dossier = dossier;
    }
    public Map<String,String> getDossier(){
        return dossier;
    }
    public void setDossier(Map<String,String> dossier){
        this.dossier = dossier;
    }

    @Override
    public String toString() {
        return "candidat{" +super.toString() +"dossier=" + dossier + '}';
    }
}
