package hery.itu.servlet;

import hery.itu.base.DictionaryRepository;
import hery.itu.base.DictionaryRepository.Dictionary;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Résout le dictionnaire courant et charge ses entrées dans la requête pour les vues du parcours manuel.
 * Le dictionnaire courant vient du paramètre {@code d}, sinon du cookie posé lors du dernier choix,
 * sinon du plus ancien (créé au besoin). Une base injoignable est signalée à la vue, jamais avalée.
 */
final class DictionaryAttributes {

    static final String DICTIONARY = "huffmanDictionary";
    static final String DICTIONARIES = "dictionaries";
    static final String CURRENT = "currentDictionary";
    static final String DB_ERROR = "dbError";
    static final String PARAMETER = "d";
    static final String COOKIE = "dictionnaire";

    private static final int COOKIE_MAX_AGE = 365 * 24 * 3600;
    private static final Logger LOG = Logger.getLogger(DictionaryAttributes.class.getName());

    private DictionaryAttributes() {}

    /**
     * @return les entrées du dictionnaire courant, ou une table vide si la base est injoignable
     *         (l'attribut {@value #DB_ERROR} est alors positionné pour la vue)
     */
    static Map<String, String> load(DictionaryRepository repository, HttpServletRequest request,
                                    HttpServletResponse response) {
        try {
            List<Dictionary> all = repository.listDictionaries();
            if (all.isEmpty()) {
                all = List.of(repository.defaultDictionary());
            }
            Dictionary current = resolve(all, request);
            remember(request, response, current);
            Map<String, String> entries = repository.findAll(current.id());
            request.setAttribute(DICTIONARIES, all);
            request.setAttribute(CURRENT, current);
            request.setAttribute(DICTIONARY, entries);
            return entries;
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Lecture du dictionnaire impossible", e);
            request.setAttribute(DB_ERROR, Boolean.TRUE);
            request.setAttribute(DICTIONARIES, List.of());
            request.setAttribute(DICTIONARY, new TreeMap<>());
            return new TreeMap<>();
        }
    }

    /** Pour les POST : l'identifiant du dictionnaire courant, sans charger ses entrées. */
    static int currentId(DictionaryRepository repository, HttpServletRequest request) throws SQLException {
        Integer requested = requestedId(request);
        if (requested != null && repository.findDictionary(requested).isPresent()) {
            return requested;
        }
        return repository.defaultDictionary().id();
    }

    /** Identifiant demandé par le paramètre {@code d}, sinon par le cookie ; {@code null} si aucun. */
    static Integer requestedId(HttpServletRequest request) {
        Integer fromParameter = parse(request.getParameter(PARAMETER));
        if (fromParameter != null) {
            return fromParameter;
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (COOKIE.equals(cookie.getName())) {
                    return parse(cookie.getValue());
                }
            }
        }
        return null;
    }

    private static Dictionary resolve(List<Dictionary> all, HttpServletRequest request) {
        Integer requested = requestedId(request);
        if (requested != null) {
            for (Dictionary dictionary : all) {
                if (dictionary.id() == requested) {
                    return dictionary;
                }
            }
        }
        return all.get(0);
    }

    private static void remember(HttpServletRequest request, HttpServletResponse response, Dictionary current) {
        Cookie cookie = new Cookie(COOKIE, String.valueOf(current.id()));
        String path = request.getContextPath();
        cookie.setPath(path == null || path.isEmpty() ? "/" : path);
        cookie.setMaxAge(COOKIE_MAX_AGE);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    private static Integer parse(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
