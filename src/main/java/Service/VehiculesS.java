package Service;
import DAO.VehiculesDAO;
import Entities.Vehicule;
import java.sql.SQLException;
import java.util.List;
public class VehiculesS {
    private VehiculesDAO vehiculesDAO;
    public VehiculesS() throws SQLException {
        vehiculesDAO = new VehiculesDAO();
    }
    public List<Vehicule> getVehicules() throws SQLException {
         return vehiculesDAO.getAllVehicules();
    }
    public Vehicule getVehicule(String id) throws SQLException {
        return vehiculesDAO.getVehiculeByMatricule(id);
    }
    public void addVehicule(Vehicule vehicule) throws SQLException {
        vehiculesDAO.addVehicule(vehicule);
    }
    public void updateVehicule(Vehicule vehicule) throws SQLException {
        vehiculesDAO.updateVehicule(vehicule);
    }
    public void deleteVehicule(String id) throws SQLException {
        vehiculesDAO.deleteVehicule(id);
    }
    public List<Vehicule> getVehiculeOrdered(String carc,String order) throws SQLException {
        return vehiculesDAO.getVehiulesOrdered(carc," "+order);
    }

}
