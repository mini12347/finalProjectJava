package Connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConxDB {
    private static final String URL = "jdbc:mysql://localhost:3306/auto_ecole";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static Connection connection = null;

    /**
     * Pattern Singleton pour la connexion à la base de données
     * Retourne toujours la même instance de connexion, ou en crée une nouvelle si nécessaire
     */
    public static synchronized Connection getInstance() {
        try {
            if (connection == null || connection.isClosed()) {
                // Chargement explicite du driver
                Class.forName("com.mysql.cj.jdbc.Driver");

                // Création d'une nouvelle connexion
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                connection.setAutoCommit(true);
                System.out.println("✅ Connexion réussie !");
            }
            return connection;
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion à la base de données: " + e.getMessage());
            // Créer une nouvelle connexion en cas d'échec
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                return connection;
            } catch (Exception retryEx) {
                System.err.println("❌ Échec de la nouvelle tentative de connexion: " + retryEx.getMessage());
                throw new RuntimeException("Impossible de se connecter à la base de données", retryEx);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver MySQL non trouvé: " + e.getMessage());
            throw new RuntimeException("Driver MySQL non trouvé", e);
        }
    }

    /**
     * Ferme la connexion à la base de données si elle est ouverte
     * Cette méthode ne doit être appelée qu'à la fin de l'application
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                connection = null;
                System.out.println("Connexion fermée");
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la fermeture de la connexion: " + e.getMessage());
        }
    }
}