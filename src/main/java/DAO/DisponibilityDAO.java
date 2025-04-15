package DAO;

import Entities.Disponibility;
import Entities.Hours;

import java.sql.*;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import Connection.ConxDB;
public class DisponibilityDAO {


    // Ajouter les disponibilités d'un moniteur dans la base de données
    public void ajouterDisponibility(int cin, Disponibility disponibilite) throws SQLException {
        String sql = "INSERT INTO disponibility (cin, jour, start_hour, end_hour) VALUES (?, ?, ?, ?)";
        Connection conn = ConxDB.getInstance();
        PreparedStatement ps = conn.prepareStatement(sql);

        for (Map.Entry<DayOfWeek, Hours> entry : disponibilite.getDaysOfWeek().entrySet()) {
            DayOfWeek jour = entry.getKey();
            Hours horaire = entry.getValue();

            ps.setInt(1, cin);
            ps.setString(2, jour.name());
            ps.setInt(3, horaire.getStarthour());
            ps.setInt(4, horaire.getEndhour());
            ps.executeUpdate();
        }
    }

    // Chercher les disponibilités d'un moniteur par CIN
    public Disponibility chercherDisponibility(int cin) throws SQLException {
        String sql = "SELECT jour, start_hour, end_hour FROM disponibility WHERE cin = ?";
        Connection conn = ConxDB.getInstance();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, cin);
        ResultSet rs = ps.executeQuery();

        Map<DayOfWeek, Hours> disponibilites = new HashMap<>();

        while (rs.next()) {
            DayOfWeek jour = DayOfWeek.valueOf(rs.getString("jour"));
            int start = rs.getInt("start_hour");
            int end = rs.getInt("end_hour");

            Hours horaire = new Hours(start, end);
            disponibilites.put(jour, horaire);
        }

        return new Disponibility(cin, disponibilites); // Using cin as ID for now
    }

    // Supprimer les disponibilités d'un moniteur par CIN
    public void supprimerDisponibility(int cin) throws SQLException {
        String sql = "DELETE FROM disponibility WHERE cin = ?";
        Connection conn = ConxDB.getInstance();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, cin);
        ps.executeUpdate();
    }

    // Récupérer toutes les disponibilités pour tous les moniteurs et retourner une Liste
    public List<Disponibility> getAllDisponibilities() throws SQLException {
        List<Disponibility> disponibilitesList = new ArrayList<>();

        // Modified query to group by CIN for distinct moniteur disponibilities
        String sql = "SELECT DISTINCT cin FROM disponibility ORDER BY cin";
        try (Connection conn = ConxDB.getInstance();
                PreparedStatement psDistinct = conn.prepareStatement(sql);
             ResultSet rsDistinct = psDistinct.executeQuery()) {

            while (rsDistinct.next()) {
                int cin = rsDistinct.getInt("cin");
                Map<DayOfWeek, Hours> dayMap = new HashMap<>();

                // For each moniteur CIN, get all their availability entries
                String daysSql = "SELECT jour, start_hour, end_hour FROM disponibility WHERE cin = ?";
                try (PreparedStatement psDays = conn.prepareStatement(daysSql)) {
                    psDays.setInt(1, cin);
                    try (ResultSet rsDays = psDays.executeQuery()) {
                        while (rsDays.next()) {
                            String jourStr = rsDays.getString("jour");
                            DayOfWeek jour;

                            // Vérifier si le jour est déjà un enum DayOfWeek valide
                            try {
                                jour = DayOfWeek.valueOf(jourStr);
                            } catch (IllegalArgumentException e) {
                                // Si ce n'est pas un enum valide, essayer de convertir les noms français
                                switch (jourStr.toUpperCase()) {
                                    case "LUNDI": jour = DayOfWeek.MONDAY; break;
                                    case "MARDI": jour = DayOfWeek.TUESDAY; break;
                                    case "MERCREDI": jour = DayOfWeek.WEDNESDAY; break;
                                    case "JEUDI": jour = DayOfWeek.THURSDAY; break;
                                    case "VENDREDI": jour = DayOfWeek.FRIDAY; break;
                                    case "SAMEDI": jour = DayOfWeek.SATURDAY; break;
                                    case "DIMANCHE": jour = DayOfWeek.SUNDAY; break;
                                    default: throw new IllegalArgumentException("Jour invalide: " + jourStr);
                                }
                            }

                            int start = rsDays.getInt("start_hour");
                            int end = rsDays.getInt("end_hour");
                            Hours horaire = new Hours(start, end);
                            dayMap.put(jour, horaire);
                        }
                    }
                }

                // Create a single Disponibility object for each moniteur
                if (!dayMap.isEmpty()) {
                    disponibilitesList.add(new Disponibility(cin, dayMap));
                }
            }
        }

        return disponibilitesList;
    }
}