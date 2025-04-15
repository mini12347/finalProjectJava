package Service;

import DAO.AutoEcoleDAO;
import Entities.AutoEcole;
import Entities.Disponibility;

import java.sql.SQLException;

public class AutoEcoleInfosS {
    private AutoEcoleDAO autoEcoleDAO = new AutoEcoleDAO();

    public void addAutoEcole(AutoEcole autoEcole) throws SQLException {
        autoEcoleDAO.addAutoEcole(autoEcole);
    }

    public AutoEcole getAutoEcole() throws SQLException {
       AutoEcole autoEcole = autoEcoleDAO.getLastModifiedAutoEcole();
       return autoEcole;
    }
    public Disponibility getTimeTable() throws SQLException {
        return autoEcoleDAO.getTimeTable();
    }

}
