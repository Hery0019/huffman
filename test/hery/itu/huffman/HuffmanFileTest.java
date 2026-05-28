package hery.itu.huffman;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class HuffmanFileTest {

    @Test
    void compressThenDecompressRestoresTheText() {
        for (String text : List.of(
                "hello huffman",
                "bonjour le monde, écrit à la main\nseconde ligne\ttabulée",
                "😀 emoji hors BMP 😀😀",
                "aaaa",
                "ab",
                "x")) {
            HuffmanFile.Compressed compressed = HuffmanFile.compress(text);
            assertEquals(text, HuffmanFile.decompress(compressed.bytes()), "aller-retour pour « " + text + " »");
            assertEquals(text.length(), compressed.symbolCount());
        }
    }

    @Test
    void emptyTextGivesHeaderOnly() {
        HuffmanFile.Compressed compressed = HuffmanFile.compress("");
        assertEquals(HuffmanFile.HEADER_BYTES, compressed.totalBytes());
        assertEquals(0, compressed.treeBits());
        assertEquals(0, compressed.dataBits());
        assertEquals("", HuffmanFile.decompress(compressed.bytes()));
    }

    @Test
    void fileStartsWithMagicAndSymbolCount() {
        byte[] bytes = HuffmanFile.compress("abc").bytes();
        assertArrayEquals(HuffmanFile.MAGIC, Arrays.copyOf(bytes, 4));
        assertArrayEquals(new byte[] {0, 0, 0, 3}, Arrays.copyOfRange(bytes, 4, 8));
    }

    @Test
    void sizeAccountingMatchesTheBytesProduced() {
        HuffmanFile.Compressed compressed = HuffmanFile.compress("hello huffman, écrit à la main");
        int payloadBits = compressed.treeBits() + compressed.dataBits();
        int expectedBytes = HuffmanFile.HEADER_BYTES + (payloadBits + 7) / 8;
        assertEquals(expectedBytes, compressed.totalBytes());
        // 17 feuilles × 17 bits + 16 nœuds internes × 1 bit
        assertEquals(17 * 17 + 16, compressed.treeBits());
    }

    @Test
    void repetitiveTextReallyShrinks() {
        String text = "abracadabra ".repeat(50);
        HuffmanFile.Compressed compressed = HuffmanFile.compress(text);
        assertTrue(compressed.totalBytes() < HuffmanFile.utf8Size(text) / 2,
                compressed.totalBytes() + " octets pour " + HuffmanFile.utf8Size(text) + " octets de texte");
    }

    @Test
    void rejectsForeignOrTruncatedFiles() {
        assertThrows(IllegalArgumentException.class, () -> HuffmanFile.decompress("bonjour".getBytes()));
        assertThrows(IllegalArgumentException.class, () -> HuffmanFile.decompress(new byte[] {'H', 'U', 'F'}));

        byte[] good = HuffmanFile.compress("hello huffman").bytes();
        byte[] truncated = Arrays.copyOf(good, good.length - 3);
        assertThrows(IllegalArgumentException.class, () -> HuffmanFile.decompress(truncated));
    }

    @Test
    void utf8SizeCountsBytesNotChars() {
        assertEquals(1, HuffmanFile.utf8Size("a"));
        assertEquals(2, HuffmanFile.utf8Size("é"));
        assertEquals(4, HuffmanFile.utf8Size("😀"));
    }
}
