package Service;

import DAO.SeanceDAO;
import Entities.Seance;

import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class SeanceS {
    private SeanceDAO dao;

    public SeanceS() {
        dao = new SeanceDAO();
    }

    public boolean ajouterSeance(Seance seance) {
        return dao.ajouterSeance(seance);
    }

    public List<Seance> getAllSeances() {
        return dao.getAllSeances();
    }

    public boolean supprimerSeance(Seance s) {
        return dao.supprimerSeance(s);
    }

    public boolean modifierSeance(Seance seance) {
        return dao.modifierSeance(seance);
    }
    public boolean ValidateSeassion(int idMoniteur, LocalDate date, Time heure) {
        return dao.hasSeanceByMoniteurAndDateTime(idMoniteur, date, heure);
    }
    public List<Integer>getSalles() {
        return Arrays.asList(100,101,102,103,104,105,106,107,108,109,110);
    }
    public List<Seance> getSeanceThisWeek(){
        return dao.getSeanceThisWeek();
    }
    public boolean hasCandidateConflict(int cinCandidat, LocalDate date, Time time){
        return dao.hasCandidateConflict(cinCandidat,date,time);
    }
    public boolean isDuplicateSession(int cinMoniteur, int cinCandidat, LocalDate date, Time time){
        return dao.isDuplicateSession(cinMoniteur,cinCandidat,date,time);
    }
    public boolean isSalleFull(int salleNumber, LocalDate date, Time time)  {
        return dao.isSalleFull(salleNumber,date,time);
    }
}
