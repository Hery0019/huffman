package hery.itu.huffman;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CodeStatisticsTest {

    private static CodeStatistics forText(String text) {
        HuffmanTree tree = HuffmanTree.fromText(text);
        return CodeStatistics.of(HuffmanTree.countFrequencies(text), tree.generateCodes());
    }

    @Test
    void equiprobableSymbolsGiveAFixedLengthOptimalCode() {
        CodeStatistics stats = forText("abcdefgh");
        assertEquals(3.0, stats.entropy(), 1e-9);
        assertEquals(3.0, stats.averageLength(), 1e-9);
        assertEquals(1.0, stats.efficiency(), 1e-9);
        assertEquals(3, stats.fixedLength());
        assertEquals(1.0, stats.kraftSum(), 1e-9);
        assertEquals(24, stats.fixedBitsTotal());
    }

    @Test
    void huffmanCodesAreCompleteAndNeverBeatEntropy() {
        for (String text : List.of("hello huffman", "aaaaaaaabbbbccd", "Portez ce vieux whisky au juge blond qui fume.")) {
            CodeStatistics stats = forText(text);
            assertEquals(1.0, stats.kraftSum(), 1e-9, "un code de Huffman est complet");
            assertTrue(stats.averageLength() >= stats.entropy() - 1e-9, "L ≥ H");
            assertTrue(stats.averageLength() < stats.entropy() + 1, "L < H + 1");
            assertTrue(stats.efficiency() > 0 && stats.efficiency() <= 1);
        }
    }

    @Test
    void dyadicFrequenciesReachTheEntropyExactly() {
        // 8, 4, 2, 1 puis 1 : probabilités 1/2, 1/4, 1/8, 1/16, 1/16 → H = L
        CodeStatistics stats = forText("aaaaaaaabbbbccde");
        assertEquals(stats.entropy(), stats.averageLength(), 1e-9);
        assertEquals(1.0, stats.efficiency(), 1e-9);
    }

    @Test
    void singleSymbolHasZeroEntropyAndOneBitCode() {
        CodeStatistics stats = forText("aaaa");
        assertEquals(0.0, stats.entropy(), 1e-9);
        assertEquals(1.0, stats.averageLength(), 1e-9);
        assertEquals(0.0, stats.efficiency(), 1e-9);
        assertEquals(1, stats.fixedLength());
        assertEquals(0.5, stats.kraftSum(), 1e-9);
    }

    @Test
    void fixedLengthRoundsUp() {
        assertEquals(1, CodeStatistics.fixedLength(0));
        assertEquals(1, CodeStatistics.fixedLength(1));
        assertEquals(1, CodeStatistics.fixedLength(2));
        assertEquals(2, CodeStatistics.fixedLength(3));
        assertEquals(2, CodeStatistics.fixedLength(4));
        assertEquals(3, CodeStatistics.fixedLength(5));
        assertEquals(5, CodeStatistics.fixedLength(17));
        assertEquals(8, CodeStatistics.fixedLength(256));
    }

    @Test
    void kraftSumOfAManualDictionary() {
        assertEquals(0.75, CodeStatistics.kraftSum(Map.of("a", "0", "b", "10").values()), 1e-9);
        assertEquals(1.0, CodeStatistics.kraftSum(List.of("00", "01", "10", "11")), 1e-9);
        assertEquals(0.0, CodeStatistics.kraftSum(List.of()), 1e-9);
    }
}
