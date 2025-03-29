package DAO;
import Entities.AutoEcole;
import Entities.Disponibility;
import Entities.Hours;
import java.sql.*;
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;
import Connection.ConxDB;
public class AutoEcoleDAO {
    private Connection connection;

    public AutoEcoleDAO() {
        connection = ConxDB.getInstance();
    }

    public void addAutoEcole(AutoEcole autoEcole) throws SQLException {
        String query = "INSERT INTO auto_ecole (nom, numtel, email, adresse, horaire) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, autoEcole.getNom());
        stmt.setInt(2, autoEcole.getNumtel());
        stmt.setString(3, autoEcole.getEmail());
        stmt.setString(4, autoEcole.getAdresse());
        stmt.setString(5, serializeDisponibility(autoEcole.getHoraire()));
        stmt.executeUpdate();
    }

    public AutoEcole getLastModifiedAutoEcole() throws SQLException {
        String query = "SELECT * FROM auto_ecole ORDER BY updated_at DESC LIMIT 1";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        if (rs.next()) {
            return new AutoEcole(
                    rs.getString("nom"),
                    rs.getInt("numtel"),
                    rs.getString("email"),
                    rs.getString("adresse"),
                    deserializeDisponibility(rs.getString("horaire"))
            );
        } else {
            return null;
        }
    }

    private String serializeDisponibility(Disponibility disponibility) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<DayOfWeek, Hours> entry : disponibility.getDaysOfWeek().entrySet()) {
            sb.append(entry.getKey()).append(":").append(entry.getValue().getStarthour())
                    .append("-").append(entry.getValue().getEndhour()).append(";");
        }
        return sb.toString();
    }

    private Disponibility deserializeDisponibility(String data) {
        Map<DayOfWeek, Hours> daysOfWeek = new HashMap<>();
        if (data != null && !data.isEmpty()) {
            String[] entries = data.split(";");
            for (String entry : entries) {
                String[] parts = entry.split(":");
                DayOfWeek day = DayOfWeek.valueOf(parts[0]);
                String[] hours = parts[1].split("-");
                Hours hourRange = new Hours(Integer.parseInt(hours[0]), Integer.parseInt(hours[1]));
                daysOfWeek.put(day, hourRange);
            }
        }
        return new Disponibility(daysOfWeek);
    }
}
