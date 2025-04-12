package DAO;

import Connection.ConxDB;
import Entities.Paiement;
import Entities.ParFacilite;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PaiementDAO {
    private final Connection connection;

    public PaiementDAO() throws SQLException {
        connection = ConxDB.getInstance();
    }

    public List<Paiement> getPaiementsByCIN(int cin) {
        List<Paiement> paiements = new ArrayList<>();
        String query = "SELECT * FROM paiement WHERE id_client = ? ORDER BY date, time";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, cin);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Paiement paiement = createPaiementFromResultSet(rs);

                    // Check if this payment has a par_facilite entry
                    ParFacilite parFacilite = getParFaciliteByPaiementId(paiement.getIdPaiement());
                    paiement.setParFacilite(parFacilite);

                    paiements.add(paiement);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return paiements;
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

    public ParFacilite getParFaciliteByPaiementId(int idPaiement) {
        ParFacilite parFacilite = null;
        String query = "SELECT p.*, pf.accompte, pf.montans FROM paiement p " +
                "JOIN par_facilite pf ON p.id_paiement = pf.id_paiement " +
                "WHERE p.id_paiement = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, idPaiement);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    parFacilite = createParFaciliteFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return parFacilite;
    }

    private ParFacilite createParFaciliteFromResultSet(ResultSet rs) throws SQLException {
        List<Double> montants = parseMontants(rs.getString("montans"));

        return new ParFacilite(
                rs.getInt("id_paiement"),
                rs.getDate("date").toLocalDate(),
                rs.getTime("time"),
                rs.getInt("id_client"),
                rs.getString("description"),
                rs.getDouble("montant"),
                rs.getString("etat"),
                rs.getDouble("accompte"),
                montants
        );
    }

    private List<Double> parseMontants(String montansJson) {
        if (montansJson == null || montansJson.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // Remove brackets and split
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

    public boolean initiateParFacilitePayment(Paiement paiement) {
        try {
            // Start transaction
            connection.setAutoCommit(false);

            // Calculate down payment (20%) and installments (80%)
            double totalAmount = paiement.getMontant();
            double downPayment = totalAmount * 0.2;
            double remainingAmount = totalAmount * 0.8;

            // Create 3 installments
            List<Double> installments = createInstallments(remainingAmount);

            // Insert par_facilite entry
            insertParFaciliteEntry(paiement.getIdPaiement(), downPayment, installments);

            // Update payment state
            updatePaiementState(paiement, "par facilité");

            // Commit transaction
            connection.commit();
            return true;
        } catch (SQLException e) {
            // Rollback in case of error
            handleTransactionRollback(e);
            return false;
        } finally {
            resetAutoCommit();
        }
    }

    private List<Double> createInstallments(double remainingAmount) {
        // Divide the remaining amount into 3 roughly equal installments
        double installmentAmount = remainingAmount / 3;
        List<Double> installments = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            installments.add(installmentAmount);
        }
        return installments;
    }

    private void insertParFaciliteEntry(int idPaiement, double downPayment, List<Double> installments) throws SQLException {
        String insertQuery = "INSERT INTO par_facilite (id_paiement, accompte, montans) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(insertQuery)) {
            ps.setInt(1, idPaiement);
            ps.setDouble(2, downPayment);
            ps.setString(3, installments.toString());
            ps.executeUpdate();
        }
    }

    public boolean effectuerPaiement(Paiement paiement, boolean isParFacilite) {
        try {
            // Start transaction
            connection.setAutoCommit(false);

            // Vérifier si le paiement est par facilité
            if (isParFacilite && paiement.getParFacilite() != null) {
                ParFacilite parFacilite = paiement.getParFacilite();
                List<Double> montants = parFacilite.getMontans();

                // Première étape : payer l'acompte
                if (parFacilite.getAccompte() > 0) {
                    // Mise à jour de l'état initial
                    updatePaiementState(paiement, "acompte payé");

                    // Réinitialiser l'acompte
                    updateDownPayment(paiement.getIdPaiement(), 0);
                }
                // Ensuite, payer les tranches
                else if (!montants.isEmpty()) {
                    // Supprimer la première tranche
                    montants.remove(0);

                    // Mettre à jour les tranches restantes
                    updateInstallments(paiement.getIdPaiement(), montants);

                    // Si plus de tranches, mettre à jour l'état
                    if (montants.isEmpty()) {
                        updatePaiementStateWithDateTime(paiement, "effectué");
                    } else {
                        updatePaiementState(paiement, "en cours");
                    }
                }
            }
            // Paiement standard
            else {
                updatePaiementStateWithDateTime(paiement, "effectué");
            }

            // Commit transaction
            connection.commit();
            return true;
        } catch (SQLException e) {
            // Rollback in case of error
            handleTransactionRollback(e);
            return false;
        } finally {
            resetAutoCommit();
        }
    }

    // Méthode pour mettre à jour l'état avec la date et l'heure actuelles
    private void updatePaiementStateWithDateTime(Paiement paiement, String nouvelEtat) throws SQLException {
        String updatePaiementQuery = "UPDATE paiement SET etat = ?, date = CURRENT_DATE, time = CURRENT_TIME WHERE id_paiement = ?";
        try (PreparedStatement ps = connection.prepareStatement(updatePaiementQuery)) {
            ps.setString(1, nouvelEtat);
            ps.setInt(2, paiement.getIdPaiement());
            ps.executeUpdate();
        }
    }

    private void updateDownPayment(int idPaiement, double newDownPayment) throws SQLException {
        String updateQuery = "UPDATE par_facilite SET accompte = ? WHERE id_paiement = ?";
        try (PreparedStatement ps = connection.prepareStatement(updateQuery)) {
            ps.setDouble(1, newDownPayment);
            ps.setInt(2, idPaiement);
            ps.executeUpdate();
        }
    }

    private void updateInstallments(int idPaiement, List<Double> montants) throws SQLException {
        String updateQuery = "UPDATE par_facilite SET montans = ? WHERE id_paiement = ?";
        try (PreparedStatement ps = connection.prepareStatement(updateQuery)) {
            // Convert montants back to JSON-like string
            ps.setString(1, montants.toString());
            ps.setInt(2, idPaiement);
            ps.executeUpdate();
        }
    }

    private void updatePaiementState(Paiement paiement, String nouvelEtat) throws SQLException {
        String updatePaiementQuery = "UPDATE paiement SET etat = ? WHERE id_paiement = ?";
        try (PreparedStatement ps = connection.prepareStatement(updatePaiementQuery)) {
            ps.setString(1, nouvelEtat);
            ps.setInt(2, paiement.getIdPaiement());
            ps.executeUpdate();
        }
    }

    private void handleTransactionRollback(SQLException e) {
        try {
            connection.rollback();
        } catch (SQLException rollbackEx) {
            rollbackEx.printStackTrace();
        }
        e.printStackTrace();
    }

    private void resetAutoCommit() {
        try {
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}