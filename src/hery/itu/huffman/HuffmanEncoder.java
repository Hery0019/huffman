package  hery.itu.huffman;

import java.util.Map;

public class HuffmanEncoder {
    private Map<Character, String> huffmanCodes;

    public void setCodes(Map<Character, String> codes) {
        this.huffmanCodes = codes;
    }

    public String encode(String text) {
        StringBuilder encodedText = new StringBuilder();
        for (char c : text.toCharArray()) {
            encodedText.append(huffmanCodes.get(c));
        }
        return encodedText.toString();
    }
}
