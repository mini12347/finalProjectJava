package Entities;

import java.sql.Time;
import java.time.LocalDate;
import java.util.List;

public class ParFacilite extends Paiement{
    private Double accompte;
     private List<Double> montants;
    public ParFacilite(int idPaiement, LocalDate date, Time time, int idClient, String description, Double montant,Double accompte, List<Double> montants) {
        super(idPaiement, date, time, idClient, description, montant);
        this.accompte = accompte;
        this.montants = montants;
    }
    public Double getAccompte() {
        return accompte;
    }
    public void setAccompte(Double accompte) {
        this.accompte = accompte;
    }
    public List<Double> getMontants() {
        return montants;
    }
    public void setMontants(List<Double> montants) {
        this.montants = montants;
    }

    @Override
    public String toString() {
        return "ParFacilite{" +
                "accompte=" + accompte +
                ", montants=" + montants + super.toString()+
                "} " ;
    }
}
