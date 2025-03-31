package hery.itu.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.Gson;

import hery.itu.huffman.*;

@WebServlet("/huffman")
public class HuffmanServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String text = request.getParameter("text");

        // Calcul des fréquences
        Map<Character, Integer> frequencyMap = new HashMap<>();
        for (char c : text.toCharArray()) {
            frequencyMap.put(c, frequencyMap.getOrDefault(c, 0) + 1);
        }

        // Construction de l'arbre Huffman
        HuffmanTree tree = new HuffmanTree();
        tree.buildTree(frequencyMap);
        Map<Character, String> huffmanCodes = tree.generateCodes();

        // Encodage
        HuffmanEncoder encoder = new HuffmanEncoder();
        encoder.setCodes(huffmanCodes);
        String encodedText = encoder.encode(text);

        // Décodage
        HuffmanDecoder decoder = new HuffmanDecoder(tree.getRoot());
        String decodedText = decoder.decode(encodedText);

        // Stocker les résultats dans la requête
        request.setAttribute("originalText", text);
        request.setAttribute("encodedText", encodedText);
        request.setAttribute("decodedText", decodedText);
        request.setAttribute("frequencyMap", frequencyMap);
        request.setAttribute("huffmanCodes", huffmanCodes);
        request.setAttribute("huffmanTreeJson", tree.toJson());

        request.getRequestDispatcher("result.jsp").forward(request, response);
    }
}

