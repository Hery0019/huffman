package hery.itu.huffman;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class HuffmanRoundTripTest {

    private static String roundTrip(String text) {
        HuffmanTree tree = HuffmanTree.fromText(text);
        Map<Character, String> codes = tree.generateCodes();
        String encoded = new HuffmanEncoder(codes).encode(text);
        return new HuffmanDecoder(tree.getRoot()).decode(encoded);
    }

    @Test
    void encodeThenDecodeRestoresTheText() {
        for (String text : List.of(
                "hello huffman",
                "bonjour le monde, écrit à la main\nseconde ligne\ttabulée",
                "aab",
                "😀 emoji hors BMP 😀😀",
                "   ")) {
            assertEquals(text, roundTrip(text), "aller-retour pour « " + text + " »");
        }
    }

    @Test
    void singleDistinctSymbolGetsCodeZeroAndRoundTrips() {
        HuffmanTree tree = HuffmanTree.fromText("aaaa");
        Map<Character, String> codes = tree.generateCodes();

        assertEquals(Map.of('a', "0"), codes);
        assertEquals("0000", new HuffmanEncoder(codes).encode("aaaa"));
        assertEquals("aaaa", roundTrip("aaaa"));
    }

    @Test
    void emptyTextGivesEmptyTreeAndEmptyOutput() {
        HuffmanTree tree = HuffmanTree.fromText("");
        assertNull(tree.getRoot());
        assertTrue(tree.generateCodes().isEmpty());
        assertEquals("", new HuffmanDecoder(tree.getRoot()).decode(""));
    }

    @Test
    void generatedCodesArePrefixFree() {
        Map<Character, String> codes = HuffmanTree.fromText("the quick brown fox jumps over the lazy dog").generateCodes();
        for (Map.Entry<Character, String> a : codes.entrySet()) {
            for (Map.Entry<Character, String> b : codes.entrySet()) {
                if (!a.getKey().equals(b.getKey())) {
                    assertFalse(b.getValue().startsWith(a.getValue()),
                            a.getKey() + "=" + a.getValue() + " est préfixe de " + b.getKey() + "=" + b.getValue());
                }
            }
        }
    }

    @Test
    void frequentSymbolsGetShorterOrEqualCodes() {
        String text = "aaaaaaaabbbbccd";
        Map<Character, String> codes = HuffmanTree.fromText(text).generateCodes();
        assertTrue(codes.get('a').length() <= codes.get('b').length());
        assertTrue(codes.get('b').length() <= codes.get('c').length());
        assertTrue(codes.get('c').length() <= codes.get('d').length());
    }

    @Test
    void countFrequenciesKeepsFirstAppearanceOrder() {
        Map<Character, Integer> frequencies = HuffmanTree.countFrequencies("banana");
        assertEquals(List.of('b', 'a', 'n'), List.copyOf(frequencies.keySet()));
        assertEquals(3, frequencies.get('a'));
    }

    @Test
    void mergeTraceReplaysToTheSameTree() {
        HuffmanTree tree = HuffmanTree.fromText("hello huffman");
        List<HuffmanNode> nodes = tree.getNodes();
        List<HuffmanTree.MergeStep> steps = tree.getSteps();

        int distinct = HuffmanTree.countFrequencies("hello huffman").size();
        assertEquals(distinct - 1, steps.size(), "k symboles → k-1 fusions");
        assertEquals(2 * distinct - 1, nodes.size(), "k feuilles + k-1 parents");
        for (int i = 0; i < nodes.size(); i++) {
            assertEquals(i, nodes.get(i).getId(), "identifiants dans l'ordre de création");
        }

        // Rejouer les fusions : on doit finir avec exactement une racine, celle de l'arbre.
        java.util.Set<Integer> forest = new java.util.HashSet<>();
        for (HuffmanNode node : nodes) {
            if (node.isLeaf()) forest.add(node.getId());
        }
        for (HuffmanTree.MergeStep step : steps) {
            assertTrue(forest.remove(step.leftId()), "le fils gauche est dans la file");
            assertTrue(forest.remove(step.rightId()), "le fils droit est dans la file");
            HuffmanNode parent = nodes.get(step.parentId());
            assertEquals(step.frequency(), parent.getFrequency());
            assertEquals(nodes.get(step.leftId()).getFrequency() + nodes.get(step.rightId()).getFrequency(), step.frequency());
            forest.add(step.parentId());
        }
        assertEquals(java.util.Set.of(tree.getRoot().getId()), forest);
    }

    @Test
    void singleSymbolHasNoMergeStep() {
        HuffmanTree tree = HuffmanTree.fromText("zzz");
        assertTrue(tree.getSteps().isEmpty());
        assertEquals(1, tree.getNodes().size());
    }

    @Test
    void encoderRejectsSymbolWithoutCode() {
        HuffmanEncoder encoder = new HuffmanEncoder(HuffmanTree.fromText("ab").generateCodes());
        assertThrows(IllegalArgumentException.class, () -> encoder.encode("abc"));
    }

    @Test
    void decoderRejectsInvalidBit() {
        HuffmanDecoder decoder = new HuffmanDecoder(HuffmanTree.fromText("aab").getRoot());
        assertThrows(IllegalArgumentException.class, () -> decoder.decode("0102"));
    }

    @Test
    void decoderRejectsIncompleteSequence() {
        // "aaabc" : a sur un bit, b et c sur deux bits. Le préfixe strict d'un code de deux bits
        // n'est jamais un code complet (les codes sont sans préfixe) : la séquence est incomplète.
        HuffmanTree tree = HuffmanTree.fromText("aaabc");
        Map<Character, String> codes = tree.generateCodes();
        String twoBitCode = codes.get('b');
        assertEquals(2, twoBitCode.length());
        String incomplete = twoBitCode.substring(0, 1);

        HuffmanDecoder decoder = new HuffmanDecoder(tree.getRoot());
        assertThrows(IllegalArgumentException.class, () -> decoder.decode(incomplete));
    }
}
