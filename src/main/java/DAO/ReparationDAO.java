package DAO;

import Connection.ConxDB;
import Entities.Reparation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReparationDAO {
    private static final Logger LOGGER = Logger.getLogger(ReparationDAO.class.getName());

    public ReparationDAO() {
        // No connection field needed
    }

    public List<Reparation> getReparationParIdVehicule(String vehiculeMatricule) {
        List<Reparation> reparations = new ArrayList<>();
        String query = "SELECT * FROM reparation WHERE vehicule_immatriculation = ?";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement statement = conn.prepareStatement(query)) {

            statement.setString(1, vehiculeMatricule);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    reparations.add(extractReparationFromResultSet(resultSet, vehiculeMatricule));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving repairs for vehicle: " + vehiculeMatricule, e);
        }
        return reparations;
    }

    public void insertReparation(Reparation reparation) throws SQLException {
        String query = "INSERT INTO reparation (vehicule_immatriculation, description, date_reparation, " +
                "cout, kilometrage, facture_scan) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement statement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            setReparationParameters(statement, reparation);
            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating repair failed, no rows affected.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    reparation.setId(generatedKeys.getInt(1));
                }
            }
            LOGGER.log(Level.INFO, "Repair inserted successfully with ID: " + reparation.getId());
        }
    }

    public void updateReparation(Reparation reparation) throws SQLException {
        String query = "UPDATE reparation SET vehicule_immatriculation = ?, description = ?, " +
                "date_reparation = ?, cout = ?, kilometrage = ?, facture_scan = ? WHERE id = ?";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement statement = conn.prepareStatement(query)) {

            setReparationParameters(statement, reparation);
            statement.setInt(7, reparation.getId());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating repair failed, no rows affected. ID: " + reparation.getId());
            }
            LOGGER.log(Level.INFO, "Repair updated successfully with ID: " + reparation.getId());
        }
    }

    public void deleteReparation(Reparation reparation) throws SQLException {
        String query = "DELETE FROM reparation WHERE id = ?";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement statement = conn.prepareStatement(query)) {

            statement.setInt(1, reparation.getId());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Deleting repair failed, no rows affected. ID: " + reparation.getId());
            }
            LOGGER.log(Level.INFO, "Repair deleted successfully with ID: " + reparation.getId());
        }
    }

    public List<Reparation> getAllReparations() {
        List<Reparation> reparations = new ArrayList<>();
        String query = "SELECT * FROM reparation";

        try (Connection conn = ConxDB.getInstance();
             Statement statement = conn.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                String matricule = resultSet.getString("vehicule_immatriculation");
                reparations.add(extractReparationFromResultSet(resultSet, matricule));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving all repairs", e);
        }
        return reparations;
    }

    public Reparation getReparationById(int id) {
        String query = "SELECT * FROM reparation WHERE id = ?";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement statement = conn.prepareStatement(query)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String matricule = resultSet.getString("vehicule_immatriculation");
                    return extractReparationFromResultSet(resultSet, matricule);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving repair with ID: " + id, e);
        }
        return null;
    }

    public void updateFacturePath(int reparationId, String facturePath) throws SQLException {
        String query = "UPDATE reparation SET facture_scan = ? WHERE id = ?";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, facturePath);
            stmt.setInt(2, reparationId);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating invoice path failed, no rows affected. ID: " + reparationId);
            }
            LOGGER.log(Level.INFO, "Invoice path updated successfully for repair ID: " + reparationId);
        }
    }

    private Reparation extractReparationFromResultSet(ResultSet resultSet, String matricule) throws SQLException {
        return new Reparation(
                resultSet.getInt("id"),
                matricule,
                resultSet.getString("description"),
                resultSet.getDate("date_reparation"),
                resultSet.getDouble("cout"),
                resultSet.getInt("kilometrage"),
                resultSet.getString("facture_scan")
        );
    }

    private void setReparationParameters(PreparedStatement statement, Reparation reparation) throws SQLException {
        statement.setString(1, reparation.getMatriculeVehicule());
        statement.setString(2, reparation.getDescription());

        if (reparation.getDate() != null) {
            statement.setDate(3, new java.sql.Date(reparation.getDate().getTime()));
        } else {
            statement.setNull(3, Types.DATE);
        }

        statement.setDouble(4, reparation.getCout());
        statement.setInt(5, reparation.getKilometrage());

        if (reparation.getFactureScan() != null) {
            statement.setString(6, reparation.getFactureScan());
        } else {
            statement.setNull(6, Types.VARCHAR);
        }
    }
}