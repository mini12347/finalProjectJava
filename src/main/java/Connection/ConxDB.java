package Connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConxDB {
    private static Connection connexion;
    private static final String DB_URL = "jdbc:mysql://localhost:3307/finalproject";
    private static final String USER = "root";
    //private static final String PASS = "root";

    // Méthode pour obtenir la connexion
    public static Connection getInstance() {
        if (connexion == null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connexion = DriverManager.getConnection(DB_URL, USER,"");
                System.out.println("✅ Connexion réussie !");
            } catch (ClassNotFoundException | SQLException e) {
                System.err.println("❌ Erreur de connexion : " + e.getMessage());
            }
        }
        return connexion;
    }

    // Méthode pour fermer la connexion proprement
    public static void closeConnection() {
        if (connexion != null) {
            try {
                connexion.close();
                connexion = null;
                System.out.println("🔌 Connexion fermée !");
            } catch (SQLException e) {
                System.err.println("❌ Erreur lors de la fermeture de la connexion : " + e.getMessage());
            }
        }
    }
}
