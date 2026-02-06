package hery.itu.huffman;

import java.util.Map;
import java.util.Optional;

/**
 * Règles d'admission d'une entrée (symbole, code) dans un dictionnaire :
 * un seul symbole, un code binaire, pas de doublon, et surtout aucun code préfixe d'un autre
 * (sinon le texte encodé n'est plus décodable sans ambiguïté).
 */
public final class DictionaryValidator {

    public enum Problem {
        EMPTY,
        NOT_SINGLE_CHARACTER,
        INVALID_CODE,
        DUPLICATE_CHARACTER,
        DUPLICATE_CODE,
        PREFIX_CONFLICT
    }

    private DictionaryValidator() {}

    /** @return le problème détecté, ou vide si l'entrée peut être ajoutée à {@code existing}. */
    public static Optional<Problem> check(String symbol, String code, Map<String, String> existing) {
        if (symbol == null || symbol.isEmpty() || code == null || code.isEmpty()) {
            return Optional.of(Problem.EMPTY);
        }
        if (symbol.codePointCount(0, symbol.length()) != 1) {
            return Optional.of(Problem.NOT_SINGLE_CHARACTER);
        }
        if (!code.matches("[01]+")) {
            return Optional.of(Problem.INVALID_CODE);
        }
        if (existing.containsKey(symbol)) {
            return Optional.of(Problem.DUPLICATE_CHARACTER);
        }
        for (String other : existing.values()) {
            if (other.equals(code)) {
                return Optional.of(Problem.DUPLICATE_CODE);
            }
            if (other.startsWith(code) || code.startsWith(other)) {
                return Optional.of(Problem.PREFIX_CONFLICT);
            }
        }
        return Optional.empty();
    }
}
