package DAO;

import Entities.Vehicule;
import Entities.TypeP;
import Connection.ConxDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehiculeDAO {
    private Connection conn;

    // Constructeur qui initialise la connexion
    public VehiculeDAO() {
        this.conn = ConxDB.getInstance();
        if (this.conn == null) {
            throw new IllegalStateException("Erreur : Impossible d'obtenir une connexion à la base de données !");
        }
    }

    // Ajouter un véhicule dans la base de données
    public void ajouterVehicule(Vehicule vehicule) throws SQLException {
        String sql = "INSERT INTO vehicule (matricule, datem, kilometrage, type) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, vehicule.getMatricule());
            ps.setDate(2, new java.sql.Date(vehicule.getDatem().getTime()));
            ps.setInt(3, vehicule.getKilometrage());
            ps.setString(4, vehicule.getType().name()); // Conversion de l'énumération TypeP en String
            ps.executeUpdate();
        }
    }

    // Chercher un véhicule par matricule
    public Vehicule chercherVehicule(String matricule) throws SQLException {
        String sql = "SELECT * FROM vehicule WHERE matricule = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matricule);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TypeP type = TypeP.valueOf(rs.getString("type")); // Conversion String → Enum
                    return new Vehicule(
                            rs.getString("matricule"),
                            rs.getDate("datem"),
                            rs.getInt("kilometrage"),
                            type
                    );
                }
            }
        }
        return null;
    }

    // Supprimer un véhicule par matricule
    public void supprimerVehicule(String matricule) throws SQLException {
        String sql = "DELETE FROM vehicule WHERE matricule = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matricule);
            ps.executeUpdate();
        }
    }

    // Afficher tous les véhicules
    public List<Vehicule> afficherTousLesVehicules() throws SQLException {
        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT * FROM vehicule";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                TypeP type = TypeP.valueOf(rs.getString("type")); // Conversion String → Enum
                vehicules.add(new Vehicule(
                        rs.getString("matricule"),
                        rs.getDate("datem"),
                        rs.getInt("kilometrage"),
                        type
                ));
            }
        }
        return vehicules;
    }

    // Récupérer tous les véhicules
    public List<Vehicule> getAllVehicules() throws SQLException {
        if (this.conn == null) {
            throw new IllegalStateException("Erreur : La connexion à la base de données est NULL dans VehiculeDAO !");
        }

        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT * FROM vehicule";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                TypeP type = TypeP.valueOf(rs.getString("type"));
                vehicules.add(new Vehicule(
                        rs.getString("matricule"),
                        rs.getDate("datem"),
                        rs.getInt("kilometrage"),
                        type
                ));
            }
        }
        return vehicules;
    }
}
