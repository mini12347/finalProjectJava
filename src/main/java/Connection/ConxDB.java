package Connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConxDB {
    private static Connection connexion;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/projectjava";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // mets ton mot de passe si nécessaire

    // Obtenir une connexion active
    public static Connection getInstance() {
        try {
            if (connexion == null || connexion.isClosed() || !connexion.isValid(2)) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connexion = DriverManager.getConnection(DB_URL, USER, PASSWORD);
                System.out.println("✅ Connexion à la base de données établie !");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Pilote JDBC introuvable : " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("❌ Erreur SQL lors de la connexion : " + e.getMessage());
        }

        return connexion;
    }

    // Fermer proprement la connexion
    public static void closeConnection() {
        if (connexion != null) {
            try {
                if (!connexion.isClosed()) {
                    connexion.close();
                    System.out.println("🔌 Connexion à la base de données fermée !");
                }
            } catch (SQLException e) {
                System.err.println("❌ Erreur lors de la fermeture : " + e.getMessage());
            } finally {
                connexion = null;
            }
        }
    }
}
