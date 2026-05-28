package hery.itu.huffman;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

/**
 * Format de fichier compressé « HUF1 », pour mesurer et manipuler une vraie compression binaire
 * (le texte encodé affiché à l'écran est une chaîne de '0' et de '1', huit fois plus grosse que les bits réels).
 *
 * <pre>
 *  octets 0-3   : "HUF1"
 *  octets 4-7   : nombre de symboles du texte (entier 32 bits, gros-boutiste)
 *  puis un flux de bits :
 *    arbre en pré-ordre : 1 + 16 bits du caractère pour une feuille, 0 pour un nœud interne
 *    données           : les codes des symboles, dans l'ordre du texte
 *  complété de zéros jusqu'à l'octet.
 * </pre>
 *
 * Le texte est pris par unité UTF-16 ({@code char}), comme le reste du parcours automatique.
 */
public final class HuffmanFile {

    public static final byte[] MAGIC = {'H', 'U', 'F', '1'};
    public static final int HEADER_BYTES = 8;
    private static final int CHAR_BITS = 16;

    /** Le fichier produit et la place occupée par chaque partie, pour l'affichage. */
    public record Compressed(byte[] bytes, int symbolCount, int treeBits, int dataBits) {
        public int totalBytes() { return bytes.length; }
    }

    private HuffmanFile() {}

    public static Compressed compress(String text) {
        HuffmanTree tree = HuffmanTree.fromText(text);
        Map<Character, String> codes = tree.generateCodes();

        BitWriter out = new BitWriter();
        out.writeBytes(MAGIC);
        out.writeInt(text.length());

        int treeBits = 0;
        if (tree.getRoot() != null) {
            treeBits = writeTree(tree.getRoot(), out);
        }
        int dataBits = 0;
        for (char c : text.toCharArray()) {
            String code = codes.get(c);
            for (int i = 0; i < code.length(); i++) {
                out.writeBit(code.charAt(i) == '1' ? 1 : 0);
            }
            dataBits += code.length();
        }
        return new Compressed(out.toByteArray(), text.length(), treeBits, dataBits);
    }

    /**
     * @throws IllegalArgumentException si le contenu n'est pas un fichier HUF1 valide
     */
    public static String decompress(byte[] file) {
        if (file.length < HEADER_BYTES || !Arrays.equals(Arrays.copyOf(file, MAGIC.length), MAGIC)) {
            throw new IllegalArgumentException("Ce n'est pas un fichier HUF1");
        }
        BitReader in = new BitReader(file, MAGIC.length);
        int symbolCount = in.readInt();
        if (symbolCount < 0) {
            throw new IllegalArgumentException("Nombre de symboles invalide");
        }
        if (symbolCount == 0) {
            return "";
        }
        HuffmanNode root = readTree(in, 0);
        return new HuffmanDecoder(root).decode(in::readBit, symbolCount);
    }

    /** Taille du texte en octets UTF-8 : la référence honnête pour mesurer le gain. */
    public static int utf8Size(String text) {
        return text.getBytes(StandardCharsets.UTF_8).length;
    }

    private static int writeTree(HuffmanNode node, BitWriter out) {
        if (node.isLeaf()) {
            out.writeBit(1);
            out.writeBits(node.getCharacter(), CHAR_BITS);
            return 1 + CHAR_BITS;
        }
        out.writeBit(0);
        return 1 + writeTree(node.getLeft(), out) + writeTree(node.getRight(), out);
    }

    private static HuffmanNode readTree(BitReader in, int depth) {
        if (depth > CHAR_BITS * 4096) {
            throw new IllegalArgumentException("Arbre trop profond : fichier corrompu");
        }
        if (in.readBit() == 1) {
            return new HuffmanNode((char) in.readBits(CHAR_BITS), 0);
        }
        HuffmanNode parent = new HuffmanNode('\0', 0);
        parent.setLeft(readTree(in, depth + 1));
        parent.setRight(readTree(in, depth + 1));
        return parent;
    }

    /** Écriture bit à bit, poids fort en premier. */
    static final class BitWriter {
        private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        private int current;
        private int filled;

        void writeBit(int bit) {
            current = (current << 1) | (bit & 1);
            if (++filled == 8) {
                bytes.write(current);
                current = 0;
                filled = 0;
            }
        }

        void writeBits(int value, int count) {
            for (int i = count - 1; i >= 0; i--) {
                writeBit((value >>> i) & 1);
            }
        }

        void writeInt(int value) {
            writeBits(value, 32);
        }

        void writeBytes(byte[] raw) {
            for (byte b : raw) {
                writeBits(b & 0xFF, 8);
            }
        }

        byte[] toByteArray() {
            if (filled > 0) {
                bytes.write(current << (8 - filled)); // bourrage à droite
                current = 0;
                filled = 0;
            }
            return bytes.toByteArray();
        }
    }

    /** Lecture bit à bit, poids fort en premier ; lève une exception si le fichier est tronqué. */
    static final class BitReader {
        private final byte[] bytes;
        private int bitPosition;

        BitReader(byte[] bytes, int startByte) {
            this.bytes = bytes;
            this.bitPosition = startByte * 8;
        }

        int readBit() {
            int index = bitPosition >>> 3;
            if (index >= bytes.length) {
                throw new IllegalArgumentException("Fichier tronqué");
            }
            int bit = (bytes[index] >>> (7 - (bitPosition & 7))) & 1;
            bitPosition++;
            return bit;
        }

        int readBits(int count) {
            int value = 0;
            for (int i = 0; i < count; i++) {
                value = (value << 1) | readBit();
            }
            return value;
        }

        int readInt() {
            return readBits(32);
        }
    }
}
