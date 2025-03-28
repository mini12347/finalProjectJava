package DAO;

import Entities.Moniteur;
import Entities.Vehicule;
import Entities.Disponibility;
import Entities.Hours;

import java.sql.*;
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class MoniteurDAO {
    private Connection conn;

    public MoniteurDAO(Connection conn) {
        this.conn = conn;
    }

    // Ajouter un moniteur dans la base de données
    public void ajouterMoniteur(Moniteur moniteur) throws SQLException {
        // Insertion du moniteur
        String sqlMoniteur = "INSERT INTO moniteur (cin, nom, prenom, adresse, mail, numTelephone, dateNaissance, vehicule) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement psMoniteur = conn.prepareStatement(sqlMoniteur)) {
            psMoniteur.setInt(1, moniteur.getCIN());
            psMoniteur.setString(2, moniteur.getNom());
            psMoniteur.setString(3, moniteur.getPrenom());
            psMoniteur.setString(4, moniteur.getAdresse());
            psMoniteur.setString(5, moniteur.getMail());
            psMoniteur.setInt(6, moniteur.getNumTelephone());
            psMoniteur.setDate(7, new java.sql.Date(moniteur.getDateNaissance().getTime()));
            psMoniteur.setString(8, moniteur.getVehicule().getMatricule()); // Utilisation du matricule du véhicule
            psMoniteur.executeUpdate();
        }

        // Insertion de la disponibilité
        String sqlDisponibility = "INSERT INTO disponibility (cin, jour, start_hour, end_hour) VALUES (?, ?, ?, ?)";
        try (PreparedStatement psDisponibility = conn.prepareStatement(sqlDisponibility)) {
            for (Map.Entry<DayOfWeek, Hours> entry : moniteur.getHoraire().getDaysOfWeek().entrySet()) {
                psDisponibility.setInt(1, moniteur.getCIN());
                psDisponibility.setString(2, entry.getKey().name());
                psDisponibility.setInt(3, entry.getValue().getStarthour());
                psDisponibility.setInt(4, entry.getValue().getEndhour());
                psDisponibility.executeUpdate();
            }
        }
    }

    // Chercher un moniteur par CIN
    public Moniteur chercherMoniteur(int cin) throws SQLException {
        String sqlMoniteur = "SELECT * FROM moniteur WHERE cin = ?";
        try (PreparedStatement psMoniteur = conn.prepareStatement(sqlMoniteur)) {
            psMoniteur.setInt(1, cin);
            try (ResultSet rsMoniteur = psMoniteur.executeQuery()) {
                if (rsMoniteur.next()) {
                    // Récupération du véhicule
                    Vehicule vehicule = new Vehicule(
                            rsMoniteur.getString("vehicule"), // ID du véhicule ou matricule
                            null, // Ajoutez ici d'autres propriétés si nécessaire
                            0,   // Si vous avez besoin d'autres informations
                            null  // Idem ici
                    );

                    // Récupération des disponibilités
                    Map<DayOfWeek, Hours> disponibilites = new HashMap<>();
                    String sqlDisponibility = "SELECT jour, start_hour, end_hour FROM disponibility WHERE cin = ?";
                    try (PreparedStatement psDisponibility = conn.prepareStatement(sqlDisponibility)) {
                        psDisponibility.setInt(1, cin);
                        try (ResultSet rsDisponibility = psDisponibility.executeQuery()) {
                            while (rsDisponibility.next()) {
                                String jourStr = rsDisponibility.getString("jour");
                                DayOfWeek jour;
                                try {
                                    jour = DayOfWeek.valueOf(jourStr.toUpperCase());
                                } catch (IllegalArgumentException e) {
                                    // Si ce n'est pas un enum valide, essayer de convertir les noms français
                                    switch(jourStr.toUpperCase()) {
                                        case "LUNDI": jour = DayOfWeek.MONDAY; break;
                                        case "MARDI": jour = DayOfWeek.TUESDAY; break;
                                        case "MERCREDI": jour = DayOfWeek.WEDNESDAY; break;
                                        case "JEUDI": jour = DayOfWeek.THURSDAY; break;
                                        case "VENDREDI": jour = DayOfWeek.FRIDAY; break;
                                        case "SAMEDI": jour = DayOfWeek.SATURDAY; break;
                                        case "DIMANCHE": jour = DayOfWeek.SUNDAY; break;
                                        default: throw new IllegalArgumentException("Jour invalide: " + jourStr);
                                    }
                                }
                                int start = rsDisponibility.getInt("start_hour");
                                int end = rsDisponibility.getInt("end_hour");

                                Hours horaire = new Hours(start, end);
                                disponibilites.put(jour, horaire);
                            }
                        }
                    }

                    return new Moniteur(
                            rsMoniteur.getInt("cin"),
                            rsMoniteur.getString("nom"),
                            rsMoniteur.getString("prenom"),
                            rsMoniteur.getString("adresse"),
                            rsMoniteur.getString("mail"),
                            rsMoniteur.getInt("numTelephone"),
                            rsMoniteur.getDate("dateNaissance"),
                            vehicule,
                            new Disponibility(disponibilites)
                    );
                }
            }
        }
        return null;
    }

    // Supprimer un moniteur par CIN
    public void supprimerMoniteur(int cin) throws SQLException {
        String sqlDisponibility = "DELETE FROM disponibility WHERE cin = ?";
        try (PreparedStatement psDisponibility = conn.prepareStatement(sqlDisponibility)) {
            psDisponibility.setInt(1, cin);
            psDisponibility.executeUpdate();
        }

        String sqlMoniteur = "DELETE FROM moniteur WHERE cin = ?";
        try (PreparedStatement psMoniteur = conn.prepareStatement(sqlMoniteur)) {
            psMoniteur.setInt(1, cin);
            psMoniteur.executeUpdate();
        }
    }

    // Afficher tous les moniteurs
    public List<Moniteur> afficherTousLesMoniteurs() throws SQLException {
        List<Moniteur> moniteurs = new ArrayList<>();
        String sqlMoniteur = "SELECT * FROM moniteur";
        try (PreparedStatement psMoniteur = conn.prepareStatement(sqlMoniteur);
             ResultSet rsMoniteur = psMoniteur.executeQuery()) {
            while (rsMoniteur.next()) {
                // Récupération du véhicule
                Vehicule vehicule = new Vehicule(
                        rsMoniteur.getString("vehicule"),
                        null, // Ajoutez des champs supplémentaires si nécessaire
                        0,
                        null
                );

                // Récupération des disponibilités
                Map<DayOfWeek, Hours> disponibilites = new HashMap<>();
                String sqlDisponibility = "SELECT jour, start_hour, end_hour FROM disponibility WHERE cin = ?";
                try (PreparedStatement psDisponibility = conn.prepareStatement(sqlDisponibility)) {
                    psDisponibility.setInt(1, rsMoniteur.getInt("cin"));
                    try (ResultSet rsDisponibility = psDisponibility.executeQuery()) {
                        while (rsDisponibility.next()) {
                            String jourStr = rsDisponibility.getString("jour");
                            DayOfWeek jour;
                            try {
                                jour = DayOfWeek.valueOf(jourStr.toUpperCase());
                            } catch (IllegalArgumentException e) {
                                // Si ce n'est pas un enum valide, essayer de convertir les noms français
                                switch(jourStr.toUpperCase()) {
                                    case "LUNDI": jour = DayOfWeek.MONDAY; break;
                                    case "MARDI": jour = DayOfWeek.TUESDAY; break;
                                    case "MERCREDI": jour = DayOfWeek.WEDNESDAY; break;
                                    case "JEUDI": jour = DayOfWeek.THURSDAY; break;
                                    case "VENDREDI": jour = DayOfWeek.FRIDAY; break;
                                    case "SAMEDI": jour = DayOfWeek.SATURDAY; break;
                                    case "DIMANCHE": jour = DayOfWeek.SUNDAY; break;
                                    default: throw new IllegalArgumentException("Jour invalide: " + jourStr);
                                }
                            }
                            int start = rsDisponibility.getInt("start_hour");
                            int end = rsDisponibility.getInt("end_hour");

                            Hours horaire = new Hours(start, end);
                            disponibilites.put(jour, horaire);
                        }
                    }
                }

                moniteurs.add(new Moniteur(
                        rsMoniteur.getInt("cin"),
                        rsMoniteur.getString("nom"),
                        rsMoniteur.getString("prenom"),
                        rsMoniteur.getString("adresse"),
                        rsMoniteur.getString("mail"),
                        rsMoniteur.getInt("numTelephone"),
                        rsMoniteur.getDate("dateNaissance"),
                        vehicule,
                        new Disponibility(disponibilites)
                ));
            }
        }
        return moniteurs;
    }
}