package hery.itu.base;

import hery.itu.huffman.DictionaryValidator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

/** Accès aux tables {@code dictionnaire} (dictionnaires nommés) et {@code dico} (leurs entrées). */
public class DictionaryRepository {

    /** Un dictionnaire nommé. */
    public record Dictionary(int id, String name) {}

    public static final String DEFAULT_NAME = "Principal";
    public static final int NAME_MAX_LENGTH = 50;

    private static final String SELECT_DICTIONARIES = "SELECT id, nom FROM dictionnaire ORDER BY id";
    private static final String SELECT_DICTIONARY = "SELECT id, nom FROM dictionnaire WHERE id = ?";
    private static final String INSERT_DICTIONARY = "INSERT INTO dictionnaire (nom) VALUES (?) RETURNING id";
    private static final String DELETE_DICTIONARY = "DELETE FROM dictionnaire WHERE id = ?";
    private static final String SELECT_ENTRIES =
            "SELECT caractere, code FROM dico WHERE dictionnaire_id = ? ORDER BY caractere";
    private static final String INSERT_ENTRY =
            "INSERT INTO dico (dictionnaire_id, caractere, code) VALUES (?, ?, CAST(? AS BIT VARYING))";
    private static final String CLEAR_ENTRIES = "DELETE FROM dico WHERE dictionnaire_id = ?";
    /** Sérialise les insertions : la vérification de préfixe et l'INSERT doivent voir le même état. */
    private static final String LOCK = "LOCK TABLE dico IN SHARE ROW EXCLUSIVE MODE";
    /** Code SQLSTATE d'une violation d'unicité. */
    private static final String UNIQUE_VIOLATION = "23505";

    // --- Dictionnaires ---

    /** Tous les dictionnaires, du plus ancien au plus récent. */
    public List<Dictionary> listDictionaries() throws SQLException {
        List<Dictionary> dictionaries = new ArrayList<>();
        try (Connection connection = Database.open();
             PreparedStatement statement = connection.prepareStatement(SELECT_DICTIONARIES);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                dictionaries.add(new Dictionary(resultSet.getInt("id"), resultSet.getString("nom")));
            }
        }
        return dictionaries;
    }

    public Optional<Dictionary> findDictionary(int id) throws SQLException {
        try (Connection connection = Database.open();
             PreparedStatement statement = connection.prepareStatement(SELECT_DICTIONARY)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? Optional.of(new Dictionary(resultSet.getInt("id"), resultSet.getString("nom")))
                        : Optional.empty();
            }
        }
    }

    /** Le dictionnaire le plus ancien ; « Principal » est créé s'il n'en existe aucun. */
    public Dictionary defaultDictionary() throws SQLException {
        List<Dictionary> all = listDictionaries();
        return all.isEmpty() ? createDictionary(DEFAULT_NAME) : all.get(0);
    }

    /**
     * @throws SQLException dont {@link #isDuplicateName(SQLException)} dit si le nom existe déjà
     */
    public Dictionary createDictionary(String name) throws SQLException {
        try (Connection connection = Database.open();
             PreparedStatement statement = connection.prepareStatement(INSERT_DICTIONARY)) {
            statement.setString(1, name);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return new Dictionary(resultSet.getInt(1), name);
            }
        }
    }

    /** Supprime le dictionnaire et ses entrées (cascade) ; {@code false} s'il n'existait pas. */
    public boolean deleteDictionary(int id) throws SQLException {
        try (Connection connection = Database.open();
             PreparedStatement statement = connection.prepareStatement(DELETE_DICTIONARY)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    public static boolean isDuplicateName(SQLException e) {
        return UNIQUE_VIOLATION.equals(e.getSQLState());
    }

    // --- Entrées ---

    /** Entrées d'un dictionnaire, triées par symbole. */
    public SortedMap<String, String> findAll(int dictionaryId) throws SQLException {
        try (Connection connection = Database.open()) {
            return readAll(connection, dictionaryId);
        }
    }

    /**
     * Insère une entrée après validation (un seul point de code, code binaire, ni doublon ni préfixe).
     * Les contraintes UNIQUE de la table restent le filet de sécurité.
     *
     * @throws DictionaryException si l'entrée viole une règle du dictionnaire
     */
    public void insert(int dictionaryId, String symbol, String code) throws SQLException, DictionaryException {
        try (Connection connection = Database.open()) {
            connection.setAutoCommit(false);
            try {
                try (Statement lock = connection.createStatement()) {
                    lock.execute(LOCK);
                }
                Optional<DictionaryValidator.Problem> problem =
                        DictionaryValidator.check(symbol, code, readAll(connection, dictionaryId));
                if (problem.isPresent()) {
                    throw new DictionaryException(problem.get());
                }
                try (PreparedStatement statement = connection.prepareStatement(INSERT_ENTRY)) {
                    statement.setInt(1, dictionaryId);
                    statement.setString(2, symbol);
                    statement.setString(3, code);
                    statement.executeUpdate();
                }
                connection.commit();
            } catch (SQLException | DictionaryException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    public void clear(int dictionaryId) throws SQLException {
        try (Connection connection = Database.open();
             PreparedStatement statement = connection.prepareStatement(CLEAR_ENTRIES)) {
            statement.setInt(1, dictionaryId);
            statement.executeUpdate();
        }
    }

    private static SortedMap<String, String> readAll(Connection connection, int dictionaryId) throws SQLException {
        SortedMap<String, String> dictionary = new TreeMap<>();
        try (PreparedStatement statement = connection.prepareStatement(SELECT_ENTRIES)) {
            statement.setInt(1, dictionaryId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    // BIT VARYING est lu comme une chaîne de 0 et de 1
                    dictionary.put(resultSet.getString("caractere"), resultSet.getString("code"));
                }
            }
        }
        return dictionary;
    }
}
