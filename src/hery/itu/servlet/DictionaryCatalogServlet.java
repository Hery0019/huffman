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

/** Création et suppression des dictionnaires nommés. */
@WebServlet("/dictionaries")
public class DictionaryCatalogServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(DictionaryCatalogServlet.class.getName());

    private final DictionaryRepository repository = new DictionaryRepository();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/insertDictionary");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String page = request.getContextPath() + "/insertDictionary";
        String action = request.getParameter("action");
        try {
            if ("create".equals(action)) {
                create(request, response, page);
            } else if ("delete".equals(action)) {
                int dictionaryId = DictionaryAttributes.currentId(repository, request);
                repository.deleteDictionary(dictionaryId);
                // Le cookie pointe vers un dictionnaire disparu : la prochaine page retombe sur le plus ancien.
                response.sendRedirect(page + "?deleted=1");
            } else {
                response.sendRedirect(page);
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Gestion des dictionnaires impossible", e);
            response.sendRedirect(page + "?error=database");
        }
    }

    private void create(HttpServletRequest request, HttpServletResponse response, String page)
            throws IOException, SQLException {
        String name = request.getParameter("name");
        name = name == null ? "" : name.strip();
        if (name.isEmpty()) {
            response.sendRedirect(page + "?error=name_empty");
            return;
        }
        if (name.length() > DictionaryRepository.NAME_MAX_LENGTH) {
            response.sendRedirect(page + "?error=name_too_long");
            return;
        }
        try {
            DictionaryRepository.Dictionary created = repository.createDictionary(name);
            response.sendRedirect(page + "?d=" + created.id() + "&created=1");
        } catch (SQLException e) {
            if (DictionaryRepository.isDuplicateName(e)) {
                response.sendRedirect(page + "?error=name_taken");
                return;
            }
            throw e;
        }
    }
}
