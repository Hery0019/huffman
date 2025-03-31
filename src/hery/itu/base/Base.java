package hery.itu.base;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Base {
    // Informations de connexion à la base de données
    private static final String URL = "jdbc:postgresql://localhost:5432/huffman";
    private static final String USER = "postgres";
    private static final String PASSWORD = "hery";
    
    private Connection connection;
    
    public Base() {
        try {
            // Chargement du driver PostgreSQL
            Class.forName("org.postgresql.Driver");
            
            // Établissement de la connexion
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connexion à la base de données établie avec succès.");
        } catch (ClassNotFoundException e) {
            System.err.println("Erreur: Driver PostgreSQL non trouvé.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Erreur de connexion à la base de données.");
            e.printStackTrace();
        }
    }
    
    public Connection getConnection() {
        return this.connection;
    }
    
    public void closeConnection() {
        if (this.connection != null) {
            try {
                this.connection.close();
                System.out.println("Connexion à la base de données fermée.");
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture de la connexion.");
                e.printStackTrace();
            }
        }
    }
    
    // Exemple d'utilisation
    public static void main(String[] args) {
        Base base = new Base();
        
        // Utiliser la connexion pour exécuter des requêtes...
        // Connection conn = base.getConnection();
        
        // N'oubliez pas de fermer la connexion quand vous avez fini
        base.closeConnection();
    }
}