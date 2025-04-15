package Service;

import DAO.VehiculesDAO;
import Entities.Vehicule;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service class for vehicle operations
 */
public class VehiculesS {
    private static final Logger LOGGER = Logger.getLogger(VehiculesS.class.getName());
    private final VehiculesDAO vehiculesDAO;

    public VehiculesS() {
        vehiculesDAO = new VehiculesDAO();
    }

    /**
     * Retrieves all vehicles
     * @return List of all vehicles
     * @throws SQLException if a database error occurs
     */
    public List<Vehicule> getVehicules() throws SQLException {
        return vehiculesDAO.getAllVehicules();
    }

    /**
     * Retrieves a vehicle by its ID
     * @param id Vehicle registration number
     * @return Vehicle object or null if not found
     * @throws SQLException if a database error occurs
     */
    public Vehicule getVehicule(String id) throws SQLException {
        return vehiculesDAO.getVehiculeByMatricule(id);
    }

    /**
     * Adds a new vehicle
     * @param vehicule Vehicle to add
     * @throws SQLException if a database error occurs
     */
    public void addVehicule(Vehicule vehicule) throws SQLException {
        try {
            vehiculesDAO.addVehicule(vehicule);
            LOGGER.log(Level.INFO, "Vehicle added successfully: " + vehicule.getMatricule());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding vehicle: " + vehicule.getMatricule(), e);
            throw e;
        }
    }

    /**
     * Updates an existing vehicle
     * @param vehicule Vehicle to update
     * @throws SQLException if a database error occurs
     */
    public void updateVehicule(Vehicule vehicule) throws SQLException {
        try {
            vehiculesDAO.updateVehicule(vehicule);
            LOGGER.log(Level.INFO, "Vehicle updated successfully: " + vehicule.getMatricule());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating vehicle: " + vehicule.getMatricule(), e);
            throw e;
        }
    }

    /**
     * Deletes a vehicle
     * @param id Vehicle registration number
     * @throws SQLException if a database error occurs
     */
    public void deleteVehicule(String id) throws SQLException {
        try {
            vehiculesDAO.deleteVehicule(id);
            LOGGER.log(Level.INFO, "Vehicle deleted successfully: " + id);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting vehicle: " + id, e);
            throw e;
        }
    }

    /**
     * Retrieves vehicles ordered by a specific column
     * @param column Column name to order by
     * @param order Order direction (ASC or DESC)
     * @return Ordered list of vehicles
     * @throws SQLException if a database error occurs
     */
    public List<Vehicule> getVehiculeOrdered(String column, String order) throws SQLException {
        try {
            return vehiculesDAO.getVehiulesOrdered(column, order);
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid column name: " + column, e);
            // Fall back to ordering by matricule
            return vehiculesDAO.getVehiulesOrdered("matricule", order);
        }
    }
}
