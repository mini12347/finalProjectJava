package DAO;

import Entities.Disponibility;
import Entities.Hours;

import java.sql.*;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisponibilityDAO {
    private Connection conn;

    public DisponibilityDAO(Connection conn) {
        this.conn = conn;
    }

    // Ajouter les disponibilités d'un moniteur dans la base de données
    public void ajouterDisponibility(int cin, Disponibility disponibilite) throws SQLException {
        String sql = "INSERT INTO disponibility (cin, jour, start_hour, end_hour) VALUES (?, ?, ?, ?)";
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

        return new Disponibility(disponibilites);
    }

    // Supprimer les disponibilités d'un moniteur par CIN
    public void supprimerDisponibility(int cin) throws SQLException {
        String sql = "DELETE FROM disponibility WHERE cin = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, cin);
        ps.executeUpdate();
    }

    // Récupérer toutes les disponibilités pour tous les moniteurs et retourner une Liste
    public List<Disponibility> getAllDisponibilities() throws SQLException {
        List<Disponibility> disponibilitesList = new ArrayList<>();
        String sql = "SELECT cin, jour, start_hour, end_hour FROM disponibility";
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        Map<Integer, Disponibility> disponibilitesMap = new HashMap<>();

        while (rs.next()) {
            int cin = rs.getInt("cin");
            String jourStr = rs.getString("jour");
            DayOfWeek jour;

            // Vérifier si le jour est déjà un enum DayOfWeek valide
            try {
                jour = DayOfWeek.valueOf(jourStr);
            } catch (IllegalArgumentException e) {
                // Si ce n'est pas un enum valide, essayer de convertir les noms français
                switch(jourStr.toUpperCase()) {
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

            int start = rs.getInt("start_hour");
            int end = rs.getInt("end_hour");

            Hours horaire = new Hours(start, end);

            if (!disponibilitesMap.containsKey(cin)) {
                disponibilitesMap.put(cin, new Disponibility(new HashMap<>()));
            }
            disponibilitesMap.get(cin).getDaysOfWeek().put(jour, horaire);
        }

        // Convert Map into List
        for (Disponibility disponibility : disponibilitesMap.values()) {
            disponibilitesList.add(disponibility);
        }

        return disponibilitesList;
    }
}