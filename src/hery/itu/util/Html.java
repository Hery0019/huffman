package hery.itu.util;

/**
 * Aides d'affichage pour les JSP : échappement HTML et rendu des symboles / bits.
 * Aucune logique métier ici — uniquement de la présentation.
 */
public final class Html {

    /** Au-delà de ce nombre de bits, le reste est rendu sans coloration (limite la taille du DOM). */
    private static final int COLORED_BITS_MAX = 6000;

    private Html() {}

    /** Échappe une valeur pour l'insérer dans du HTML (texte ou attribut entre guillemets). */
    public static String esc(Object value) {
        if (value == null) return "";
        String s = String.valueOf(value);
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#39;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    /** Représentation lisible d'un symbole : les caractères invisibles sont rendus visibles. */
    public static String symbol(Object value) {
        if (value == null) return "";
        String s = String.valueOf(value);
        return switch (s) {
            case " " -> "␣";  // ␣ espace
            case "\n" -> "↵"; // ↵ retour à la ligne
            case "\r" -> "␍"; // ␍ retour chariot
            case "\t" -> "⇥"; // ⇥ tabulation
            case "\0" -> "∅"; // ∅ nœud interne
            default -> esc(s);
        };
    }

    /**
     * Rend une chaîne de bits avec un span coloré par bit ({@code bit-0}, {@code bit-1}).
     * Tout autre caractère (ex. {@code ?} pour un symbole absent du dictionnaire) est marqué {@code bit-missing}.
     */
    public static String bits(String encoded) {
        if (encoded == null || encoded.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(encoded.length() * 24);
        int colored = Math.min(encoded.length(), COLORED_BITS_MAX);
        for (int i = 0; i < colored; i++) {
            char c = encoded.charAt(i);
            switch (c) {
                case '0' -> sb.append("<span class=\"bit-0\">0</span>");
                case '1' -> sb.append("<span class=\"bit-1\">1</span>");
                default -> sb.append("<span class=\"bit-missing\" title=\"Symbole absent du dictionnaire\">")
                             .append(esc(String.valueOf(c))).append("</span>");
            }
        }
        if (colored < encoded.length()) {
            sb.append(esc(encoded.substring(colored)));
        }
        return sb.toString();
    }
}
