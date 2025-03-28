package Service;

import DAO.PaiementDAO;
import Entities.Paiement;

import java.util.List;

public class PaiementService {
    private PaiementDAO paiementDAO;

    public PaiementService() {
        this.paiementDAO = new PaiementDAO();
    }

    public List<Paiement> getPaiementsByCIN(int cin) {
        return paiementDAO.getPaiementsByCIN(cin);
    }

    public boolean effectuerPaiement(Paiement paiement, boolean parFacilite) {
        // Additional business logic can be added here if needed
        return paiementDAO.effectuerPaiement(paiement, parFacilite);
    }

    public boolean initiateParFacilitePayment(Paiement paiement) {
        // Additional business logic validation can be added here if needed
        // For example, checking payment eligibility, additional constraints, etc.
        return paiementDAO.initiateParFacilitePayment(paiement);
    }
}