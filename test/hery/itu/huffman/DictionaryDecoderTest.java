package hery.itu.huffman;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class DictionaryDecoderTest {

    private static final Map<String, String> CODES = Map.of("e", "0", " ", "10", "l", "110", "o", "1110", "a", "1111");

    @Test
    void decodesWhatTheEncoderProduced() {
        String text = "aleo leo";
        String bits = new DictionaryEncoder(CODES).encode(text).encoded();
        DictionaryDecoder.Result result = new DictionaryDecoder(CODES).decode(bits);

        assertTrue(result.isOk());
        assertEquals(text, result.text());
        assertEquals(8, result.symbolCount());
        assertEquals(-1, result.errorPosition());
    }

    @Test
    void emptyInputDecodesToEmptyText() {
        DictionaryDecoder.Result result = new DictionaryDecoder(CODES).decode("");
        assertTrue(result.isOk());
        assertEquals("", result.text());
    }

    @Test
    void reportsInvalidCharacterWithPositionAndPartialText() {
        // 0 → e, 10 → espace, puis « 2 » (indice 3, position 4) n'est pas un bit
        DictionaryDecoder.Result result = new DictionaryDecoder(CODES).decode("0102x");
        assertFalse(result.isOk());
        assertEquals("e ", result.text());
        assertEquals(2, result.symbolCount());
        assertEquals(3, result.errorPosition());
        assertTrue(result.error().contains("position 4"));
    }

    @Test
    void reportsIncompleteTrailingSequence() {
        DictionaryDecoder.Result result = new DictionaryDecoder(CODES).decode("011");
        assertFalse(result.isOk());
        assertEquals("e", result.text());
        assertEquals(1, result.errorPosition(), "l'erreur pointe le début du code inachevé");
        assertTrue(result.error().contains("incomplète"));
    }

    @Test
    void reportsBitsMatchingNoCode() {
        // Avec les codes 0, 10, 110, 1110, 1111 tout chemin mène à un code : on construit un dictionnaire à trous.
        DictionaryDecoder decoder = new DictionaryDecoder(Map.of("a", "00", "b", "01"));
        DictionaryDecoder.Result result = decoder.decode("0010");
        assertFalse(result.isOk());
        assertEquals("a", result.text());
        assertEquals(2, result.errorPosition());
        assertTrue(result.error().contains("aucun code"));
    }

    @Test
    void emptyDictionaryCannotDecode() {
        DictionaryDecoder.Result result = new DictionaryDecoder(Map.of()).decode("0101");
        assertFalse(result.isOk());
        assertTrue(result.error().contains("vide"));
    }

    @Test
    void rejectsAmbiguousDictionaries() {
        assertThrows(IllegalArgumentException.class, () -> new DictionaryDecoder(Map.of("a", "0", "b", "01")));
        assertThrows(IllegalArgumentException.class, () -> new DictionaryDecoder(Map.of("a", "01", "b", "0")));
        assertThrows(IllegalArgumentException.class, () -> new DictionaryDecoder(Map.of("a", "1", "b", "1")));
        assertThrows(IllegalArgumentException.class, () -> new DictionaryDecoder(Map.of("a", "0x")));
    }
}
