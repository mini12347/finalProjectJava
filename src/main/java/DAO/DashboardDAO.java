package DAO;

import Connection.ConxDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DashboardDAO {
    public int getTotalCandidats() throws SQLException {
        String query = "SELECT COUNT(*) FROM candidat";
        Connection conn = ConxDB.getInstance(); // Obtention de la connexion partagée

        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
        // Ne pas fermer la connexion ici, elle sera réutilisée
    }
    /**
     * Récupère le taux de réussite des examens de code
     */
    public double getTauxReussiteCode() throws SQLException {
        String query = "SELECT " +
                "COUNT(CASE WHEN resultat = 'SUCCES' THEN 1 END) as succes, " +
                "COUNT(*) as total " +
                "FROM examen_code " +
                "WHERE resultat IN ('SUCCES', 'ECHEC')";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                int succes = rs.getInt("succes");
                int total = rs.getInt("total");

                if (total > 0) {
                    return ((double) succes / total) * 100;
                }
            }
            return 0.0;
        }
    }

    /**
     * Récupère le taux de réussite des examens de conduite
     */
    public double getTauxReussiteConduite() throws SQLException {
        String query = "SELECT " +
                "COUNT(CASE WHEN resultat = 'SUCCES' THEN 1 END) as succes, " +
                "COUNT(*) as total " +
                "FROM examenconduite " +
                "WHERE resultat IN ('SUCCES', 'ECHEC')";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                int succes = rs.getInt("succes");
                int total = rs.getInt("total");

                if (total > 0) {
                    return ((double) succes / total) * 100;
                }
            }
            return 0.0;
        }
    }

    /**
     * Récupère les revenus mensuels (somme de tous les paiements du mois courant)
     */
    public double getRevenusMensuels() throws SQLException {
        String query = "SELECT SUM(montant) as total FROM paiement " +
                "WHERE MONTH(date) = MONTH(CURRENT_DATE()) " +
                "AND YEAR(date) = YEAR(CURRENT_DATE()) " +
                "AND etat = 'effectué'";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                Double total = rs.getDouble("total");
                return total != null ? total : 0.0;
            }
            return 0.0;
        }
    }

    /**
     * Récupère le nombre de moniteurs actifs
     */
    public int getNombreMoniteurs() throws SQLException {
        String query = "SELECT COUNT(*) FROM moniteur";
        try (Connection conn = ConxDB.getInstance();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    /**
     * Récupère le nombre de véhicules disponibles
     */
    public int getNombreVehicules() throws SQLException {
        String query = "SELECT COUNT(*) FROM vehicule";
        try (Connection conn = ConxDB.getInstance();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    /**
     * Récupère la distribution des candidats par type de permis
     */
    public Map<String, Integer> getDistributionTypePermis() throws SQLException {
        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("MOTO", 0);
        distribution.put("VOITURE", 0);
        distribution.put("CAMION", 0);

        // Cette requête compte le nombre de candidats participant à des séances par type de permis
        String query = "SELECT typePermis, COUNT(DISTINCT cin_candidat ) as total " +
                "FROM seance " +
                "GROUP BY typePermis";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String typePermis = rs.getString("typePermis");
                int total = rs.getInt("total");
                distribution.put(typePermis, total);
            }

            return distribution;
        }
    }
}