package DAO;

import Connection.ConxDB;
import Entities.TypeP;
import Entities.Vehicule;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VehiculesDAO {
    private Connection conx;
    private static final SimpleDateFormat DB_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");

    public VehiculesDAO() {
        conx = ConxDB.getInstance();
    }

    public List<Vehicule> getAllVehicules() throws SQLException {
        String query = "SELECT * FROM vehicule";
        List<Vehicule> vehicules = new ArrayList<>();

        try (Statement stmt = conx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String matricule = rs.getString("matricule");
                String datemStr = rs.getString("datem");
                Integer kilometrage = rs.getObject("kilometrage", Integer.class);
                String typeStr = rs.getString("type");

                Date datem = datemStr != null ? StringToDate(datemStr) : null;
                TypeP type = typeOf(typeStr);

                Vehicule vehicule = new Vehicule(matricule, datem,
                        kilometrage != null ? kilometrage : 0, type);
                vehicules.add(vehicule);
            }
        }

        return vehicules;
    }

    public List<Vehicule> getVehiulesOrdered(String colname, String order) throws SQLException {
        String query = "SELECT * FROM vehicule ORDER BY " + colname + " " + order;
        List<Vehicule> vehicules = new ArrayList<>();

        try (Statement stmt = conx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                vehicules.add(new Vehicule(
                        rs.getString("matricule"),
                        StringToDate(rs.getString("datem")),
                        rs.getInt("kilometrage"),
                        typeOf(rs.getString("type"))
                ));
            }
        }

        return vehicules;
    }

    public static Date StringToDate(String datem) {
        try {
            return DB_DATE_FORMAT.parse(datem);
        } catch (ParseException e) {
            System.err.println("Error parsing date: " + datem);
            e.printStackTrace();
            return null;
        }
    }
    public static TypeP typeOf(String type) {
        if (type == null) return null;
        try {
            return TypeP.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("Unknown vehicle type: " + type);
            return null;
        }
    }

    public void addVehicule(Vehicule vehicule) throws SQLException {
        String query = "INSERT INTO vehicule (matricule, datem, kilometrage, type) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = conx.prepareStatement(query)) {
            stmt.setString(1, vehicule.getMatricule());

            Date formattedDate = vehicule.getDatem();
            if (formattedDate != null) {
                LocalDate localDate = LocalDate.parse(new SimpleDateFormat("yyyy-MM-dd").format(formattedDate));
                stmt.setString(2, localDate.toString());
            } else {
                stmt.setNull(2, Types.DATE);
            }

            stmt.setObject(3, vehicule.getKilometrage(), Types.INTEGER);
            stmt.setString(4, vehicule.getType() != null ? vehicule.getType().toString() : null);

            stmt.executeUpdate();
        }
    }

    public void updateVehicule(Vehicule vehicule) throws SQLException {
        String query = "UPDATE vehicule SET datem = ?, kilometrage = ?, type = ? WHERE matricule = ?";

        try (PreparedStatement stmt = conx.prepareStatement(query)) {
            stmt.setString(1, DB_DATE_FORMAT.format(vehicule.getDatem()));
            stmt.setInt(2, vehicule.getKilometrage());
            stmt.setString(3, vehicule.getType() != null ? vehicule.getType().toString() : null);
            stmt.setString(4, vehicule.getMatricule());

            stmt.executeUpdate();
        }
    }

    public void deleteVehicule(String matricule) throws SQLException {
        String query = "DELETE FROM vehicule WHERE matricule = ?";

        try (PreparedStatement stmt = conx.prepareStatement(query)) {
            stmt.setString(1, matricule);
            stmt.executeUpdate();
        }
    }

    public Vehicule getVehiculeByMatricule(String matricule) throws SQLException {
        String query = "SELECT * FROM vehicule WHERE matricule = ?";

        try (PreparedStatement stmt = conx.prepareStatement(query)) {
            stmt.setString(1, matricule);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Vehicule(
                        rs.getString("matricule"),
                        StringToDate(rs.getString("datem")),
                        rs.getInt("kilometrage"),
                        typeOf(rs.getString("type"))
                );
            }
        }
        return null;
    }
}
