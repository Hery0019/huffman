package hery.itu.huffman;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ShannonFanoTest {

    private static Map<Character, Integer> frequencies(String text) {
        return HuffmanTree.countFrequencies(text);
    }

    @Test
    void textbookExampleGivesTheExpectedCodes() {
        // Exemple classique : A=15 B=7 C=6 D=6 E=5 → A=00 B=01 C=10 D=110 E=111
        Map<Character, Integer> frequencies = new LinkedHashMap<>();
        frequencies.put('A', 15);
        frequencies.put('B', 7);
        frequencies.put('C', 6);
        frequencies.put('D', 6);
        frequencies.put('E', 5);
        Map<Character, String> codes = ShannonFano.codes(frequencies);

        assertEquals("00", codes.get('A'));
        assertEquals("01", codes.get('B'));
        assertEquals("10", codes.get('C'));
        assertEquals("110", codes.get('D'));
        assertEquals("111", codes.get('E'));
    }

    @Test
    void codesArePrefixFreeAndDecodeTheText() {
        for (String text : List.of("hello huffman", "aaaaaaaabbbbccd", "Portez ce vieux whisky au juge blond qui fume.", "ab")) {
            Map<Character, String> codes = ShannonFano.codes(frequencies(text));
            Map<String, String> asStrings = new LinkedHashMap<>();
            codes.forEach((symbol, code) -> asStrings.put(String.valueOf(symbol), code));

            String bits = new DictionaryEncoder(asStrings).encode(text).encoded();
            DictionaryDecoder.Result result = new DictionaryDecoder(asStrings).decode(bits); // refuse un code ambigu
            assertTrue(result.isOk(), text);
            assertEquals(text, result.text());
        }
    }

    @Test
    void neverBeatsHuffmanOnAverageLength() {
        boolean strictlyWorseSomewhere = false;
        for (String text : List.of("hello huffman", "Portez ce vieux whisky au juge blond qui fume.",
                "aaaaaaaaaaaaaaabbbbbbbccccccddddddeeeee", "abracadabra abracadabra", "mississippi")) {
            Map<Character, Integer> frequencies = frequencies(text);
            HuffmanTree tree = new HuffmanTree();
            tree.buildTree(frequencies);
            double huffman = CodeStatistics.of(frequencies, tree.generateCodes()).averageLength();
            double shannonFano = CodeStatistics.of(frequencies, ShannonFano.codes(frequencies)).averageLength();
            assertTrue(shannonFano >= huffman - 1e-9, text + " : Shannon-Fano " + shannonFano + " < Huffman " + huffman);
            if (shannonFano > huffman + 1e-9) strictlyWorseSomewhere = true;
        }
        assertTrue(strictlyWorseSomewhere, "au moins un texte où Shannon-Fano est strictement moins bon");
    }

    @Test
    void equiprobablePowerOfTwoMatchesHuffman() {
        Map<Character, Integer> frequencies = frequencies("abcdefgh");
        Map<Character, String> codes = ShannonFano.codes(frequencies);
        codes.values().forEach(code -> assertEquals(3, code.length()));
        assertEquals(1.0, CodeStatistics.kraftSum(codes.values()), 1e-9);
    }

    @Test
    void singleSymbolGetsCodeZero() {
        assertEquals(Map.of('z', "0"), ShannonFano.codes(frequencies("zzz")));
        assertFalse(ShannonFano.codes(frequencies("")).containsKey('z'));
    }
}
