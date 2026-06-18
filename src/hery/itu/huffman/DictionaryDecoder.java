package hery.itu.huffman;

import java.util.Map;

/**
 * Décode une suite de bits avec un dictionnaire saisi à la main (symbole → code), en suivant un arbre
 * de préfixes reconstruit à partir des codes. Le décodage n'est possible que si aucun code n'est le
 * préfixe d'un autre : un dictionnaire ambigu est refusé à la construction.
 */
public final class DictionaryDecoder {

    /**
     * Résultat du décodage. En cas d'erreur, {@code text} contient ce qui a pu être décodé avant,
     * et {@code errorPosition} l'indice (à partir de 0) du premier bit qui n'a pas pu l'être.
     */
    public record Result(String text, int symbolCount, int errorPosition, String error) {
        public boolean isOk() {
            return error == null;
        }
    }

    private static final class Node {
        Node zero;
        Node one;
        String symbol;
    }

    private final Node root = new Node();
    private final int size;

    /** @throws IllegalArgumentException si un code n'est pas binaire, ou si le dictionnaire est ambigu */
    public DictionaryDecoder(Map<String, String> codes) {
        for (Map.Entry<String, String> entry : codes.entrySet()) {
            insert(entry.getKey(), entry.getValue());
        }
        this.size = codes.size();
    }

    private void insert(String symbol, String code) {
        if (code == null || !code.matches("[01]+")) {
            throw new IllegalArgumentException("Code invalide pour « " + symbol + " » : " + code);
        }
        Node node = root;
        for (int i = 0; i < code.length(); i++) {
            if (node.symbol != null) {
                throw new IllegalArgumentException("Dictionnaire ambigu : le code de « " + node.symbol
                        + " » est un préfixe de celui de « " + symbol + " »");
            }
            if (code.charAt(i) == '0') {
                if (node.zero == null) node.zero = new Node();
                node = node.zero;
            } else {
                if (node.one == null) node.one = new Node();
                node = node.one;
            }
        }
        if (node.symbol != null) {
            throw new IllegalArgumentException("Dictionnaire ambigu : « " + node.symbol + " » et « " + symbol
                    + " » ont le même code");
        }
        if (node.zero != null || node.one != null) {
            throw new IllegalArgumentException("Dictionnaire ambigu : le code de « " + symbol
                    + " » est un préfixe d'un autre code");
        }
        node.symbol = symbol;
    }

    public Result decode(String bits) {
        if (size == 0) {
            return new Result("", 0, 0, "Le dictionnaire est vide.");
        }
        StringBuilder text = new StringBuilder();
        int count = 0;
        int codeStart = 0;
        Node node = root;
        for (int i = 0; i < bits.length(); i++) {
            char bit = bits.charAt(i);
            if (bit != '0' && bit != '1') {
                return new Result(text.toString(), count, i,
                        "Caractère « " + bit + " » inattendu à la position " + (i + 1) + " : seuls 0 et 1 sont acceptés.");
            }
            node = (bit == '0') ? node.zero : node.one;
            if (node == null) {
                return new Result(text.toString(), count, codeStart,
                        "Les bits à partir de la position " + (codeStart + 1) + " ne correspondent à aucun code.");
            }
            if (node.symbol != null) {
                text.append(node.symbol);
                count++;
                node = root;
                codeStart = i + 1;
            }
        }
        if (node != root) {
            return new Result(text.toString(), count, codeStart,
                    "Séquence incomplète : les " + (bits.length() - codeStart) + " derniers bits ne forment pas un code entier.");
        }
        return new Result(text.toString(), count, -1, null);
    }
}
