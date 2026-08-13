package hery.itu.huffman;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Codage de Shannon-Fano : les symboles triés par fréquence décroissante sont coupés en deux groupes
 * de poids aussi proches que possible, 0 pour le premier, 1 pour le second, puis chaque groupe est
 * découpé à son tour. Le code obtenu est préfixe mais pas toujours optimal, contrairement à Huffman.
 */
public final class ShannonFano {

    private ShannonFano() {}

    /** Codes de Shannon-Fano ; à fréquence égale, l'ordre de première apparition départage. */
    public static Map<Character, String> codes(Map<Character, Integer> frequencies) {
        List<Map.Entry<Character, Integer>> sorted = new ArrayList<>(frequencies.entrySet());
        sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue())); // tri stable : l'ordre d'arrivée départage

        Map<Character, String> codes = new LinkedHashMap<>();
        if (sorted.isEmpty()) {
            return codes;
        }
        if (sorted.size() == 1) {
            codes.put(sorted.get(0).getKey(), "0"); // même convention que Huffman pour un symbole unique
            return codes;
        }
        split(sorted, 0, sorted.size(), new StringBuilder(), codes);
        // Restituer l'ordre d'origine des symboles pour l'affichage
        Map<Character, String> ordered = new LinkedHashMap<>();
        for (Character symbol : frequencies.keySet()) {
            ordered.put(symbol, codes.get(symbol));
        }
        return ordered;
    }

    private static void split(List<Map.Entry<Character, Integer>> symbols, int from, int to,
                              StringBuilder prefix, Map<Character, String> codes) {
        if (to - from == 1) {
            codes.put(symbols.get(from).getKey(), prefix.toString());
            return;
        }
        int total = 0;
        for (int i = from; i < to; i++) {
            total += symbols.get(i).getValue();
        }
        // Point de coupure : le premier groupe s'arrête là où son poids est le plus proche de la moitié.
        int cut = from + 1;
        int left = symbols.get(from).getValue();
        int bestGap = Math.abs(2 * left - total);
        for (int i = from + 1; i < to - 1; i++) {
            int candidate = left + symbols.get(i).getValue();
            int gap = Math.abs(2 * candidate - total);
            if (gap < bestGap) {
                bestGap = gap;
                left = candidate;
                cut = i + 1;
            } else {
                break; // le poids ne fait que croître : l'écart augmente ensuite
            }
        }
        prefix.append('0');
        split(symbols, from, cut, prefix, codes);
        prefix.setLength(prefix.length() - 1);

        prefix.append('1');
        split(symbols, cut, to, prefix, codes);
        prefix.setLength(prefix.length() - 1);
    }
}
