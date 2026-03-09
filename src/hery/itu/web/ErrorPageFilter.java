package hery.itu.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Dernier filet : toute exception non gérée est journalisée et remplacée par la page d'erreur
 * de l'application, au lieu de la page Tomcat par défaut qui expose la pile d'appels.
 * (Sans web.xml, c'est le moyen de déclarer une page d'erreur.)
 */
@WebFilter(urlPatterns = "/*")
public class ErrorPageFilter extends HttpFilter {

    public static final String ERROR_VIEW = "/WEB-INF/views/error.jsp";
    public static final String TITLE_ATTRIBUTE = "errorTitle";
    public static final String MESSAGE_ATTRIBUTE = "errorMessage";

    private static final Logger LOG = Logger.getLogger(ErrorPageFilter.class.getName());

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (IOException | ServletException | RuntimeException e) {
            LOG.log(Level.SEVERE, "Erreur non gérée : " + request.getMethod() + " " + request.getRequestURI(), e);
            if (response.isCommitted()) {
                throw e; // trop tard pour changer la réponse
            }
            response.reset();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            request.setAttribute(TITLE_ATTRIBUTE, "Erreur interne");
            request.setAttribute(MESSAGE_ATTRIBUTE,
                    "Une erreur inattendue est survenue. Le détail est dans le journal du serveur.");
            request.getRequestDispatcher(ERROR_VIEW).forward(request, response);
        }
    }
}
