package hery.itu.huffman;

import java.util.Map;

/** Encode un texte avec une table de codes issue de {@link HuffmanTree#generateCodes()}. */
public class HuffmanEncoder {
    private final Map<Character, String> huffmanCodes;

    public HuffmanEncoder(Map<Character, String> huffmanCodes) {
        this.huffmanCodes = huffmanCodes;
    }

    /**
     * @throws IllegalArgumentException si un caractère du texte n'a pas de code
     *         (la table ne provient pas de l'arbre de ce texte)
     */
    public String encode(String text) {
        StringBuilder encoded = new StringBuilder();
        for (char c : text.toCharArray()) {
            String code = huffmanCodes.get(c);
            if (code == null) {
                throw new IllegalArgumentException("Aucun code pour le caractère U+"
                        + String.format("%04X", (int) c));
            }
            encoded.append(code);
        }
        return encoded.toString();
    }
}
