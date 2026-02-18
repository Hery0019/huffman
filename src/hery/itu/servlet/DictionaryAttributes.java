package hery.itu.servlet;

import hery.itu.base.DictionaryRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Charge le dictionnaire dans la requête pour les vues du parcours manuel, en signalant une base injoignable. */
final class DictionaryAttributes {

    static final String DICTIONARY = "huffmanDictionary";
    static final String DB_ERROR = "dbError";

    private static final Logger LOG = Logger.getLogger(DictionaryAttributes.class.getName());

    private DictionaryAttributes() {}

    /**
     * @return le dictionnaire chargé, ou une table vide si la base est injoignable
     *         (l'attribut {@value #DB_ERROR} est alors positionné pour la vue)
     */
    static Map<String, String> load(DictionaryRepository repository, HttpServletRequest request) {
        Map<String, String> dictionary;
        try {
            dictionary = repository.findAll();
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Lecture du dictionnaire impossible", e);
            dictionary = new TreeMap<>();
            request.setAttribute(DB_ERROR, Boolean.TRUE);
        }
        request.setAttribute(DICTIONARY, dictionary);
        return dictionary;
    }
}
