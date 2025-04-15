package Service;

import DAO.PaiementDAO;
import Entities.Examen;
import Entities.Paiement;
import Entities.Reparation;

import java.sql.SQLException;
import java.util.List;

public class PaiementService {
    private PaiementDAO paiementDAO;

    // Mot de passe pour sécuriser les paiements
    // Dans une application réelle, ce mot de passe devrait être stocké de manière sécurisée
    // et haché dans la base de données, pas en texte brut dans le code source
    private static final String SECRETAIRE_PASSWORD = "admin123";

    public PaiementService() throws SQLException {
        this.paiementDAO = new PaiementDAO();
    }

    public List<Paiement> getPaiementsByCIN(int cin) {
        return paiementDAO.getPaiementsByCIN(cin);
    }

    /**
     * Effectue un paiement après vérification du mot de passe
     * @param paiement Le paiement à effectuer
     * @param parFacilite Indique si c'est un paiement par facilité
     * @param password Le mot de passe saisi
     * @return true si le paiement est effectué avec succès, false sinon
     */
    public boolean effectuerPaiement(Paiement paiement, boolean parFacilite, String password) {
        // Vérifier le mot de passe avant d'effectuer le paiement
        if (!verifierMotDePasse(password)) {
            return false;
        }

        // Si le mot de passe est correct, procéder au paiement
        return paiementDAO.effectuerPaiement(paiement, parFacilite);
    }

    public boolean initiateParFacilitePayment(Paiement paiement) {
        // Additional business logic validation can be added here if needed
        // For example, checking payment eligibility, additional constraints, etc.
        return paiementDAO.initiateParFacilitePayment(paiement);
    }

    /**
     * Vérifie si le mot de passe saisi correspond au mot de passe de la secrétaire
     * @param password Le mot de passe à vérifier
     * @return true si le mot de passe est correct, false sinon
     */
    public boolean verifierMotDePasse(String password) {
        return SECRETAIRE_PASSWORD.equals(password);
    }

    public boolean createPaiementFromExamen(Object examen, boolean parFacilite) {
        return createPaiementFromExamen((Object) examen, parFacilite);
    }
    public void ajouterPaiement(Paiement paiement) throws SQLException {
        paiementDAO.insertPaiement(paiement);
    }
}