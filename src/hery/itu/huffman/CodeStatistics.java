package hery.itu.huffman;

import java.util.Collection;
import java.util.Map;

/**
 * Mesures théoriques d'un code préfixe pour une distribution de symboles :
 * entropie de Shannon, longueur moyenne, efficacité, codage fixe minimal et somme de Kraft.
 *
 * @param symbolCount     nombre total de symboles du texte
 * @param distinctSymbols nombre de symboles distincts
 * @param entropy         H = −Σ p·log₂ p, en bits par symbole : la borne inférieure de toute longueur moyenne
 * @param averageLength   L = Σ p·ℓ, en bits par symbole, pour le code donné
 * @param efficiency      H / L, entre 0 et 1 (1 = le code atteint l'entropie)
 * @param fixedLength     ⌈log₂ k⌉ : la longueur d'un codage à longueur fixe des k symboles distincts
 * @param kraftSum        Σ 2^−ℓ : 1 pour un code complet, < 1 s'il reste des mots de code disponibles
 */
public record CodeStatistics(int symbolCount, int distinctSymbols, double entropy, double averageLength,
                             double efficiency, int fixedLength, double kraftSum) {

    public static CodeStatistics of(Map<Character, Integer> frequencies, Map<Character, String> codes) {
        int total = 0;
        for (int frequency : frequencies.values()) {
            total += frequency;
        }
        double entropy = 0;
        double averageLength = 0;
        if (total > 0) {
            for (Map.Entry<Character, Integer> entry : frequencies.entrySet()) {
                double p = (double) entry.getValue() / total;
                entropy -= p * log2(p);
                averageLength += p * codes.get(entry.getKey()).length();
            }
        }
        double efficiency = averageLength == 0 ? 0 : entropy / averageLength;
        return new CodeStatistics(total, frequencies.size(), entropy, averageLength, efficiency,
                fixedLength(frequencies.size()), kraftSum(codes.values()));
    }

    /** ⌈log₂ k⌉ bits pour distinguer k symboles ; 1 bit au minimum pour rester décodable. */
    public static int fixedLength(int distinctSymbols) {
        if (distinctSymbols <= 1) {
            return 1;
        }
        return (int) Math.ceil(log2(distinctSymbols) - 1e-12);
    }

    /** Σ 2^−ℓ sur les longueurs des codes ; les codes vides ou nuls sont ignorés. */
    public static double kraftSum(Collection<String> codes) {
        double sum = 0;
        for (String code : codes) {
            if (code != null && !code.isEmpty()) {
                sum += Math.pow(2, -code.length());
            }
        }
        return sum;
    }

    /** Nombre de bits d'un codage fixe du texte entier. */
    public int fixedBitsTotal() {
        return symbolCount * fixedLength;
    }

    public static double log2(double x) {
        return Math.log(x) / Math.log(2);
    }
}
