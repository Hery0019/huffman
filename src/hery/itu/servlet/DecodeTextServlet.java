package hery.itu.servlet;

import hery.itu.base.DictionaryRepository;
import hery.itu.huffman.DictionaryDecoder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/** Parcours manuel : décodage d'une suite de bits avec le dictionnaire saisi. */
@WebServlet("/decodeText")
public class DecodeTextServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/decodeText.jsp";

    private final DictionaryRepository repository = new DictionaryRepository();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        DictionaryAttributes.load(repository, request, response);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String bits = request.getParameter("bits");
        if (bits == null || bits.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/decodeText");
            return;
        }
        // Les bits collés d'ailleurs sont souvent groupés par espaces ou retours à la ligne.
        bits = bits.replaceAll("\\s+", "");

        Map<String, String> dictionary = DictionaryAttributes.load(repository, request, response);
        request.setAttribute("submittedBits", bits);
        if (request.getAttribute(DictionaryAttributes.DB_ERROR) == null) {
            try {
                request.setAttribute("decodeResult", new DictionaryDecoder(dictionary).decode(bits));
            } catch (IllegalArgumentException e) {
                // Dictionnaire antérieur aux contraintes d'unicité et de préfixe : inutilisable tel quel.
                request.setAttribute("dictionaryError", e.getMessage());
            }
        }
        request.getRequestDispatcher(VIEW).forward(request, response);
    }
}
