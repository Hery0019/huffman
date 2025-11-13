package hery.itu.huffman;

import com.google.gson.Gson;
import java.util.*;

public class HuffmanTree {
    private HuffmanNode root;

    public void buildTree(Map<Character, Integer> frequencyMap) {
        PriorityQueue<HuffmanNode> queue = new PriorityQueue<>();
        for (var entry : frequencyMap.entrySet()) {
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
        root = queue.poll();
    }

    public HuffmanNode getRoot() {
        return root;
    }

    public Map<Character, String> generateCodes() {
        Map<Character, String> huffmanCodes = new HashMap<>();
        generateCodesHelper(root, "", huffmanCodes);
        return huffmanCodes;
    }

    private void generateCodesHelper(HuffmanNode node, String code, Map<Character, String> map) {
        if (node == null) return;
        if (node.getLeft() == null && node.getRight() == null) {
            map.put(node.getCharacter(), code);
        }
        generateCodesHelper(node.getLeft(), code + "0", map);
        generateCodesHelper(node.getRight(), code + "1", map);
    }

    // Convertir l'arbre en JSON pour l'affichage
    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(root);
    }
}
