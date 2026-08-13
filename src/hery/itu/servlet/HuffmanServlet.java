package hery.itu.servlet;

import hery.itu.huffman.CodeStatistics;
import hery.itu.huffman.HuffmanDecoder;
import hery.itu.huffman.HuffmanEncoder;
import hery.itu.huffman.HuffmanFile;
import hery.itu.huffman.HuffmanTree;
import hery.itu.huffman.ShannonFano;
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

        Map<Character, Integer> frequencyMap = HuffmanTree.countFrequencies(text);
        HuffmanTree tree = new HuffmanTree();
        tree.buildTree(frequencyMap);
        Map<Character, String> huffmanCodes = tree.generateCodes();
        String encodedText = new HuffmanEncoder(huffmanCodes).encode(text);
        String decodedText = new HuffmanDecoder(tree.getRoot()).decode(encodedText);

        // Taille réelle du fichier binaire : la chaîne de bits affichée est huit fois plus grosse que les bits.
        HuffmanFile.Compressed compressed = HuffmanFile.compress(text);

        request.setAttribute("originalText", text);
        request.setAttribute("encodedText", encodedText);
        request.setAttribute("decodedText", decodedText);
        request.setAttribute("frequencyMap", frequencyMap);
        request.setAttribute("huffmanCodes", huffmanCodes);
        request.setAttribute("statistics", CodeStatistics.of(frequencyMap, huffmanCodes));

        // Shannon-Fano sur les mêmes fréquences, pour comparer avec le code optimal.
        Map<Character, String> shannonFanoCodes = ShannonFano.codes(frequencyMap);
        request.setAttribute("shannonFanoCodes", shannonFanoCodes);
        request.setAttribute("shannonFanoStatistics", CodeStatistics.of(frequencyMap, shannonFanoCodes));
        request.setAttribute("textUtf8Bytes", HuffmanFile.utf8Size(text));
        request.setAttribute("fileBytes", compressed.totalBytes());
        request.setAttribute("fileTreeBits", compressed.treeBits());
        request.setAttribute("fileDataBits", compressed.dataBits());
        // Sérialisation JSON pour le rendu D3 : préoccupation de présentation, donc ici et non dans le domaine.
        // La trace (nœuds à plat + fusions) permet de rejouer la construction pas à pas.
        request.setAttribute("huffmanTreeTraceJson", TreeTraceJson.of(tree));

        request.getRequestDispatcher(RESULT_VIEW).forward(request, response);
    }
}
