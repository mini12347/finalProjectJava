package DAO;

import Connection.ConxDB;
import Entities.TypeP;
import Entities.Vehicule;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VehiculesDAO {
    private static final Logger LOGGER = Logger.getLogger(VehiculesDAO.class.getName());

    // Date formats
    private static final SimpleDateFormat DB_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DISPLAY_DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
    private static final DateTimeFormatter DB_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public List<Vehicule> getAllVehicules() {
        String query = "SELECT * FROM vehicule";
        List<Vehicule> vehicules = new ArrayList<>();

        try (Connection conn = ConxDB.getInstance();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                vehicules.add(extractVehiculeFromResultSet(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving all vehicles", e);
        }
        return vehicules;
    }

    public List<Vehicule> getVehiulesOrdered(String colname, String order) {
        if (!isValidColumnName(colname)) {
            throw new IllegalArgumentException("Invalid column name: " + colname);
        }

        if (!order.trim().equalsIgnoreCase("ASC") && !order.trim().equalsIgnoreCase("DESC")) {
            order = "ASC";
        }

        String query = "SELECT * FROM vehicule ORDER BY " + colname + " " + order;
        List<Vehicule> vehicules = new ArrayList<>();

        try (Connection conn = ConxDB.getInstance();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                vehicules.add(extractVehiculeFromResultSet(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving ordered vehicles", e);
        }
        return vehicules;
    }

    private boolean isValidColumnName(String columnName) {
        List<String> validColumns = List.of("matricule", "datem", "kilometrage", "type");
        return validColumns.contains(columnName.toLowerCase());
    }

    public void addVehicule(Vehicule vehicule) throws SQLException {
        String query = "INSERT INTO vehicule (matricule, datem, kilometrage, type) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            setVehiculeParameters(stmt, vehicule);
            stmt.executeUpdate();
        }
    }

    public void updateVehicule(Vehicule vehicule) throws SQLException {
        String query = "UPDATE vehicule SET datem = ?, kilometrage = ?, type = ? WHERE matricule = ?";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            setVehiculeParameters(stmt, vehicule);
            stmt.setString(4, vehicule.getMatricule());
            stmt.executeUpdate();
        }
    }

    public void deleteVehicule(String matricule) throws SQLException {
        try (Connection conn = ConxDB.getInstance()) {
            conn.setAutoCommit(false);
            try {
                deleteRelatedReparations(conn, matricule);

                String query = "DELETE FROM vehicule WHERE matricule = ?";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, matricule);
                    stmt.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private void deleteRelatedReparations(Connection conn, String matricule) throws SQLException {
        String query = "DELETE FROM reparation WHERE vehicule_immatriculation = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, matricule);
            stmt.executeUpdate();
        }
    }

    public Vehicule getVehiculeByMatricule(String matricule) {
        String query = "SELECT * FROM vehicule WHERE matricule = ?";

        try (Connection conn = ConxDB.getInstance();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, matricule);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractVehiculeFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving vehicle by matricule", e);
        }
        return null;
    }

    private Vehicule extractVehiculeFromResultSet(ResultSet rs) throws SQLException {
        String matricule = rs.getString("matricule");
        String datemStr = rs.getString("datem");
        Integer kilometrage = rs.getObject("kilometrage", Integer.class);
        String typeStr = rs.getString("type");

        Date datem = null;
        if (datemStr != null && !datemStr.isEmpty()) {
            try {
                datem = parseDate(datemStr);
            } catch (ParseException e) {
                LOGGER.log(Level.WARNING, "Error parsing date: " + datemStr, e);
            }
        }

        TypeP type = typeOf(typeStr);

        return new Vehicule(matricule, datem, kilometrage != null ? kilometrage : 0, type);
    }

    private void setVehiculeParameters(PreparedStatement stmt, Vehicule vehicule) throws SQLException {
        stmt.setString(1, vehicule.getMatricule());

        if (vehicule.getDatem() != null) {
            stmt.setString(2, DB_DATE_FORMAT.format(vehicule.getDatem()));
        } else {
            stmt.setNull(2, Types.DATE);
        }

        stmt.setObject(3, vehicule.getKilometrage(), Types.INTEGER);
        stmt.setString(4, vehicule.getType() != null ? vehicule.getType().toString() : null);
    }

    public static Date parseDate(String dateStr) throws ParseException {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }

        try {
            return DB_DATE_FORMAT.parse(dateStr);
        } catch (ParseException e1) {
            try {
                return DISPLAY_DATE_FORMAT.parse(dateStr);
            } catch (ParseException e2) {
                try {
                    LocalDate localDate = LocalDate.parse(dateStr, DB_FORMATTER);
                    return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                } catch (DateTimeParseException e3) {
                    try {
                        LocalDate localDate = LocalDate.parse(dateStr, DISPLAY_FORMATTER);
                        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                    } catch (DateTimeParseException e4) {
                        throw new ParseException("Unable to parse date: " + dateStr, 0);
                    }
                }
            }
        }
    }

    public static Date StringToDate(String datem) {
        try {
            return parseDate(datem);
        } catch (ParseException e) {
            LOGGER.log(Level.WARNING, "Error parsing date: " + datem, e);
            return null;
        }
    }

    public static TypeP typeOf(String type) {
        if (type == null || type.isEmpty()) return null;

        String normalizedType = type.trim().toUpperCase();

        try {
            return TypeP.valueOf(normalizedType);
        } catch (IllegalArgumentException e) {
            if (normalizedType.equals("MOTO") || normalizedType.equals("MOTORCYCLE")) {
                return TypeP.MOTO;
            } else if (normalizedType.equals("VOITURE") || normalizedType.equals("CAR")) {
                return TypeP.VOITURE;
            } else if (normalizedType.equals("CAMION") || normalizedType.equals("TRUCK")) {
                return TypeP.CAMION;
            }

            LOGGER.log(Level.WARNING, "Unknown vehicle type: " + type);
            return null;
        }
    }
}