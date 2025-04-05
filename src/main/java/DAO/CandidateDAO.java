package DAO;

import Entities.Candidat;
import java.sql.Connection;
import Connection.ConxDB;
import java.sql.*;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class CandidateDAO {
    private Connection connection;

    public CandidateDAO() {
        connection = ConxDB.getInstance();
    }

    public int save(Candidat candidate) throws SQLException {
        // Check if CIN already exists
        if (cinExists(candidate.getCIN())) {
            throw new SQLException("Un candidat avec ce CIN existe déjà");
        }

        // First insert into personne table
        String personneSQL = "INSERT INTO personne (cin, nom, prenom, adresse, mail, num_telephone, date_naissance) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(personneSQL)) {
            stmt.setInt(1, candidate.getCIN());
            stmt.setString(2, candidate.getNom());
            stmt.setString(3, candidate.getPrenom());
            stmt.setString(4, candidate.getAdresse() != null ? candidate.getAdresse() : "");
            stmt.setString(5, candidate.getMail());
            stmt.setInt(6, candidate.getNumTelephone());

            // Convertir java.util.Date en java.sql.Date
            if (candidate.getDateNaissance() != null) {
                java.sql.Date sqlDate = new java.sql.Date(candidate.getDateNaissance().getTime());
                stmt.setDate(7, sqlDate);
            } else {
                stmt.setNull(7, Types.DATE);
            }

            stmt.executeUpdate();
        }

        // Then insert into candidat table
        String candidatSQL = "INSERT INTO candidat (cin) VALUES (?)";
        try (PreparedStatement stmt = connection.prepareStatement(candidatSQL)) {
            stmt.setInt(1, candidate.getCIN());
            stmt.executeUpdate();
        }

        // Save image to dossier_candidat if available
        if (candidate.getCinImage() != null && candidate.getCinImage().length > 0) {
            saveCinImage(candidate);
        }

        return candidate.getCIN();
    }

    private void saveCinImage(Candidat candidate) throws SQLException {
        String sql = "INSERT INTO dossier_candidat (cin_candidat, cle_document, valeur_document) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, candidate.getCIN());
            stmt.setString(2, "Image CIN");
            // Use a reference to the image instead of storing the binary directly
            stmt.setString(3, "CIN-IMG-" + candidate.getCIN());
            stmt.executeUpdate();

            // In a real application, you would save the image to a file system or BLOB field
            // For now, we'll just simulate this by creating a reference
        }
    }

    public void update(Candidat candidate) throws SQLException {
        // Update personne table
        String personneSQL = "UPDATE personne SET nom=?, prenom=?, adresse=?, mail=?, num_telephone=?, date_naissance=? " +
                "WHERE cin=?";

        try (PreparedStatement stmt = connection.prepareStatement(personneSQL)) {
            stmt.setString(1, candidate.getNom());
            stmt.setString(2, candidate.getPrenom());
            stmt.setString(3, candidate.getAdresse() != null ? candidate.getAdresse() : "");
            stmt.setString(4, candidate.getMail());
            stmt.setInt(5, candidate.getNumTelephone());

            // Convertir java.util.Date en java.sql.Date
            if (candidate.getDateNaissance() != null) {
                java.sql.Date sqlDate = new java.sql.Date(candidate.getDateNaissance().getTime());
                stmt.setDate(6, sqlDate);
            } else {
                stmt.setNull(6, Types.DATE);
            }

            stmt.setInt(7, candidate.getCIN());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Error updating candidate: " + e.getMessage(), e);
        }

        // Update image if needed
        if (candidate.getCinImage() != null && candidate.getCinImage().length > 0) {
            // Check if image record exists
            if (cinImageExists(candidate.getCIN())) {
                updateCinImage(candidate);
            } else {
                saveCinImage(candidate);
            }
        }
    }

    private boolean cinImageExists(int cin) throws SQLException {
        String sql = "SELECT COUNT(*) FROM dossier_candidat WHERE cin_candidat = ? AND cle_document = 'Image CIN'";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, cin);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private void updateCinImage(Candidat candidate) throws SQLException {
        String sql = "UPDATE dossier_candidat SET valeur_document = ? " +
                "WHERE cin_candidat = ? AND cle_document = 'Image CIN'";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "CIN-IMG-" + candidate.getCIN() + "-UPDATED");
            stmt.setInt(2, candidate.getCIN());
            stmt.executeUpdate();
        }
    }

    public List<Candidat> findAll() throws SQLException {
        List<Candidat> candidates = new ArrayList<>();
        String sql = "SELECT p.* FROM personne p " +
                "JOIN candidat c ON p.cin = c.cin";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                try {
                    Candidat candidate = mapResultSetToCandidate(rs);
                    candidates.add(candidate);
                } catch (Exception e) {
                    System.err.println("Erreur de mapping : " + e.getMessage());
                }
            }
        }
        return candidates;
    }

    private Candidat mapResultSetToCandidate(ResultSet rs) throws SQLException {
        Candidat candidate = new Candidat();
        candidate.setCIN(rs.getInt("cin"));
        candidate.setNom(rs.getString("nom"));
        candidate.setPrenom(rs.getString("prenom"));
        candidate.setAdresse(rs.getString("adresse"));
        candidate.setMail(rs.getString("mail"));
        candidate.setNumTelephone(rs.getInt("num_telephone"));

        // Convertir java.sql.Date en java.util.Date
        java.sql.Date dateNaissanceSQL = rs.getDate("date_naissance");
        if (dateNaissanceSQL != null) {
            java.util.Date utilDate = new java.util.Date(dateNaissanceSQL.getTime());
            candidate.setDateNaissance(utilDate);
        }

        // Get CIN image if exists (in a real app, you would load this from storage)
        loadCinImage(candidate);

        return candidate;
    }

    private void loadCinImage(Candidat candidate) {
        // This is a placeholder - in a real application, you would load the actual image data
        // from a file system or from a BLOB field in the database
        candidate.setCinImage(new byte[0]); // Empty byte array for now
    }

    public void delete(int cin) throws SQLException {
        // Due to foreign key constraints, just deleting from personne will cascade to candidat
        String sql = "DELETE FROM personne WHERE cin = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, cin);
            stmt.executeUpdate();
        }
    }

    public List<Candidat> search(String keyword) throws SQLException {
        List<Candidat> candidates = new ArrayList<>();
        String sql = "SELECT p.* FROM personne p " +
                "JOIN candidat c ON p.cin = c.cin " +
                "WHERE p.nom LIKE ? OR p.prenom LIKE ? OR p.cin LIKE ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    candidates.add(mapResultSetToCandidate(rs));
                }
            }
        }
        return candidates;
    }

    public boolean cinExists(int cin) throws SQLException {
        String sql = "SELECT COUNT(*) FROM personne WHERE cin = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, cin);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public Candidat findByCin(int cin) throws SQLException {
        String sql = "SELECT p.* FROM personne p " +
                "JOIN candidat c ON p.cin = c.cin " +
                "WHERE p.cin = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, cin);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCandidate(rs);
                }
            }
        }
        return null;
    }
}