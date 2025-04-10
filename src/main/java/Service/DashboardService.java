package Service;

import DAO.DashboardDAO;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Map;

/**
 * Service pour les fonctionnalités du tableau de bord
 */
public class DashboardService {
    private DashboardDAO dashboardDAO;

    public DashboardService() {
        this.dashboardDAO = new DashboardDAO();
    }

    /**
     * Récupère le nombre total de candidats
     */
    public String getTotalCandidats() {
        try {
            int total = dashboardDAO.getTotalCandidats();
            return String.valueOf(total);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du nombre de candidats: " + e.getMessage());
            return "0";
        }
    }

    /**
     * Récupère le taux de réussite des examens de code formaté
     */
    public String getTauxReussiteCode() {
        try {
            double taux = dashboardDAO.getTauxReussiteCode();
            DecimalFormat df = new DecimalFormat("#.#");
            return df.format(taux) + "%";
        } catch (SQLException e) {
            System.err.println("Erreur lors du calcul du taux de réussite code: " + e.getMessage());
            return "0%";
        }
    }

    /**
     * Récupère le taux de réussite des examens de conduite formaté
     */
    public String getTauxReussiteConduite() {
        try {
            double taux = dashboardDAO.getTauxReussiteConduite();
            DecimalFormat df = new DecimalFormat("#.#");
            return df.format(taux) + "%";
        } catch (SQLException e) {
            System.err.println("Erreur lors du calcul du taux de réussite conduite: " + e.getMessage());
            return "0%";
        }
    }

    /**
     * Récupère les revenus mensuels formatés
     */
    public String getRevenusMensuels() {
        try {
            double revenus = dashboardDAO.getRevenusMensuels();
            DecimalFormat df = new DecimalFormat("#,##0.00");
            return df.format(revenus) + " DT";
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des revenus: " + e.getMessage());
            return "0.00 DT";
        }
    }

    /**
     * Récupère le nombre de moniteurs
     */
    public String getNombreMoniteurs() {
        try {
            int total = dashboardDAO.getNombreMoniteurs();
            return String.valueOf(total);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du nombre de moniteurs: " + e.getMessage());
            return "0";
        }
    }

    /**
     * Récupère le nombre de véhicules
     */
    public String getNombreVehicules() {
        try {
            int total = dashboardDAO.getNombreVehicules();
            return String.valueOf(total);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du nombre de véhicules: " + e.getMessage());
            return "0";
        }
    }

    /**
     * Récupère le nombre de candidats pour le permis moto
     */
    public String getCandidatsTypeA() {
        try {
            Map<String, Integer> distribution = dashboardDAO.getDistributionTypePermis();
            return String.valueOf(distribution.getOrDefault("MOTO", 0));
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la distribution par type: " + e.getMessage());
            return "0";
        }
    }

    /**
     * Récupère le nombre de candidats pour le permis voiture
     */
    public String getCandidatsTypeB() {
        try {
            Map<String, Integer> distribution = dashboardDAO.getDistributionTypePermis();
            return String.valueOf(distribution.getOrDefault("VOITURE", 0));
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la distribution par type: " + e.getMessage());
            return "0";
        }
    }

    /**
     * Récupère le nombre de candidats pour le permis camion
     */
    public String getCandidatsTypeC() {
        try {
            Map<String, Integer> distribution = dashboardDAO.getDistributionTypePermis();
            return String.valueOf(distribution.getOrDefault("CAMION", 0));
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la distribution par type: " + e.getMessage());
            return "0";
        }
    }
}