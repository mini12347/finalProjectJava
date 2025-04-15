package DAO;

import Connection.ConxDB;
import Entities.Paiement;
import Entities.ParFacilite;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PaiementDAO {
    private static final Logger LOGGER = Logger.getLogger(PaiementDAO.class.getName());


    public List<Paiement> getPaiementsByCIN(int cin) {
        List<Paiement> paiements = new ArrayList<>();
        String query = "SELECT * FROM paiement WHERE id_client = ? ORDER BY date, time";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, cin);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Paiement paiement = createPaiementFromResultSet(rs);
                    paiement.setParFacilite(getParFaciliteByPaiementId(paiement.getIdPaiement()));
                    paiements.add(paiement);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting payments by CIN", e);
        }
        return paiements;
    }

    public void insertPaiement(Paiement paiement) throws SQLException {
        String query = "INSERT INTO paiement (date, time, id_client, description, montant, etat) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement statement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            statement.setDate(1, Date.valueOf(paiement.getDate()));
            statement.setTime(2, paiement.getTime());
            statement.setInt(3, paiement.getIdClient());
            statement.setString(4, paiement.getDescription());
            statement.setDouble(5, paiement.getMontant());
            statement.setString(6, paiement.getEtat());

            int rows = statement.executeUpdate();

            if (rows == 0) {
                throw new SQLException("Payment insertion failed, no rows affected.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    paiement.setIdPaiement(generatedKeys.getInt(1));
                }
            }
            LOGGER.log(Level.INFO, "Payment added successfully: " + paiement);
        }
    }

    public boolean initiateParFacilitePayment(Paiement paiement) {
        try (Connection conn = ConxDB.getInstance()) {
            conn.setAutoCommit(false);
            try {
                double totalAmount = paiement.getMontant();
                double downPayment = totalAmount * 0.2;
                double remainingAmount = totalAmount * 0.8;
                List<Double> installments = createInstallments(remainingAmount);

                insertParFaciliteEntry(conn, paiement.getIdPaiement(), downPayment, installments);
                updatePaiementState(conn, paiement, "par facilité");

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                LOGGER.log(Level.SEVERE, "Error initiating installment payment", e);
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database connection error", e);
            return false;
        }
    }

    public boolean effectuerPaiement(Paiement paiement, boolean isParFacilite) {
        try (Connection conn = ConxDB.getInstance()) {
            conn.setAutoCommit(false);
            try {
                if (isParFacilite && paiement.getParFacilite() != null) {
                    processInstallmentPayment(conn, paiement);
                } else {
                    updatePaiementStateWithDateTime(conn, paiement, "effectué");
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                LOGGER.log(Level.SEVERE, "Error processing payment", e);
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database connection error", e);
            return false;
        }
    }

    private void processInstallmentPayment(Connection conn, Paiement paiement) throws SQLException {
        ParFacilite parFacilite = paiement.getParFacilite();
        List<Double> montants = parFacilite.getMontans();

        if (parFacilite.getAccompte() > 0) {
            updatePaiementState(conn, paiement, "acompte payé");
            updateDownPayment(conn, paiement.getIdPaiement(), 0);
        } else if (!montants.isEmpty()) {
            montants.remove(0);
            updateInstallments(conn, paiement.getIdPaiement(), montants);

            if (montants.isEmpty()) {
                updatePaiementStateWithDateTime(conn, paiement, "effectué");
            } else {
                updatePaiementState(conn, paiement, "en cours");
            }
        }
    }

    // Helper methods with Connection parameter for transaction control
    private void insertParFaciliteEntry(Connection conn, int idPaiement, double downPayment,
                                        List<Double> installments) throws SQLException {
        String query = "INSERT INTO par_facilite (id_paiement, accompte, montans) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, idPaiement);
            ps.setDouble(2, downPayment);
            ps.setString(3, installments.toString());
            ps.executeUpdate();
        }
    }

    private void updatePaiementState(Connection conn, Paiement paiement, String nouvelEtat) throws SQLException {
        String query = "UPDATE paiement SET etat = ? WHERE id_paiement = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, nouvelEtat);
            ps.setInt(2, paiement.getIdPaiement());
            ps.executeUpdate();
        }
    }

    private void updatePaiementStateWithDateTime(Connection conn, Paiement paiement, String nouvelEtat)
            throws SQLException {
        String query = "UPDATE paiement SET etat = ?, date = CURRENT_DATE, time = CURRENT_TIME WHERE id_paiement = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, nouvelEtat);
            ps.setInt(2, paiement.getIdPaiement());
            ps.executeUpdate();
        }
    }

    private void updateDownPayment(Connection conn, int idPaiement, double newDownPayment) throws SQLException {
        String query = "UPDATE par_facilite SET accompte = ? WHERE id_paiement = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setDouble(1, newDownPayment);
            ps.setInt(2, idPaiement);
            ps.executeUpdate();
        }
    }

    private void updateInstallments(Connection conn, int idPaiement, List<Double> montants) throws SQLException {
        String query = "UPDATE par_facilite SET montans = ? WHERE id_paiement = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, montants.toString());
            ps.setInt(2, idPaiement);
            ps.executeUpdate();
        }
    }

    // Non-transactional helper methods
    public ParFacilite getParFaciliteByPaiementId(int idPaiement) {
        String query = "SELECT p.*, pf.accompte, pf.montans FROM paiement p " +
                "JOIN par_facilite pf ON p.id_paiement = pf.id_paiement " +
                "WHERE p.id_paiement = ?";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, idPaiement);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return createParFaciliteFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting installment payment", e);
        }
        return null;
    }

    private Paiement createPaiementFromResultSet(ResultSet rs) throws SQLException {
        return new Paiement(
                rs.getInt("id_paiement"),
                rs.getDate("date").toLocalDate(),
                rs.getTime("time"),
                rs.getInt("id_client"),
                rs.getString("description"),
                rs.getDouble("montant"),
                rs.getString("etat")
        );
    }

    private ParFacilite createParFaciliteFromResultSet(ResultSet rs) throws SQLException {
        return new ParFacilite(
                rs.getInt("id_paiement"),
                rs.getDate("date").toLocalDate(),
                rs.getTime("time"),
                rs.getInt("id_client"),
                rs.getString("description"),
                rs.getDouble("montant"),
                rs.getString("etat"),
                rs.getDouble("accompte"),
                parseMontants(rs.getString("montans"))
        );
    }

    private List<Double> createInstallments(double remainingAmount) {
        double installmentAmount = remainingAmount / 3;
        return List.of(installmentAmount, installmentAmount, installmentAmount);
    }

    private List<Double> parseMontants(String montansJson) {
        if (montansJson == null || montansJson.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String cleanedMontans = montansJson.replaceAll("[\\[\\]]", "").trim();
        if (cleanedMontans.isEmpty()) {
            return new ArrayList<>();
        }

        return List.of(cleanedMontans.split(","))
                .stream()
                .map(String::trim)
                .map(Double::parseDouble)
                .collect(Collectors.toList());
    }
}