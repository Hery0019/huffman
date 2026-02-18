package hery.itu.servlet;

import hery.itu.base.DictionaryRepository;
import hery.itu.huffman.DictionaryEncoder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/** Parcours manuel : encodage d'un texte avec le dictionnaire saisi. */
@WebServlet("/encodeText")
public class HuffmanEncodeServlet extends HttpServlet {

    private static final String VIEW = "/WEB-INF/views/encodeText.jsp";

    private final DictionaryRepository repository = new DictionaryRepository();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        DictionaryAttributes.load(repository, request);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String text = request.getParameter("text");
        if (text == null || text.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/encodeText");
            return;
        }

        Map<String, String> dictionary = DictionaryAttributes.load(repository, request);
        if (request.getAttribute(DictionaryAttributes.DB_ERROR) == null) {
            DictionaryEncoder.Result result = new DictionaryEncoder(dictionary).encode(text);
            request.setAttribute("submittedText", text);
            request.setAttribute("encodedText", result.encoded());
            request.setAttribute("missingSymbols", result.missingSymbols());
        }
        request.getRequestDispatcher(VIEW).forward(request, response);
    }
}
