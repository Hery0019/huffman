package hery.itu.servlet;

import hery.itu.base.DictionaryException;
import hery.itu.base.DictionaryRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Parcours manuel : affichage et alimentation du dictionnaire (table {@code dico}). */
@WebServlet("/insertDictionary")
public class HuffmanDictionaryServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(HuffmanDictionaryServlet.class.getName());
    private static final String VIEW = "/WEB-INF/views/insertDictionary.jsp";

    private final DictionaryRepository repository = new DictionaryRepository();

    /** GET : formulaire et table, lus en base à chaque affichage (pas de cache à invalider). */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        DictionaryAttributes.load(repository, request);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    /** POST : insertion d'une entrée, puis redirection avec le résultat dans l'URL (PRG). */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Pas de trim sur le symbole : l'espace est un caractère à part entière.
        String symbol = request.getParameter("character");
        String code = request.getParameter("huffmanCode");
        if (code != null) {
            code = code.strip();
        }

        String page = request.getContextPath() + "/insertDictionary";
        try {
            repository.insert(symbol, code);
            response.sendRedirect(page + "?added=1");
        } catch (DictionaryException e) {
            response.sendRedirect(page + "?error=" + e.getProblem().name().toLowerCase(Locale.ROOT));
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Insertion dans le dictionnaire impossible", e);
            response.sendRedirect(page + "?error=database");
        }
    }
}
