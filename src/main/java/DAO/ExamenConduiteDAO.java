package DAO;

import Entities.Candidat;
import Entities.ExamenConduite;
import Entities.Res;
import Entities.TypeP;
import Connection.ConxDB;
import com.sothawo.mapjfx.Coordinate;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExamenConduiteDAO {

    private final CandidateDAO candidatDAO;

    public ExamenConduiteDAO() {
        this.candidatDAO = new CandidateDAO();
    }

    public ExamenConduiteDAO(CandidateDAO candidatDAO) {
        this.candidatDAO = candidatDAO;
    }

    public boolean insert(ExamenConduite examen) {
        String query = "INSERT INTO examenconduite (date, time, candidat_cin, resultat, type_permis, localisation, cout) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setDate(1, Date.valueOf(examen.getDate()));
            pstmt.setTime(2, examen.getTime());
            pstmt.setInt(3, examen.getCandidat().getCIN());
            pstmt.setString(4, examen.getResultat().name());
            pstmt.setString(5, examen.getType().toString());
            pstmt.setString(6, examen.getLocalisation());
            pstmt.setDouble(7, examen.getCout());

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
            System.err.println("Error inserting driving exam: " + e.getMessage());
            return false;
        }
    }

    public boolean update(ExamenConduite examen) {
        String query = "UPDATE examenconduite SET date = ?, time = ?, candidat_cin = ?, resultat = ?, " +
                "type_permis = ?, localisation = ?, cout = ? WHERE id = ?";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setDate(1, Date.valueOf(examen.getDate()));
            pstmt.setTime(2, examen.getTime());
            pstmt.setInt(3, examen.getCandidat().getCIN());
            pstmt.setString(4, examen.getResultat().name());
            pstmt.setString(5, examen.getType().toString());
            pstmt.setString(6, examen.getLocalisation());
            pstmt.setDouble(7, examen.getCout());
            pstmt.setInt(8, examen.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating driving exam: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(int id) {
        String query = "DELETE FROM examenconduite WHERE id = ?";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting driving exam: " + e.getMessage());
            return false;
        }
    }

    public ExamenConduite getById(int id) {
        String query = "SELECT * FROM examenconduite WHERE id = ?";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractExamenFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting driving exam by ID: " + e.getMessage());
        }
        return null;
    }

    public List<ExamenConduite> getAll() {
        List<ExamenConduite> examens = new ArrayList<>();
        String query = "SELECT * FROM examenconduite";

        try (Connection conn = ConxDB.getInstance();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                examens.add(extractExamenFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all driving exams: " + e.getMessage());
        }
        return examens;
    }

    public List<ExamenConduite> getByCandidatCin(String cin) {
        List<ExamenConduite> examens = new ArrayList<>();
        String query = "SELECT * FROM examenconduite WHERE candidat_cin = ?";

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

    public List<ExamenConduite> getByTypePermis(String typePermis) {
        List<ExamenConduite> examens = new ArrayList<>();
        String query = "SELECT * FROM examenconduite WHERE type_permis = ?";

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

    public List<ExamenConduite> getByResult(Res resultat) {
        List<ExamenConduite> examens = new ArrayList<>();
        String query = "SELECT * FROM examenconduite WHERE resultat = ?";

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

    public List<ExamenConduite> getByLocalisation(String localisation) {
        List<ExamenConduite> examens = new ArrayList<>();
        String query = "SELECT * FROM examenconduite WHERE localisation LIKE ?";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, "%" + localisation + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    examens.add(extractExamenFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting exams by location: " + e.getMessage());
        }
        return examens;
    }

    public List<ExamenConduite> getByCostRange(double minCost, double maxCost) {
        List<ExamenConduite> examens = new ArrayList<>();
        String query = "SELECT * FROM examenconduite WHERE cout >= ? AND cout <= ?";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setDouble(1, minCost);
            pstmt.setDouble(2, maxCost);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    examens.add(extractExamenFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting exams by cost range: " + e.getMessage());
        }
        return examens;
    }

    public double getAverageCost() {
        String query = "SELECT AVG(cout) as average_cost FROM examenconduite";

        try (Connection conn = ConxDB.getInstance();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getDouble("average_cost");
            }
        } catch (SQLException e) {
            System.err.println("Error calculating average cost: " + e.getMessage());
        }
        return 0.0;
    }

    public Optional<Coordinate> extractCoordinatesFromLocalisation(String localisation) {
        try {
            if (localisation != null && localisation.matches(".*\\d+\\.\\d+.*,.*\\d+\\.\\d+.*")) {
                String[] parts = localisation.split(",");
                Optional<Double> latitude = Optional.empty();
                Optional<Double> longitude = Optional.empty();

                for (String part : parts) {
                    part = part.trim();
                    if (part.matches("\\d+\\.\\d+")) {
                        double value = Double.parseDouble(part);
                        if (!latitude.isPresent() && value >= 30 && value <= 40) {
                            latitude = Optional.of(value);
                        } else if (!longitude.isPresent() && value >= 5 && value <= 15) {
                            longitude = Optional.of(value);
                        }
                    }
                }

                if (latitude.isPresent() && longitude.isPresent()) {
                    return Optional.of(new Coordinate(latitude.get(), longitude.get()));
                }
            }
        } catch (Exception e) {
            System.err.println("Error extracting coordinates: " + e.getMessage());
        }
        return Optional.empty();
    }

    private double calculateDistance(Coordinate coord1, Coordinate coord2) {
        final int R = 6371; // Earth radius in km

        double latDistance = Math.toRadians(coord2.getLatitude() - coord1.getLatitude());
        double lonDistance = Math.toRadians(coord2.getLongitude() - coord1.getLongitude());

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(coord1.getLatitude())) * Math.cos(Math.toRadians(coord2.getLatitude())) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    private ExamenConduite extractExamenFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        Date date = rs.getDate("date");
        Time time = rs.getTime("time");
        String candidatCin = rs.getString("candidat_cin");
        String resultatStr = rs.getString("resultat");
        String typePermisStr = rs.getString("type_permis");
        String localisation = rs.getString("localisation");
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

        return new ExamenConduite(
                id,
                date.toLocalDate(),
                time,
                candidat,
                resultat,
                localisation,
                typePermis,
                cout
        );
    }

    public boolean isInTunisArea(Coordinate coord) {
        final double MIN_LAT = 36.70;
        final double MAX_LAT = 36.90;
        final double MIN_LON = 10.05;
        final double MAX_LON = 10.25;

        return coord.getLatitude() >= MIN_LAT && coord.getLatitude() <= MAX_LAT &&
                coord.getLongitude() >= MIN_LON && coord.getLongitude() <= MAX_LON;
    }

    public double getGlobalSuccessRate() {
        String query = "SELECT " +
                "(SELECT COUNT(*) FROM examenconduite WHERE resultat = 'SUCCES') AS success_count, " +
                "(SELECT COUNT(*) FROM examenconduite WHERE resultat = 'SUCCES' OR resultat = 'ECHEC') AS total_count";

        try (Connection conn = ConxDB.getInstance();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                int successCount = rs.getInt("success_count");
                int totalCount = rs.getInt("total_count");

                if (totalCount == 0) {
                    return 0.0;
                }
                return (double) successCount / totalCount * 100;
            }
        } catch (SQLException e) {
            System.err.println("Error calculating global success rate: " + e.getMessage());
        }
        return 0.0;
    }

    public double getSuccessRateByCandidatCin(String cin) {
        String query = "SELECT " +
                "(SELECT COUNT(*) FROM examenconduite WHERE candidat_cin = ? AND resultat = 'SUCCES') AS success_count, " +
                "(SELECT COUNT(*) FROM examenconduite WHERE candidat_cin = ?) AS total_count";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, cin);
            pstmt.setString(2, cin);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int successCount = rs.getInt("success_count");
                    int totalCount = rs.getInt("total_count");

                    if (totalCount == 0) {
                        return 0.0;
                    }
                    return (double) successCount / totalCount * 100;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calculating success rate by candidate: " + e.getMessage());
        }
        return 0.0;
    }
}