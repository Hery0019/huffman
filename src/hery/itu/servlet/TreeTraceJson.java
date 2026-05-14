package hery.itu.servlet;

import com.google.gson.Gson;
import hery.itu.huffman.HuffmanNode;
import hery.itu.huffman.HuffmanTree;
import java.util.ArrayList;
import java.util.List;

/** Sérialise l'arbre et sa trace de construction pour le rejeu pas à pas côté navigateur. */
final class TreeTraceJson {

    /** Un nœud à plat : les enfants sont référencés par identifiant ; {@code character} est nul pour un nœud interne. */
    record Node(int id, String character, int frequency, Integer left, Integer right) {}

    /** Ce que la page reçoit : la table des nœuds et les fusions dans l'ordre. */
    record Trace(List<Node> nodes, List<HuffmanTree.MergeStep> steps) {}

    private TreeTraceJson() {}

    static String of(HuffmanTree tree) {
        List<Node> nodes = new ArrayList<>();
        for (HuffmanNode node : tree.getNodes()) {
            nodes.add(new Node(
                    node.getId(),
                    node.isLeaf() ? String.valueOf(node.getCharacter()) : null,
                    node.getFrequency(),
                    node.getLeft() == null ? null : node.getLeft().getId(),
                    node.getRight() == null ? null : node.getRight().getId()));
        }
        return new Gson().toJson(new Trace(nodes, tree.getSteps()));
    }
}
