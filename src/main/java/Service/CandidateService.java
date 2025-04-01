package Service;

import Entities.Candidat;

import java.sql.SQLException;
import java.util.List;

import DAO.CandidateDAO;

public class CandidateService {
    private final CandidateDAO repository = new CandidateDAO ();

    // Méthode pour sauvegarder un candidat
    public int saveCandidate(Candidat candidate) throws SQLException {
        return  repository.save(candidate);
    }

    // Méthode pour récupérer tous les candidats
    public List<Candidat> getAllCandidates() throws SQLException {
        return repository.findAll();
    }

    // Méthode pour supprimer un candidat par son ID
    public void deleteCandidate(int id) throws SQLException {
        repository.delete(id);
    }

    // Méthode pour rechercher des candidats par mot-clé
    public List<Candidat> searchCandidates(String keyword) throws SQLException {
        return repository.search(keyword);
    }

    public void updateCandidate(Candidat candidate) throws SQLException {
        repository.update(candidate);
    }
}



