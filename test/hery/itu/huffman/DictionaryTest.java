package hery.itu.huffman;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DictionaryTest {

    private static final Map<String, String> EXISTING = Map.of("a", "0", "b", "10");

    @Test
    void acceptsValidEntryIncludingSpaceAndEmoji() {
        assertEquals(Optional.empty(), DictionaryValidator.check("c", "110", EXISTING));
        assertEquals(Optional.empty(), DictionaryValidator.check(" ", "111", EXISTING));
        assertEquals(Optional.empty(), DictionaryValidator.check("😀", "1110", EXISTING));
    }

    @Test
    void rejectsEmptyOrMultiCharacterSymbols() {
        assertEquals(Optional.of(DictionaryValidator.Problem.EMPTY), DictionaryValidator.check("", "0", Map.of()));
        assertEquals(Optional.of(DictionaryValidator.Problem.EMPTY), DictionaryValidator.check("a", "", Map.of()));
        assertEquals(Optional.of(DictionaryValidator.Problem.EMPTY), DictionaryValidator.check(null, "0", Map.of()));
        assertEquals(Optional.of(DictionaryValidator.Problem.NOT_SINGLE_CHARACTER), DictionaryValidator.check("ab", "0", Map.of()));
    }

    @Test
    void rejectsNonBinaryCode() {
        assertEquals(Optional.of(DictionaryValidator.Problem.INVALID_CODE), DictionaryValidator.check("a", "012", Map.of()));
        assertEquals(Optional.of(DictionaryValidator.Problem.INVALID_CODE), DictionaryValidator.check("a", " 01", Map.of()));
    }

    @Test
    void rejectsDuplicatesAndPrefixConflicts() {
        assertEquals(Optional.of(DictionaryValidator.Problem.DUPLICATE_CHARACTER), DictionaryValidator.check("a", "111", EXISTING));
        assertEquals(Optional.of(DictionaryValidator.Problem.DUPLICATE_CODE), DictionaryValidator.check("c", "10", EXISTING));
        assertEquals(Optional.of(DictionaryValidator.Problem.PREFIX_CONFLICT), DictionaryValidator.check("c", "01", EXISTING), "a=0 est préfixe de 01");
        assertEquals(Optional.of(DictionaryValidator.Problem.PREFIX_CONFLICT), DictionaryValidator.check("c", "1", EXISTING), "1 est préfixe de b=10");
    }

    @Test
    void encoderReplacesUnknownSymbolsAndReportsThem() {
        DictionaryEncoder encoder = new DictionaryEncoder(Map.of("a", "0", "b", "10", " ", "11"));
        DictionaryEncoder.Result result = encoder.encode("ab ac");

        assertEquals("010110?", result.encoded());
        assertEquals(Set.of("c"), result.missingSymbols());
    }

    @Test
    void encoderIteratesByCodePoint() {
        DictionaryEncoder encoder = new DictionaryEncoder(Map.of("😀", "0", "a", "1"));
        DictionaryEncoder.Result result = encoder.encode("a😀a");

        assertEquals("101", result.encoded());
        assertTrue(result.isComplete());
    }
}
