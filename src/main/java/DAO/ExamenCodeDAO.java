package DAO;

import Entities.Candidat;
import Entities.ExamenCode;
import Entities.Res;
import Entities.TypeP;
import Connection.ConxDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExamenCodeDAO {

    private final CandidateDAO candidatDAO;

    // Constructor with dependency injection
    public ExamenCodeDAO() {
        this.candidatDAO = new CandidateDAO();
    }

    public ExamenCodeDAO(CandidateDAO candidatDAO) {
        this.candidatDAO = candidatDAO;
    }

    // Insert an exam
    public boolean insert(ExamenCode examen) {
        String query = "INSERT INTO examen_code (date, time, candidat_cin, resultat, type_permis, cout) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setDate(1, Date.valueOf(examen.getDate()));
            pstmt.setTime(2, examen.getTime());
            pstmt.setInt(3, examen.getCandidat().getCIN());
            pstmt.setString(4, examen.getResultat().name());
            pstmt.setString(5, examen.getType().toString());
            pstmt.setDouble(6, examen.getCout());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        examen.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error inserting exam: " + e.getMessage());
            return false;
        }
    }

    // Update an exam
    public boolean update(ExamenCode examen) {
        String query = "UPDATE examen_code SET date = ?, time = ?, candidat_cin = ?, " +
                "resultat = ?, type_permis = ?, cout = ? WHERE id = ?";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setDate(1, Date.valueOf(examen.getDate()));
            pstmt.setTime(2, examen.getTime());
            pstmt.setInt(3, examen.getCandidat().getCIN());
            pstmt.setString(4, examen.getResultat().name());
            pstmt.setString(5, examen.getType().toString());
            pstmt.setDouble(6, examen.getCout());
            pstmt.setInt(7, examen.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating exam: " + e.getMessage());
            return false;
        }
    }

    // Delete an exam
    public boolean delete(int id) {
        String query = "DELETE FROM examen_code WHERE id = ?";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting exam: " + e.getMessage());
            return false;
        }
    }

    // Get exam by ID
    public ExamenCode getById(int id) {
        String query = "SELECT * FROM examen_code WHERE id = ?";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractExamenFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting exam by ID: " + e.getMessage());
        }
        return null;
    }

    // Get all exams
    public List<ExamenCode> getAll() {
        List<ExamenCode> examens = new ArrayList<>();
        String query = "SELECT * FROM examen_code";

        try (Connection conn = ConxDB.getInstance();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                examens.add(extractExamenFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all exams: " + e.getMessage());
        }
        return examens;
    }

    // Get exams by candidate CIN
    public List<ExamenCode> getByCandidatCin(String cin) {
        List<ExamenCode> examens = new ArrayList<>();
        String query = "SELECT * FROM examen_code WHERE candidat_cin = ?";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, cin);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    examens.add(extractExamenFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting exams by candidate CIN: " + e.getMessage());
        }
        return examens;
    }

    // Get exams by license type
    public List<ExamenCode> getByTypePermis(String typePermis) {
        List<ExamenCode> examens = new ArrayList<>();
        String query = "SELECT * FROM examen_code WHERE type_permis = ?";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, typePermis);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    examens.add(extractExamenFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting exams by license type: " + e.getMessage());
        }
        return examens;
    }

    // Get exams by result
    public List<ExamenCode> getByResult(Res resultat) {
        List<ExamenCode> examens = new ArrayList<>();
        String query = "SELECT * FROM examen_code WHERE resultat = ?";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, resultat.name());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    examens.add(extractExamenFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting exams by result: " + e.getMessage());
        }
        return examens;
    }

    // Get exams with cost greater than specified amount
    public List<ExamenCode> getByCostGreaterThan(double cout) {
        List<ExamenCode> examens = new ArrayList<>();
        String query = "SELECT * FROM examen_code WHERE cout > ?";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setDouble(1, cout);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    examens.add(extractExamenFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting exams by cost: " + e.getMessage());
        }
        return examens;
    }

    // Helper method to extract Exam object from ResultSet
    private ExamenCode extractExamenFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        Date date = rs.getDate("date");
        Time time = rs.getTime("time");
        String candidatCin = rs.getString("candidat_cin");
        String resultatStr = rs.getString("resultat");
        String typePermisStr = rs.getString("type_permis");
        double cout = rs.getDouble("cout");

        Candidat candidat = candidatDAO.findByCin(Integer.parseInt(candidatCin));

        Res resultat;
        try {
            resultat = Res.valueOf(resultatStr);
        } catch (IllegalArgumentException e) {
            System.err.println("Unknown result value in database: " + resultatStr);
            resultat = Res.EnATTENTE;
        }

        TypeP typePermis = TypeP.valueOf(typePermisStr);

        ExamenCode examen = new ExamenCode(
                date.toLocalDate(),
                time,
                candidat,
                resultat,
                typePermis,
                cout
        );
        examen.setId(id);
        return examen;
    }
}