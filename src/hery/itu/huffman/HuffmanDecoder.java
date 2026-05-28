package hery.itu.huffman;

/** Décode une suite de bits en parcourant l'arbre : 0 à gauche, 1 à droite, symbole émis à chaque feuille. */
public class HuffmanDecoder {

    /** Source de bits (0 ou 1), par exemple un flux binaire ; lève une exception si la source est épuisée. */
    @FunctionalInterface
    public interface BitSource {
        int nextBit();
    }

    private final HuffmanNode root;

    public HuffmanDecoder(HuffmanNode root) {
        this.root = root;
    }

    /**
     * Décode une chaîne de caractères '0' / '1' jusqu'au bout.
     *
     * @throws IllegalArgumentException si la chaîne contient autre chose que des 0 et des 1,
     *         ou si elle se termine au milieu d'un code
     */
    public String decode(String bits) {
        StringBuilder decoded = new StringBuilder();
        if (root == null) {
            if (bits.isEmpty()) {
                return "";
            }
            throw new IllegalArgumentException("Arbre vide : impossible de décoder");
        }

        if (root.isLeaf()) {
            // Un seul symbole : chaque bit "0" est une occurrence (voir HuffmanTree.generateCodes)
            for (int i = 0; i < bits.length(); i++) {
                checkBit(bits.charAt(i), i);
                decoded.append(root.getCharacter());
            }
            return decoded.toString();
        }

        HuffmanNode current = root;
        for (int i = 0; i < bits.length(); i++) {
            char bit = checkBit(bits.charAt(i), i);
            current = (bit == '0') ? current.getLeft() : current.getRight();
            if (current.isLeaf()) {
                decoded.append(current.getCharacter());
                current = root;
            }
        }
        if (current != root) {
            throw new IllegalArgumentException("Séquence incomplète : les derniers bits ne forment pas un code entier");
        }
        return decoded.toString();
    }

    /**
     * Décode exactement {@code symbolCount} symboles depuis une source de bits, puis s'arrête :
     * les bits de bourrage qui suivent dans un fichier ne sont pas lus.
     */
    public String decode(BitSource bits, int symbolCount) {
        if (symbolCount == 0) {
            return "";
        }
        if (root == null) {
            throw new IllegalArgumentException("Arbre vide : impossible de décoder");
        }
        StringBuilder decoded = new StringBuilder(symbolCount);
        for (int i = 0; i < symbolCount; i++) {
            HuffmanNode current = root;
            if (current.isLeaf()) {
                bits.nextBit(); // un bit par occurrence, comme à l'encodage
            } else {
                while (!current.isLeaf()) {
                    current = (bits.nextBit() == 0) ? current.getLeft() : current.getRight();
                }
            }
            decoded.append(current.getCharacter());
        }
        return decoded.toString();
    }

    private static char checkBit(char bit, int position) {
        if (bit != '0' && bit != '1') {
            throw new IllegalArgumentException("Bit invalide « " + bit + " » à la position " + position);
        }
        return bit;
    }
}
