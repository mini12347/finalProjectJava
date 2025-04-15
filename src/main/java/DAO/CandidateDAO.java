package DAO;

import Entities.Candidat;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import Connection.ConxDB;

public class CandidateDAO {




    public int save(Candidat candidate) throws SQLException {
        if (cinExists(candidate.getCIN())) {
            throw new SQLException("Un candidat avec ce CIN existe déjà");
        }

        String sql = "INSERT INTO candidates (cin, nom, prenom, telephone, email, date_naissance, cin_image) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = ConxDB.getInstance();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, candidate.getCIN());
            stmt.setString(2, candidate.getNom());
            stmt.setString(3, candidate.getPrenom());
            stmt.setInt(4, candidate.getNumTelephone());
            stmt.setString(5, candidate.getMail());
            stmt.setDate(6, candidate.getDateNaissance() != null ?
                    new java.sql.Date(candidate.getDateNaissance().getTime()) : null);
            stmt.setBytes(7, candidate.getCinImage());

            stmt.executeUpdate();
        }

        return candidate.getCIN();
    }

    public void update(Candidat candidate) throws SQLException {
        String sql = "UPDATE candidates SET nom=?, prenom=?, telephone=?, email=?, date_naissance=?, cin_image=? WHERE cin=?";

        try (Connection connection = ConxDB.getInstance();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, candidate.getNom());
            stmt.setString(2, candidate.getPrenom());
            stmt.setInt(3, candidate.getNumTelephone());
            stmt.setString(4, candidate.getMail());
            stmt.setDate(5, candidate.getDateNaissance() != null ?
                    new java.sql.Date(candidate.getDateNaissance().getTime()) : null);
            stmt.setBytes(6, candidate.getCinImage());
            stmt.setInt(7, candidate.getCIN());

            stmt.executeUpdate();
        }
    }

    public void delete(int cin) throws SQLException {
        String sql = "DELETE FROM candidates WHERE cin=?";
        try (Connection connection = ConxDB.getInstance();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, cin);
            stmt.executeUpdate();
        }
    }

    public Candidat findByCin(int cin) throws SQLException {
        String sql = "SELECT * FROM candidates WHERE cin = ?";
        try (Connection connection = ConxDB.getInstance();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, cin);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCandidate(rs);
                }
            }
        }
        return null;
    }

    public List<Candidat> findAll() throws SQLException {
        List<Candidat> candidates = new ArrayList<>();
        String sql = "SELECT * FROM candidates";

        try (Connection connection = ConxDB.getInstance();
                Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                candidates.add(mapResultSetToCandidate(rs));
            }
        }

        return candidates;
    }

    public List<Candidat> search(String keyword) throws SQLException {
        List<Candidat> candidates = new ArrayList<>();
        String sql = "SELECT * FROM candidates WHERE nom LIKE ? OR prenom LIKE ? OR cin LIKE ?";

        try (Connection connection = ConxDB.getInstance();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            String pattern = "%" + keyword + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    candidates.add(mapResultSetToCandidate(rs));
                }
            }
        }

        return candidates;
    }

    public boolean cinExists(int cin) throws SQLException {
        String sql = "SELECT COUNT(*) FROM candidates WHERE cin = ?";
        try (Connection connection = ConxDB.getInstance();
                PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, cin);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private Candidat mapResultSetToCandidate(ResultSet rs) throws SQLException {
        Candidat c = new Candidat();
        c.setCIN(rs.getInt("cin"));
        c.setNom(rs.getString("nom"));
        c.setPrenom(rs.getString("prenom"));
        c.setNumTelephone(rs.getInt("telephone"));
        c.setMail(rs.getString("email"));

        Date date = rs.getDate("date_naissance");
        if (date != null) {
            c.setDateNaissance(new java.util.Date(date.getTime()));
        }

        byte[] image = rs.getBytes("cin_image");
        c.setCinImage(image != null ? image : new byte[0]);

        return c;
    }
}
