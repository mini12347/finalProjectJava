package Entities;

import java.sql.Time;
import java.time.LocalDate;

public class ExamenCode extends Examen<Integer>{
    public ExamenCode(LocalDate date, Time time, Candidat candidat, Integer resultat,TypeP t) {
        super(date, time, candidat, resultat,t);
    }
}
