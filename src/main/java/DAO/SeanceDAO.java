package DAO;

import Connection.ConxDB;
import Entities.Seance;
import Entities.SeanceCode;
import Entities.SeanceConduite;
import Entities.TypeP;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SeanceDAO {
    private static final Logger LOGGER = Logger.getLogger(SeanceDAO.class.getName());


    public boolean ajouterSeance(Seance s) {
        String sql = s instanceof SeanceCode ?
                "INSERT INTO seance_code (date, heure, type_permis, num_salle, moniteur_cin, candidat_cin) VALUES (?, ?, ?, ?, ?, ?)" :
                "INSERT INTO seance_conduite (date, heure, type_permis, localisation, moniteur_cin, candidat_cin) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(s.getDate()));
            stmt.setTime(2, s.getTime());
            stmt.setString(3, s.getTypePermis().toString());

            if (s instanceof SeanceCode) {
                stmt.setInt(4, ((SeanceCode) s).getNumSalle());
            } else {
                stmt.setString(4, ((SeanceConduite) s).getLocalisation());
            }

            stmt.setInt(5, s.getIdMoniteur());
            stmt.setInt(6, s.getIdCandidat());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding session", e);
            return false;
        }
    }

    public boolean modifierSeance(Seance s) {
        String sql = s instanceof SeanceCode ?
                "UPDATE seance_code SET date=?, heure=?, type_permis=?, num_salle=?, moniteur_cin=?, candidat_cin=? WHERE id=?" :
                "UPDATE seance_conduite SET date=?, heure=?, type_permis=?, localisation=?, moniteur_cin=?, candidat_cin=? WHERE id=?";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(s.getDate()));
            stmt.setTime(2, s.getTime());
            stmt.setString(3, s.getTypePermis().toString());

            if (s instanceof SeanceCode) {
                stmt.setInt(4, ((SeanceCode) s).getNumSalle());
            } else {
                stmt.setString(4, ((SeanceConduite) s).getLocalisation());
            }

            stmt.setInt(5, s.getIdMoniteur());
            stmt.setInt(6, s.getIdCandidat());
            stmt.setInt(7, s.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating session", e);
            return false;
        }
    }

    public boolean supprimerSeance(Seance s) {
        String sql = s instanceof SeanceCode ?
                "DELETE FROM seance_code WHERE id=?" :
                "DELETE FROM seance_conduite WHERE id=?";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, s.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting session", e);
            return false;
        }
    }

    public List<Seance> getAllSeances() {
        List<Seance> sessions = new ArrayList<>();

        try (Connection conn = ConxDB.getInstance()) {
            // Get code sessions
            String sqlCode = "SELECT * FROM seance_code";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlCode)) {
                while (rs.next()) {
                    sessions.add(new SeanceCode(
                            rs.getInt("id"),
                            rs.getDate("date").toLocalDate(),
                            rs.getTime("heure"),
                            TypeP.valueOf(rs.getString("type_permis")),
                            rs.getInt("num_salle"),
                            rs.getInt("moniteur_cin"),
                            rs.getInt("candidat_cin")
                    ));
                }
            }

            // Get driving sessions
            String sqlConduite = "SELECT * FROM seance_conduite";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlConduite)) {
                while (rs.next()) {
                    sessions.add(new SeanceConduite(
                            rs.getInt("id"),
                            rs.getDate("date").toLocalDate(),
                            rs.getTime("heure"),
                            rs.getString("localisation"),
                            TypeP.valueOf(rs.getString("type_permis")),
                            rs.getInt("moniteur_cin"),
                            rs.getInt("candidat_cin")
                    ));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving all sessions", e);
        }

        return sessions;
    }

    public boolean hasSeanceByMoniteurAndDateTime(int idMoniteur, LocalDate date, Time heure) {
        String sql = "SELECT COUNT(*) FROM seance_code WHERE moniteur_cin = ? AND date = ? AND heure = ? " +
                "UNION ALL " +
                "SELECT COUNT(*) FROM seance_conduite WHERE moniteur_cin = ? AND date = ? AND heure = ?";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idMoniteur);
            stmt.setDate(2, Date.valueOf(date));
            stmt.setTime(3, heure);
            stmt.setInt(4, idMoniteur);
            stmt.setDate(5, Date.valueOf(date));
            stmt.setTime(6, heure);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    if (rs.getInt(1) > 0) return true;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking session conflict", e);
        }
        return false;
    }

    public List<Integer> getSalles() {
        List<Integer> salles = new ArrayList<>();
        String sql = "SELECT DISTINCT num_salle FROM seance_code";

        try (Connection conn = ConxDB.getInstance();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                salles.add(rs.getInt("num_salle"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving rooms", e);
        }
        return salles;
    }

    public List<Seance> getSeanceThisWeek() {
        List<Seance> sessions = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(java.time.DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(java.time.DayOfWeek.SUNDAY);

        try (Connection conn = ConxDB.getInstance()) {
            // Code sessions
            String queryCode = "SELECT * FROM seance_code WHERE date BETWEEN ? AND ?";
            try (PreparedStatement stmt = conn.prepareStatement(queryCode)) {
                stmt.setDate(1, Date.valueOf(startOfWeek));
                stmt.setDate(2, Date.valueOf(endOfWeek));

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        sessions.add(new SeanceCode(
                                rs.getInt("id"),
                                rs.getDate("date").toLocalDate(),
                                rs.getTime("heure"),
                                TypeP.valueOf(rs.getString("type_permis")),
                                rs.getInt("num_salle"),
                                rs.getInt("moniteur_cin"),
                                rs.getInt("candidat_cin")
                        ));
                    }
                }
            }

            // Driving sessions
            String queryConduite = "SELECT * FROM seance_conduite WHERE date BETWEEN ? AND ?";
            try (PreparedStatement stmt = conn.prepareStatement(queryConduite)) {
                stmt.setDate(1, Date.valueOf(startOfWeek));
                stmt.setDate(2, Date.valueOf(endOfWeek));

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        sessions.add(new SeanceConduite(
                                rs.getInt("id"),
                                rs.getDate("date").toLocalDate(),
                                rs.getTime("heure"),
                                rs.getString("localisation"),
                                TypeP.valueOf(rs.getString("type_permis")),
                                rs.getInt("moniteur_cin"),
                                rs.getInt("candidat_cin")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving this week's sessions", e);
        }
        return sessions;
    }

    public List<Seance> getSessionsForDate(LocalDate date) {
        List<Seance> sessions = new ArrayList<>();

        try (Connection conn = ConxDB.getInstance()) {
            // Code sessions
            String queryCode = "SELECT * FROM seance_code WHERE date = ?";
            try (PreparedStatement stmt = conn.prepareStatement(queryCode)) {
                stmt.setDate(1, Date.valueOf(date));

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        sessions.add(new SeanceCode(
                                rs.getInt("id"),
                                rs.getDate("date").toLocalDate(),
                                rs.getTime("heure"),
                                TypeP.valueOf(rs.getString("type_permis")),
                                rs.getInt("num_salle"),
                                rs.getInt("moniteur_cin"),
                                rs.getInt("candidat_cin")
                        ));
                    }
                }
            }

            // Driving sessions
            String queryConduite = "SELECT * FROM seance_conduite WHERE date = ?";
            try (PreparedStatement stmt = conn.prepareStatement(queryConduite)) {
                stmt.setDate(1, Date.valueOf(date));

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        sessions.add(new SeanceConduite(
                                rs.getInt("id"),
                                rs.getDate("date").toLocalDate(),
                                rs.getTime("heure"),
                                rs.getString("localisation"),
                                TypeP.valueOf(rs.getString("type_permis")),
                                rs.getInt("moniteur_cin"),
                                rs.getInt("candidat_cin")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving sessions for date", e);
        }
        return sessions;
    }

    public boolean hasCandidateConflict(int cinCandidat, LocalDate date, Time time) {
        String sql = "SELECT COUNT(*) FROM seance_code WHERE candidat_cin = ? AND date = ? AND heure = ? " +
                "UNION ALL " +
                "SELECT COUNT(*) FROM seance_conduite WHERE candidat_cin = ? AND date = ? AND heure = ?";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, cinCandidat);
            stmt.setDate(2, Date.valueOf(date));
            stmt.setTime(3, time);
            stmt.setInt(4, cinCandidat);
            stmt.setDate(5, Date.valueOf(date));
            stmt.setTime(6, time);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    if (rs.getInt(1) > 0) return true;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking candidate conflict", e);
        }
        return false;
    }

    public boolean isDuplicateSession(int cinMoniteur, int cinCandidat, LocalDate date, Time time) {
        String sql = "SELECT COUNT(*) FROM seance_code WHERE moniteur_cin = ? AND candidat_cin = ? AND date = ? AND heure = ? " +
                "UNION ALL " +
                "SELECT COUNT(*) FROM seance_conduite WHERE moniteur_cin = ? AND candidat_cin = ? AND date = ? AND heure = ?";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, cinMoniteur);
            stmt.setInt(2, cinCandidat);
            stmt.setDate(3, Date.valueOf(date));
            stmt.setTime(4, time);
            stmt.setInt(5, cinMoniteur);
            stmt.setInt(6, cinCandidat);
            stmt.setDate(7, Date.valueOf(date));
            stmt.setTime(8, time);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    if (rs.getInt(1) > 0) return true;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking duplicate session", e);
        }
        return false;
    }

    public boolean isSalleFull(int salleNumber, LocalDate date, Time time) {
        String sql = "SELECT COUNT(*) FROM seance_code WHERE num_salle = ? AND date = ? AND heure = ?";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, salleNumber);
            stmt.setDate(2, Date.valueOf(date));
            stmt.setTime(3, time);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) >= 10;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking room capacity", e);
        }
        return false;
    }
}