package hery.itu.servlet;

import hery.itu.huffman.HuffmanFile;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Produit le fichier compressé binaire (format HUF1) d'un texte, en téléchargement. */
@WebServlet("/compress")
public class CompressServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/index.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String text = request.getParameter("text");
        if (text == null || text.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }
        byte[] file = HuffmanFile.compress(text).bytes();

        response.setContentType("application/octet-stream");
        response.setContentLength(file.length);
        response.setHeader("Content-Disposition", "attachment; filename=\"texte.huf\"");
        response.getOutputStream().write(file);
    }
}
