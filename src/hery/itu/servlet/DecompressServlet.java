package hery.itu.servlet;

import hery.itu.huffman.HuffmanFile;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;

/** Lit un fichier HUF1 envoyé par formulaire et affiche le texte qu'il contient. */
@WebServlet("/decompress")
@MultipartConfig(maxFileSize = DecompressServlet.MAX_FILE_BYTES, maxRequestSize = 2 * DecompressServlet.MAX_FILE_BYTES,
        fileSizeThreshold = 256 * 1024)
public class DecompressServlet extends HttpServlet {

    static final long MAX_FILE_BYTES = 1024 * 1024;
    private static final String VIEW = "/WEB-INF/views/decompressed.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/index.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        byte[] file;
        String fileName;
        try {
            Part part = request.getPart("file");
            if (part == null || part.getSize() == 0) {
                fail(request, response, "Aucun fichier reçu : choisissez un fichier .huf.");
                return;
            }
            fileName = part.getSubmittedFileName();
            try (InputStream in = part.getInputStream()) {
                file = in.readAllBytes();
            }
        } catch (IllegalStateException e) {
            // Levée par le conteneur quand la limite de taille est dépassée.
            fail(request, response, "Fichier trop volumineux : " + (MAX_FILE_BYTES / 1024) + " Ko au maximum.");
            return;
        }

        String text;
        try {
            text = HuffmanFile.decompress(file);
        } catch (IllegalArgumentException e) {
            fail(request, response, "Fichier invalide : " + e.getMessage() + ".");
            return;
        }

        request.setAttribute("fileName", fileName == null || fileName.isBlank() ? "fichier.huf" : fileName);
        request.setAttribute("fileBytes", file.length);
        request.setAttribute("textBytes", HuffmanFile.utf8Size(text));
        request.setAttribute("decompressedText", text);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }

    private static void fail(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        request.setAttribute("errorMessage", message);
        request.getRequestDispatcher(VIEW).forward(request, response);
    }
}
