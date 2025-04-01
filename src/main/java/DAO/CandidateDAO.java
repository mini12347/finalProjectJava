package DAO;

import Entities.Candidat;
import java.sql.Connection;
import Connection.ConxDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CandidateDAO {
    private Connection connection;

    public CandidateDAO() {
        connection = ConxDB.getInstance(); // Assure-toi que cette méthode retourne bien une connexion valide
    }

    public int save(Candidat candidate) throws SQLException {
        if (emailExists(candidate.getMail())) {
            throw new SQLException("Un candidat avec cet email existe déjà");
        }

        String sql = "INSERT INTO candidates (nom, prenom, cin, telephone, email, date_naissance, cin_image) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            setStatementParameters(stmt, candidate);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) candidate.setId(rs.getInt(1));
            }
        }
        return candidate.getId();
    }

    public void update(Candidat candidate) throws SQLException {
        String sql = "UPDATE candidates SET nom=?, prenom=?, cin=?, telephone=?, email=?, date_naissance=?, cin_image=? WHERE id=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            setStatementParameters(stmt, candidate);
            stmt.setInt(8, candidate.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Error updating candidate: " + e.getMessage(), e);
        }
    }

    private void setStatementParameters(PreparedStatement stmt, Candidat candidate) throws SQLException {
        stmt.setString(1, candidate.getNom());
        stmt.setString(2, candidate.getPrenom());
        stmt.setInt(4, candidate.getCIN());
        stmt.setInt(5, candidate.getNumTelephone());
        stmt.setString(6, candidate.getMail());
        stmt.setDate(7, candidate.getDateNaissance() != null ? Date.valueOf(candidate.getDateNaissance()) : null);

        stmt.setBytes(8, candidate.getCinImage());
    }

    public List<Candidat> findAll() throws SQLException {
        List<Candidat> candidates = new ArrayList<>();
        String sql = "SELECT * FROM candidates";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // Log des métadonnées
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                System.out.println("Colonne " + i + " : " + metaData.getColumnName(i));
            }

            while (rs.next()) {
                try {
                    Candidat candidate = mapResultSetToCandidate(rs);
                    candidates.add(candidate);
                    System.out.println("Candidat ajouté : " + candidate.getNom());
                } catch (Exception e) {
                    System.err.println("Erreur de mapping : " + e.getMessage());
                }
            }
        }
        return candidates;
    }

    private Candidat mapResultSetToCandidate(ResultSet rs) throws SQLException {
        Candidat candidate = new Candidat();
        candidate.setId(rs.getInt("id"));
        candidate.setNom(rs.getString("nom"));
        candidate.setPrenom(rs.getString("prenom"));
        candidate.setCIN(rs.getInt("cin"));
        candidate.setNumTelephone(rs.getInt("telephone"));
        candidate.setMail(rs.getString("email"));

        // Gestion date de naissance
        Date dateNaissanceSQL = rs.getDate("date_naissance");
        candidate.setDateNaissance(dateNaissanceSQL != null ? dateNaissanceSQL.toLocalDate() : null);

        candidate.setCinImage(rs.getBytes("cin_image"));
        return candidate;
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM candidates WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public List<Candidat> search(String keyword) throws SQLException {
        List<Candidat> candidates = new ArrayList<>();
        String sql = "SELECT * FROM candidates WHERE nom LIKE ? OR prenom LIKE ? OR cin LIKE ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            for (int i = 1; i <= 3; i++) stmt.setString(i, searchPattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) candidates.add(mapResultSetToCandidate(rs));
            }
        }
        return candidates;
    }
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM candidates WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

}
