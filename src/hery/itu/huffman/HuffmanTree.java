package hery.itu.huffman;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.PriorityQueue;

/** Construction de l'arbre de Huffman à partir des fréquences, et dérivation des codes. */
public class HuffmanTree {
    private HuffmanNode root;

    /** Compte les occurrences de chaque caractère, dans l'ordre de première apparition. */
    public static Map<Character, Integer> countFrequencies(String text) {
        Map<Character, Integer> frequencies = new LinkedHashMap<>();
        for (char c : text.toCharArray()) {
            frequencies.merge(c, 1, Integer::sum);
        }
        return frequencies;
    }

    /** Construit directement l'arbre d'un texte. */
    public static HuffmanTree fromText(String text) {
        HuffmanTree tree = new HuffmanTree();
        tree.buildTree(countFrequencies(text));
        return tree;
    }

    public void buildTree(Map<Character, Integer> frequencyMap) {
        PriorityQueue<HuffmanNode> queue = new PriorityQueue<>();
        for (Map.Entry<Character, Integer> entry : frequencyMap.entrySet()) {
            queue.add(new HuffmanNode(entry.getKey(), entry.getValue()));
        }

        while (queue.size() > 1) {
            HuffmanNode left = queue.poll();
            HuffmanNode right = queue.poll();
            HuffmanNode parent = new HuffmanNode('\0', left.getFrequency() + right.getFrequency());
            parent.setLeft(left);
            parent.setRight(right);
            queue.add(parent);
        }
        root = queue.poll(); // null si aucune fréquence
    }

    /** Racine de l'arbre, ou {@code null} si l'arbre est vide. */
    public HuffmanNode getRoot() {
        return root;
    }

    /**
     * Code de chaque symbole : chemin de la racine à la feuille, 0 à gauche et 1 à droite.
     * Cas particulier : un texte à un seul symbole distinct donne un arbre réduit à une feuille ;
     * ce symbole reçoit le code "0" (sinon il serait encodé par une chaîne vide, non décodable).
     */
    public Map<Character, String> generateCodes() {
        Map<Character, String> codes = new LinkedHashMap<>();
        if (root == null) {
            return codes;
        }
        if (root.isLeaf()) {
            codes.put(root.getCharacter(), "0");
            return codes;
        }
        collectCodes(root, new StringBuilder(), codes);
        return codes;
    }

    private static void collectCodes(HuffmanNode node, StringBuilder prefix, Map<Character, String> codes) {
        if (node.isLeaf()) {
            codes.put(node.getCharacter(), prefix.toString());
            return;
        }
        prefix.append('0');
        collectCodes(node.getLeft(), prefix, codes);
        prefix.setLength(prefix.length() - 1);

        prefix.append('1');
        collectCodes(node.getRight(), prefix, codes);
        prefix.setLength(prefix.length() - 1);
    }
}
