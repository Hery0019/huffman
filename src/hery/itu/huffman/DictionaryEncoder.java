package hery.itu.huffman;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Encode un texte avec un dictionnaire saisi à la main (symbole → code).
 * Le texte est parcouru par point de code, comme la base qui stocke un caractère par ligne :
 * un emoji compte pour un symbole, pas deux.
 */
public final class DictionaryEncoder {

    /** Texte encodé et symboles rencontrés qui n'ont pas de code (remplacés par {@code ?}). */
    public record Result(String encoded, Set<String> missingSymbols) {
        public boolean isComplete() {
            return missingSymbols.isEmpty();
        }
    }

    public static final char MISSING_MARK = '?';

    private final Map<String, String> codes;

    public DictionaryEncoder(Map<String, String> codes) {
        this.codes = codes;
    }

    public Result encode(String text) {
        StringBuilder encoded = new StringBuilder();
        Set<String> missing = new TreeSet<>();
        text.codePoints().forEach(codePoint -> {
            String symbol = new String(Character.toChars(codePoint));
            String code = codes.get(symbol);
            if (code == null) {
                missing.add(symbol);
                encoded.append(MISSING_MARK);
            } else {
                encoded.append(code);
            }
        });
        return new Result(encoded.toString(), Collections.unmodifiableSet(missing));
    }
}
