package Entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Vehicule {
    private String matricule;
    private Date datem;
    private int kilometrage;
    private TypeP type;
    private List<Reparation> reparations;
    public Vehicule(String matricule, Date datem, int kilometrage, TypeP type) {
        this.matricule = matricule;
        this.datem = datem;
        this.kilometrage = kilometrage;
        this.type = type;
        this.reparations = new ArrayList<Reparation>();
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
    public List<Reparation> getReparations() {
        return reparations;
    }
    public void setReparations(List<Reparation> reparations) {
        this.reparations = reparations;
    }
    public String addReparation(Reparation reparation) {
        if(this.reparations.stream().filter(r->r.getId()==reparation.getId()).count()>0) {
            return "reparation deja existe";
        }else{
            this.reparations.add(reparation);
            return " reparation ajoutée avec succès";
        }

    }
    public void deleteReparation(Reparation reparation) {
        this.reparations.stream().filter(reparation::equals).forEach(reparations::remove);
    }
    public Reparation findReparation(int id) {
        return this.reparations.stream().filter(r -> r.getId() == id).findFirst().orElse(null);
    }
    public void replaceReparation(Reparation reparation) {
        reparations.set(this.reparations.indexOf(reparation), reparation);
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
