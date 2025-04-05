package Service;

import Entities.Candidat;
import java.sql.SQLException;
import java.util.List;
import DAO.CandidateDAO;

public class CandidateService {
    private final CandidateDAO repository = new CandidateDAO();

    /**
     * Sauvegarde un nouveau candidat
     * @param candidate Le candidat à sauvegarder
     * @return Le CIN du candidat sauvegardé
     * @throws SQLException En cas d'erreur de base de données
     */
    public int saveCandidate(Candidat candidate) throws SQLException {
        return repository.save(candidate);
    }

    /**
     * Met à jour les informations d'un candidat existant
     * @param candidate Le candidat avec les informations mises à jour
     * @throws SQLException En cas d'erreur de base de données
     */
    public void updateCandidate(Candidat candidate) throws SQLException {
        repository.update(candidate);
    }

    /**
     * Récupère tous les candidats
     * @return Liste de tous les candidats
     * @throws SQLException En cas d'erreur de base de données
     */
    public List<Candidat> getAllCandidates() throws SQLException {
        return repository.findAll();
    }

    /**
     * Supprime un candidat par son CIN
     * @param cin Le CIN du candidat à supprimer
     * @throws SQLException En cas d'erreur de base de données
     */
    public void deleteCandidate(int cin) throws SQLException {
        repository.delete(cin);
    }

    /**
     * Recherche des candidats par mot-clé
     * @param keyword Le mot-clé de recherche
     * @return Liste des candidats correspondant au critère de recherche
     * @throws SQLException En cas d'erreur de base de données
     */
    public List<Candidat> searchCandidates(String keyword) throws SQLException {
        return repository.search(keyword);
    }

    /**
     * Vérifie si un candidat avec ce CIN existe déjà
     * @param cin Le CIN à vérifier
     * @return true si un candidat avec ce CIN existe, false sinon
     * @throws SQLException En cas d'erreur de base de données
     */
    public boolean candidateExists(int cin) throws SQLException {
        return repository.cinExists(cin);
    }

    /**
     * Récupère un candidat par son CIN
     * @param cin Le CIN du candidat à récupérer
     * @return Le candidat ou null s'il n'existe pas
     * @throws SQLException En cas d'erreur de base de données
     */
    public Candidat getCandidateByCin(int cin) throws SQLException {
        return repository.findByCin(cin);
    }
}