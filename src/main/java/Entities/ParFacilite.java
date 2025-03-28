package Entities;

import java.sql.Time;
import java.time.LocalDate;
import java.util.List;

public class ParFacilite extends Paiement {
    private Double accompte;
    private List<Double> montans;  // Note the typo correction from 'montants' to 'montans'

    public ParFacilite(int idPaiement, LocalDate date, Time time, int idClient,
                       String description, Double montant, String etat,
                       Double accompte, List<Double> montans) {
        super(idPaiement, date, time, idClient, description, montant, etat);
        this.accompte = accompte;
        this.montans = montans;
    }

    public Double getAccompte() {
        return accompte;
    }

    public void setAccompte(Double accompte) {
        this.accompte = accompte;
    }

    public List<Double> getMontans() {
        return montans;
    }

    public void setMontans(List<Double> montans) {
        this.montans = montans;
    }

    @Override
    public String toString() {
        return "ParFacilite{" +
                "accompte=" + accompte +
                ", montans=" + montans +
                ", " + super.toString() +
                '}';
    }
}