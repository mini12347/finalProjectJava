package Service;

import DAO.AutoEcoleDAO;
import Entities.AutoEcole;

import java.sql.SQLException;

public class AutoEcoleInfosS {
    private AutoEcoleDAO autoEcoleDAO = new AutoEcoleDAO();

    public AutoEcoleInfosS() throws SQLException {
    }

    public void addAutoEcole(AutoEcole autoEcole) throws SQLException {
        autoEcoleDAO.addAutoEcole(autoEcole);
    }

    public AutoEcole getAutoEcole() throws SQLException {
       AutoEcole autoEcole = autoEcoleDAO.getLastModifiedAutoEcole();
        if (autoEcole==null) {
            return null;
        }
        return autoEcole;
    }


}
