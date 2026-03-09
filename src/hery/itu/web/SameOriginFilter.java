package hery.itu.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

/**
 * Protection CSRF sans jeton ni session : un POST dont l'en-tête Origin (ou, à défaut, Referer)
 * ne correspond pas à l'origine du serveur est refusé. Les navigateurs envoient toujours Origin
 * sur un POST de formulaire ; une requête sans aucun des deux en-têtes (curl, script) est acceptée.
 * Derrière un mandataire qui réécrit l'hôte ou le schéma, adapter {@link #expectedOrigin}.
 */
@WebFilter(urlPatterns = "/*")
public class SameOriginFilter extends HttpFilter {

    private static final Logger LOG = Logger.getLogger(SameOriginFilter.class.getName());

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            String source = request.getHeader("Origin");
            if (source == null) {
                source = originOf(request.getHeader("Referer"));
            }
            String expected = expectedOrigin(request);
            if (source != null && !source.equalsIgnoreCase(expected)) {
                LOG.warning("POST refusé : origine " + source + " au lieu de " + expected
                        + " sur " + request.getRequestURI());
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                request.setAttribute(ErrorPageFilter.TITLE_ATTRIBUTE, "Requête refusée");
                request.setAttribute(ErrorPageFilter.MESSAGE_ATTRIBUTE,
                        "Ce formulaire a été envoyé depuis un autre site. Repartez de l'application.");
                request.getRequestDispatcher(ErrorPageFilter.ERROR_VIEW).forward(request, response);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    /** Origine du serveur telle que le navigateur la voit : schéma://hôte[:port hors 80/443]. */
    static String expectedOrigin(HttpServletRequest request) {
        return format(request.getScheme(), request.getServerName(), request.getServerPort());
    }

    /** Origine (schéma://hôte[:port]) d'une URL Referer, ou {@code null} si absente ou illisible. */
    static String originOf(String referer) {
        if (referer == null || referer.isBlank()) {
            return null;
        }
        try {
            URI uri = new URI(referer);
            if (uri.getScheme() == null || uri.getHost() == null) {
                return null;
            }
            return format(uri.getScheme(), uri.getHost(), uri.getPort());
        } catch (URISyntaxException e) {
            return null;
        }
    }

    private static String format(String scheme, String host, int port) {
        boolean defaultPort = port <= 0
                || ("http".equalsIgnoreCase(scheme) && port == 80)
                || ("https".equalsIgnoreCase(scheme) && port == 443);
        return scheme.toLowerCase() + "://" + host.toLowerCase() + (defaultPort ? "" : ":" + port);
    }
}
