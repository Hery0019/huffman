package hery.itu.huffman;


public class HuffmanDecoder {
    private HuffmanNode root;

    public HuffmanDecoder(HuffmanNode root) {
        this.root = root;
    }

    public String decode(String encodedText) {
        StringBuilder decodedText = new StringBuilder();
        HuffmanNode current = root;
        for (char bit : encodedText.toCharArray()) {
            current = (bit == '0') ? current.getLeft() : current.getRight();
            if (current.getLeft() == null && current.getRight() == null) {
                decodedText.append(current.getCharacter());
                current = root;
            }
        }
        return decodedText.toString();
    }
}