package hery.itu.huffman;

/** Décode une chaîne de bits en parcourant l'arbre : 0 à gauche, 1 à droite, symbole émis à chaque feuille. */
public class HuffmanDecoder {
    private final HuffmanNode root;

    public HuffmanDecoder(HuffmanNode root) {
        this.root = root;
    }

    /**
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

    private static char checkBit(char bit, int position) {
        if (bit != '0' && bit != '1') {
            throw new IllegalArgumentException("Bit invalide « " + bit + " » à la position " + position);
        }
        return bit;
    }
}
