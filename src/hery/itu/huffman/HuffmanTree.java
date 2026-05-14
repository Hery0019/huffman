package hery.itu.huffman;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Construction de l'arbre de Huffman à partir des fréquences, et dérivation des codes.
 * La construction garde la trace des fusions ({@link #getSteps()}) pour pouvoir être rejouée pas à pas.
 */
public class HuffmanTree {

    /** Une fusion : les deux nœuds de plus faible fréquence retirés de la file, et le parent créé. */
    public record MergeStep(int leftId, int rightId, int parentId, int frequency) {}

    private HuffmanNode root;
    private final List<HuffmanNode> nodes = new ArrayList<>();
    private final List<MergeStep> steps = new ArrayList<>();

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
        nodes.clear();
        steps.clear();

        PriorityQueue<HuffmanNode> queue = new PriorityQueue<>();
        for (Map.Entry<Character, Integer> entry : frequencyMap.entrySet()) {
            HuffmanNode leaf = new HuffmanNode(entry.getKey(), entry.getValue());
            register(leaf);
            queue.add(leaf);
        }

        while (queue.size() > 1) {
            HuffmanNode left = queue.poll();
            HuffmanNode right = queue.poll();
            HuffmanNode parent = new HuffmanNode('\0', left.getFrequency() + right.getFrequency());
            register(parent);
            parent.setLeft(left);
            parent.setRight(right);
            steps.add(new MergeStep(left.getId(), right.getId(), parent.getId(), parent.getFrequency()));
            queue.add(parent);
        }
        root = queue.poll(); // null si aucune fréquence
    }

    private void register(HuffmanNode node) {
        node.setId(nodes.size());
        nodes.add(node);
    }

    /** Racine de l'arbre, ou {@code null} si l'arbre est vide. */
    public HuffmanNode getRoot() {
        return root;
    }

    /** Tous les nœuds, indexés par identifiant : les feuilles d'abord, puis les parents dans l'ordre des fusions. */
    public List<HuffmanNode> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    /** Les fusions dans l'ordre où elles ont eu lieu ; vide pour un arbre réduit à une feuille. */
    public List<MergeStep> getSteps() {
        return Collections.unmodifiableList(steps);
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
