package Service;

import Entities.ExamenCode;
import Entities.Res;
import DAO.ExamenCodeDAO;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class ExamenCodeService {

    private ExamenCodeDAO examenCodeDAO;
    private PaiementService paiementService;

    public ExamenCodeService() throws SQLException {
        this.examenCodeDAO = new ExamenCodeDAO();
        this.paiementService = new PaiementService();
    }

    public boolean addExamen(ExamenCode examen, boolean createPayment, boolean parFacilite) {
        // Validation des données si nécessaire
        if (examen.getDate() == null || examen.getTime() == null ||
                examen.getCandidat() == null || examen.getType() == null ||
                examen.getResultat() == null || examen.getCout() <= 0) {
            return false;
        }

        // Vérification que le résultat est l'une des valeurs autorisées
        if (examen.getResultat() != Res.SUCCES &&
                examen.getResultat() != Res.ECHEC &&
                examen.getResultat() != Res.EnATTENTE) {
            return false;
        }

        // Insertion de l'examen
        boolean examenInserted = examenCodeDAO.insert(examen);

        // Si l'insertion de l'examen a réussi et qu'on veut créer un paiement
        if (examenInserted && createPayment) {
            // Créer un paiement lié à cet examen
            return paiementService.createPaiementFromExamen(examen, parFacilite);
        }

        return examenInserted;
    }

    // Pour maintenir la compatibilité avec le code existant
    public boolean addExamen(ExamenCode examen) {
        // Par défaut, on crée un paiement sans facilité
        return addExamen(examen, true, false);
    }
    // Méthode pour obtenir les CINs des candidats qui ont déjà réussi l'examen
    public List<Integer> getSuccessfulCandidateCins() {
        List<ExamenCode> successExamens = getExamensByResult(Res.SUCCES);
        return successExamens.stream()
                .map(examen -> examen.getCandidat().getCIN())
                .distinct()
                .collect(Collectors.toList());
    }

    public boolean updateExamen(ExamenCode examen) {
        // Validation des données
        if (examen.getId() <= 0 || examen.getDate() == null || examen.getTime() == null ||
                examen.getCandidat() == null || examen.getType() == null ||
                examen.getResultat() == null || examen.getCout() <= 0) {
            return false;
        }

        // Vérification que le résultat est l'une des valeurs autorisées
        if (examen.getResultat() != Res.SUCCES &&
                examen.getResultat() != Res.ECHEC &&
                examen.getResultat() != Res.EnATTENTE) {
            return false;
        }

        return examenCodeDAO.update(examen);
    }

    public boolean deleteExamen(int id) {
        if (id <= 0) {
            return false;
        }
        return examenCodeDAO.delete(id);
    }

    public ExamenCode getExamenById(int id) {
        if (id <= 0) {
            return null;
        }
        return examenCodeDAO.getById(id);
    }

    public List<ExamenCode> getAllExamens() {
        return examenCodeDAO.getAll();
    }

    public List<ExamenCode> getExamensByCandidatCin(Integer cin) {
        if (cin == null) {
            return List.of();
        }
        return examenCodeDAO.getByCandidatCin(String.valueOf(cin));
    }

    // Autres méthodes métier selon les besoins
    public List<ExamenCode> getExamensByTypePermis(String typePermis) {
        if (typePermis == null || typePermis.isEmpty()) {
            return List.of();
        }
        return examenCodeDAO.getByTypePermis(typePermis);
    }

    public double getSuccessRateByCandidatCin(String cin) {
        if (cin == null || cin.isEmpty()) {
            return 0.0;
        }
        List<ExamenCode> examens = examenCodeDAO.getByCandidatCin(cin);
        if (examens.isEmpty()) {
            return 0.0;
        }

        long countSuccess = examens.stream()
                .filter(e -> e.getResultat() == Res.SUCCES)
                .count();

        return (double) countSuccess / examens.size() * 100;
    }

    // Méthode supplémentaire pour obtenir les examens selon leur résultat
    public List<ExamenCode> getExamensByResult(Res resultat) {
        if (resultat == null) {
            return List.of();
        }
        return examenCodeDAO.getByResult(resultat);
    }

    // Méthode pour obtenir le nombre d'examens en attente
    public int getCountExamensEnAttente() {
        return examenCodeDAO.getByResult(Res.EnATTENTE).size();
    }

    // Méthode pour obtenir le taux de réussite global
    public double getGlobalSuccessRate() {
        List<ExamenCode> allExamens = examenCodeDAO.getAll();
        if (allExamens.isEmpty()) {
            return 0.0;
        }

        // On ne considère que les examens qui ont un résultat définitif (pas EN_ATTENTE)
        List<ExamenCode> completedExamens = allExamens.stream()
                .filter(e -> e.getResultat() != Res.EnATTENTE)
                .toList();

        if (completedExamens.isEmpty()) {
            return 0.0;
        }

        long countSuccess = completedExamens.stream()
                .filter(e -> e.getResultat() == Res.SUCCES)
                .count();

        return (double) countSuccess / completedExamens.size() * 100;
    }

    // Nouvelles méthodes pour gérer le coût

    // Méthode pour obtenir le coût moyen des examens
    public double getAverageCost() {
        List<ExamenCode> allExamens = examenCodeDAO.getAll();
        if (allExamens.isEmpty()) {
            return 0.0;
        }

        double totalCost = allExamens.stream()
                .mapToDouble(ExamenCode::getCout)
                .sum();

        return totalCost / allExamens.size();
    }

    // Méthode pour obtenir le coût total des examens pour un candidat
    public double getTotalCostByCandidatCin(String cin) {
        if (cin == null || cin.isEmpty()) {
            return 0.0;
        }

        List<ExamenCode> examens = examenCodeDAO.getByCandidatCin(cin);
        if (examens.isEmpty()) {
            return 0.0;
        }

        return examens.stream()
                .mapToDouble(ExamenCode::getCout)
                .sum();
    }

    // Méthode pour obtenir les examens dont le coût est supérieur à un certain montant
    public List<ExamenCode> getExamensByCostGreaterThan(double cost) {
        if (cost < 0) {
            return List.of();
        }

        List<ExamenCode> allExamens = examenCodeDAO.getAll();
        return allExamens.stream()
                .filter(e -> e.getCout() > cost)
                .toList();
    }
}