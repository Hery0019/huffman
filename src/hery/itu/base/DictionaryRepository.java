package hery.itu.base;

import hery.itu.huffman.DictionaryValidator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

/** Accès à la table {@code dico} : lecture triée, insertion validée, remise à zéro. */
public class DictionaryRepository {

    private static final String SELECT_ALL = "SELECT caractere, code FROM dico ORDER BY caractere";
    private static final String INSERT = "INSERT INTO dico (caractere, code) VALUES (?, CAST(? AS BIT VARYING))";
    private static final String CLEAR = "TRUNCATE TABLE dico RESTART IDENTITY";
    /** Sérialise les insertions : la vérification de préfixe et l'INSERT doivent voir le même état. */
    private static final String LOCK = "LOCK TABLE dico IN SHARE ROW EXCLUSIVE MODE";

    /** Dictionnaire complet, trié par symbole. */
    public SortedMap<String, String> findAll() throws SQLException {
        try (Connection connection = Database.open()) {
            return readAll(connection);
        }
    }

    /**
     * Insère une entrée après validation (un seul point de code, code binaire, ni doublon ni préfixe).
     * Les contraintes UNIQUE de la table restent le filet de sécurité.
     *
     * @throws DictionaryException si l'entrée viole une règle du dictionnaire
     */
    public void insert(String symbol, String code) throws SQLException, DictionaryException {
        try (Connection connection = Database.open()) {
            connection.setAutoCommit(false);
            try {
                try (Statement lock = connection.createStatement()) {
                    lock.execute(LOCK);
                }
                Optional<DictionaryValidator.Problem> problem =
                        DictionaryValidator.check(symbol, code, readAll(connection));
                if (problem.isPresent()) {
                    throw new DictionaryException(problem.get());
                }
                try (PreparedStatement statement = connection.prepareStatement(INSERT)) {
                    statement.setString(1, symbol);
                    statement.setString(2, code);
                    statement.executeUpdate();
                }
                connection.commit();
            } catch (SQLException | DictionaryException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    public void clear() throws SQLException {
        try (Connection connection = Database.open();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(CLEAR);
        }
    }

    private static SortedMap<String, String> readAll(Connection connection) throws SQLException {
        SortedMap<String, String> dictionary = new TreeMap<>();
        try (PreparedStatement statement = connection.prepareStatement(SELECT_ALL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                // BIT VARYING est lu comme une chaîne de 0 et de 1
                dictionary.put(resultSet.getString("caractere"), resultSet.getString("code"));
            }
        }
        return dictionary;
    }
}
