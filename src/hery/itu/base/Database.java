package hery.itu.base;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Ouverture d'une connexion PostgreSQL. La configuration vient de l'environnement,
 * jamais du code : HUFFMAN_DB_URL, HUFFMAN_DB_USER, HUFFMAN_DB_PASSWORD
 * (variables d'environnement ou propriétés système -D, ces dernières prioritaires).
 * Le mot de passe accepte aussi la variable standard PGPASSWORD.
 */
public final class Database {

    public static final String URL_SETTING = "HUFFMAN_DB_URL";
    public static final String USER_SETTING = "HUFFMAN_DB_USER";
    public static final String PASSWORD_SETTING = "HUFFMAN_DB_PASSWORD";

    private static final String DEFAULT_URL = "jdbc:postgresql://localhost:5432/huffman";
    private static final String DEFAULT_USER = "postgres";

    private Database() {}

    /**
     * @throws SQLException si la configuration est incomplète ou si la base est injoignable ;
     *         l'appelant décide quoi afficher, rien n'est avalé ici.
     */
    public static Connection open() throws SQLException {
        String url = setting(URL_SETTING, DEFAULT_URL);
        String user = setting(USER_SETTING, DEFAULT_USER);
        String password = setting(PASSWORD_SETTING, null);
        if (password == null) {
            password = setting("PGPASSWORD", null);
        }
        if (password == null) {
            throw new SQLException("Mot de passe de la base absent : définir " + PASSWORD_SETTING
                    + " (variable d'environnement ou propriété système)");
        }
        return DriverManager.getConnection(url, user, password);
    }

    private static String setting(String name, String fallback) {
        String value = System.getProperty(name);
        if (value == null || value.isBlank()) {
            value = System.getenv(name);
        }
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
