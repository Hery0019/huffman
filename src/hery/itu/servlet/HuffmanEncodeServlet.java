package hery.itu.servlet;

import hery.itu.base.Base;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/encodeText")
public class HuffmanEncodeServlet extends HttpServlet {

    // Charger le dictionnaire depuis la base de données
    private Map<String, String> getHuffmanDictionary() {
        Map<String, String> dictionary = new HashMap<>();
        Base base = new Base();
    
        try (Connection connection = base.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT caractere, code FROM dico");
             ResultSet resultSet = statement.executeQuery()) {
    
            while (resultSet.next()) {
                String caractere = resultSet.getString("caractere");
                String code = resultSet.getString("code"); // Récupérer directement la valeur en tant que String
                dictionary.put(caractere, code);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur chargement Huffman: " + e.getMessage());
            e.printStackTrace();
        }
        return dictionary;
    }

    // GET : Charger le dictionnaire et afficher la page
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("huffmanDictionary", getHuffmanDictionary());
        request.getRequestDispatcher("encodeText.jsp").forward(request, response);
    }

    // POST : Encoder le texte
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String textToEncode = request.getParameter("text");
        Map<String, String> huffmanDictionary = getHuffmanDictionary();
        StringBuilder encodedText = new StringBuilder();

        for (char ch : textToEncode.toCharArray()) {
            encodedText.append(huffmanDictionary.getOrDefault(String.valueOf(ch), "?"));
        }

        request.setAttribute("encodedText", encodedText.toString());
        request.setAttribute("huffmanDictionary", huffmanDictionary);
        request.getRequestDispatcher("encodeText.jsp").forward(request, response);
    }
}
