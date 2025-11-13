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

@WebServlet("/clearDictionary")
public class ClearDictionaryServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Base database = new Base();
        Connection connection = database.getConnection();

        if (connection != null) {
            String sql = "TRUNCATE TABLE dico RESTART IDENTITY";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.executeUpdate();
                System.out.println("✅ Dictionnaire vidé avec succès !");
            } catch (SQLException e) {
                System.err.println("❌ Erreur SQL: " + e.getMessage());
                e.printStackTrace();
            } finally {
                database.closeConnection();
            }
        } else {
            System.err.println("❌ Connexion à la base de données échouée !");
        }

        // Rediriger vers la page après suppression
        response.sendRedirect("insertDictionary.jsp");
    }
}
