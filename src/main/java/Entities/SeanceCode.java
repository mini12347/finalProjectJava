package Entities;

import java.sql.Time;
import java.time.LocalDate;

public class SeanceCode extends Seance {
    private int numSalle;

    public SeanceCode(int id, LocalDate date, Time time,TypeP t , int numSalle,Moniteur moniteur) {
        super(id, date, time,t,moniteur);
        this.numSalle = numSalle;

    }
    public int getNumSalle() {
        return numSalle;
    }
    public void setNumSalle(int numSalle) {
        this.numSalle = numSalle;
    }

    @Override
    public String toString() {
        return "SeanceCode{" +
                "numSalle=" + numSalle + super.toString()+
                "} " ;
    }
}
