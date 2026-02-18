package hery.itu.servlet;

import hery.itu.base.DictionaryRepository;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Vide le dictionnaire manuel. */
@WebServlet("/clearDictionary")
public class ClearDictionaryServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(ClearDictionaryServlet.class.getName());

    private final DictionaryRepository repository = new DictionaryRepository();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/insertDictionary");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String page = request.getContextPath() + "/insertDictionary";
        try {
            repository.clear();
            response.sendRedirect(page + "?cleared=1");
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Vidage du dictionnaire impossible", e);
            response.sendRedirect(page + "?error=database");
        }
    }
}
