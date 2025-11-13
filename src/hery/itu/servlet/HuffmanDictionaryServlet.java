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
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/insertDictionary")
public class HuffmanDictionaryServlet extends HttpServlet {
    private Map<String, String> huffmanDictionary = new HashMap<>();

    // Charger les valeurs de la base au démarrage
    @Override
    public void init() throws ServletException {
        loadDictionaryFromDB();
    }

    private void loadDictionaryFromDB() {
        huffmanDictionary.clear();
        Base database = new Base();
        Connection connection = database.getConnection();

        if (connection == null) {
            System.err.println("❌ Impossible de se connecter à la base.");
            return;
        }

        String sql = "SELECT caractere, code FROM dico";

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                String character = resultSet.getString("caractere");
                String huffmanCode = resultSet.getString("code"); // BIT VARYING est récupéré comme String

                huffmanDictionary.put(character, huffmanCode);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            database.closeConnection();
        }
    }

    // GET : Afficher le formulaire et la table
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("huffmanDictionary", huffmanDictionary);
        request.getRequestDispatcher("insertDictionary.jsp").forward(request, response);
    }

    // POST : Insérer dans la table `dico` et mettre à jour l'affichage
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String character = request.getParameter("character");
        String huffmanCode = request.getParameter("huffmanCode");

        if (character == null || huffmanCode == null || character.trim().isEmpty() || huffmanCode.trim().isEmpty()) {
            response.sendRedirect("insertDictionary?error=empty");
            return;
        }

        // Vérifier que `huffmanCode` est bien une séquence binaire valide
        if (!huffmanCode.matches("[01]+")) {
            response.sendRedirect("insertDictionary?error=invalid_bit_string");
            return;
        }

        Base database = new Base();
        Connection connection = database.getConnection();

        if (connection == null) {
            response.sendRedirect("insertDictionary?error=db_connection");
            return;
        }

        String sql = "INSERT INTO dico (caractere, code) VALUES (?, CAST(? AS BIT VARYING))";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, character.trim());
            statement.setString(2, huffmanCode.trim()); // Conversion explicite en BIT VARYING

            int rowsInserted = statement.executeUpdate();

            if (rowsInserted > 0) {
                System.out.println("✅ Insertion réussie !");
                loadDictionaryFromDB();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect("insertDictionary?error=sql");
            return;
        } finally {
            database.closeConnection();
        }

        response.sendRedirect("insertDictionary");
    }
}
