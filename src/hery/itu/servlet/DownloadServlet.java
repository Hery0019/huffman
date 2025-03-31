package hery.itu.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/download")
public class DownloadServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String data = request.getParameter("data");
        if (data == null || data.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Données non disponibles.");
            return;
        }

        response.setContentType("text/plain");
        response.setHeader("Content-Disposition", "attachment; filename=huffman_encoded.txt");

        PrintWriter out = response.getWriter();
        out.print(data);
        out.flush();
        out.close();
    }
}
