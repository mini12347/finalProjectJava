package Service;

import DAO.ReparationDAO;
import Entities.Reparation;
import Entities.Vehicule;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service class for repair operations
 */
public class ReparationS {
    private static final Logger LOGGER = Logger.getLogger(ReparationS.class.getName());
    private final ReparationDAO reparationDAO;

    /**
     * Constructor initializes the DAO
     */
    public ReparationS() {
        this.reparationDAO = new ReparationDAO();
    }

    /**
     * Retrieves all repairs for a specific vehicle
     *
     * @param vehicle The vehicle to get repairs for
     * @return List of repairs for the vehicle
     * @throws SQLException if a database error occurs
     */
    public List<Reparation> getReparationsParIdVehicule(Vehicule vehicle) throws SQLException {
        return reparationDAO.getReparationParIdVehicule(vehicle.getMatricule());
    }

    /**
     * Adds a new repair for a vehicle
     *
     * @param vehicle The vehicle to add the repair to
     * @param repair The repair to add
     * @throws SQLException if a database error occurs
     */
    public void ajouterReparation(Vehicule vehicle, Reparation repair) throws SQLException {
        try {
            // Update the vehicle's repair list
            vehicle.addReparation(repair);

            // Insert into database
            reparationDAO.insertReparation(repair);

            LOGGER.log(Level.INFO, "Repair added successfully for vehicle: " + vehicle.getMatricule());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding repair for vehicle: " + vehicle.getMatricule(), e);
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error adding repair", e);
            throw new SQLException("Failed to add repair: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a repair for a vehicle
     *
     * @param vehicle The vehicle to delete the repair from
     * @param repair The repair to delete
     * @throws SQLException if a database error occurs
     */
    public void supprimerReparation(Vehicule vehicle, Reparation repair) throws SQLException {
        try {
            // Update the vehicle's repair list
            vehicle.deleteReparation(repair);

            // Delete from database
            reparationDAO.deleteReparation(repair);

            LOGGER.log(Level.INFO, "Repair deleted successfully for vehicle: " + vehicle.getMatricule());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting repair for vehicle: " + vehicle.getMatricule(), e);
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error deleting repair", e);
            throw new SQLException("Failed to delete repair: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an existing repair for a vehicle
     *
     * @param vehicle The vehicle to update the repair for
     * @param repair The repair with updated information
     * @throws SQLException if a database error occurs
     */
    public void modifierReparation(Vehicule vehicle, Reparation repair) throws SQLException {
        try {
            // Update the vehicle's repair list
            vehicle.replaceReparation(repair);

            // Update in database
            reparationDAO.updateReparation(repair);

            LOGGER.log(Level.INFO, "Repair updated successfully for vehicle: " + vehicle.getMatricule());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating repair for vehicle: " + vehicle.getMatricule(), e);
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error updating repair", e);
            throw new SQLException("Failed to update repair: " + e.getMessage(), e);
        }
    }

    /**
     * Gets all repairs from the database
     *
     * @return List of all repairs
     * @throws SQLException if a database error occurs
     */
    public List<Reparation> getAllReparations() throws SQLException {
        return reparationDAO.getAllReparations();
    }

}
