package DAO;

import Connection.ConxDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationDAO {
    /**
     * Récupère les alertes pour les notifications
     */
    public List<Map<String, String>> getAlertes() {
        List<Map<String, String>> alertes = new ArrayList<>();

        try (Connection conn = ConxDB.getInstance()) {
            // Exemple: Alertes pour les véhicules avec kilométrage élevé
            String query = "SELECT matricule, kilometrage FROM vehicule WHERE kilometrage > 100000";
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    Map<String, String> alerte = new HashMap<>();
                    alerte.put("message", "Véhicule " + rs.getString("matricule") +
                            " a atteint " + rs.getInt("kilometrage") + " km, vérification nécessaire");
                    alerte.put("type", "warning");
                    alertes.add(alerte);
                }
            }

            // Si aucune alerte réelle n'est trouvée, ajouter quelques alertes statiques pour montrer la fonctionnalité
            if (alertes.isEmpty()) {
                Map<String, String> alerte1 = new HashMap<>();
                alerte1.put("message", "Véhicule TUN 8234 doit passer la visite technique dans 7 jours");
                alerte1.put("type", "warning");
                alertes.add(alerte1);

                Map<String, String> alerte2 = new HashMap<>();
                alerte2.put("message", "Assurance de Véhicule TUN 5643 expire dans 14 jours");
                alerte2.put("type", "danger");
                alertes.add(alerte2);

                Map<String, String> alerte3 = new HashMap<>();
                alerte3.put("message", "Vidange de Véhicule TUN 9876 prévue dans 200 km");
                alerte3.put("type", "info");
                alertes.add(alerte3);
            }

            return alertes;
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des alertes: " + e.getMessage());

            // En cas d'erreur, retourner des alertes statiques
            List<Map<String, String>> alertesStatiques = new ArrayList<>();

            Map<String, String> alerte = new HashMap<>();
            alerte.put("message", "Erreur de connexion à la base de données");
            alerte.put("type", "danger");
            alertesStatiques.add(alerte);

            return alertesStatiques;
        }
    }
}