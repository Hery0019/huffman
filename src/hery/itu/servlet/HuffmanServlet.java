package hery.itu.servlet;

import com.google.gson.Gson;
import hery.itu.huffman.HuffmanDecoder;
import hery.itu.huffman.HuffmanEncoder;
import hery.itu.huffman.HuffmanTree;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/** Parcours automatique : fréquences → arbre → codes → encodage → décodage de contrôle. */
@WebServlet("/huffman")
public class HuffmanServlet extends HttpServlet {

    private static final String RESULT_VIEW = "/WEB-INF/views/result.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/index.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String text = request.getParameter("text");
        if (text == null || text.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }

        HuffmanTree tree = HuffmanTree.fromText(text);
        Map<Character, String> huffmanCodes = tree.generateCodes();
        String encodedText = new HuffmanEncoder(huffmanCodes).encode(text);
        String decodedText = new HuffmanDecoder(tree.getRoot()).decode(encodedText);

        request.setAttribute("originalText", text);
        request.setAttribute("encodedText", encodedText);
        request.setAttribute("decodedText", decodedText);
        request.setAttribute("frequencyMap", HuffmanTree.countFrequencies(text));
        request.setAttribute("huffmanCodes", huffmanCodes);
        // Sérialisation JSON de l'arbre pour le rendu D3 : préoccupation de présentation, donc ici et non dans le domaine.
        request.setAttribute("huffmanTreeJson", new Gson().toJson(tree.getRoot()));

        request.getRequestDispatcher(RESULT_VIEW).forward(request, response);
    }
}
