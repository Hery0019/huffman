package hery.itu.main;

import hery.itu.huffman.*;
import hery.itu.huffman.HuffmanTree;


import java.util.HashMap;
import java.util.Map;

// Classe principale pour tester l'algorithme
public class Main {
    public static void main(String[] args) {
        String text = "hello huffman";

        // Calcul des frequences
        Map<Character, Integer> frequencyMap = new HashMap<>();
        for (char c : text.toCharArray()) {
            frequencyMap.put(c, frequencyMap.getOrDefault(c, 0) + 1);
        }

        // Construction de l'arbre
        HuffmanTree tree = new HuffmanTree();
        tree.buildTree(frequencyMap);
        Map<Character, String> huffmanCodes = tree.generateCodes();

        // Encodage
        HuffmanEncoder encoder = new HuffmanEncoder();
        encoder.setCodes(huffmanCodes);
        String encodedText = encoder.encode(text);
        System.out.println("Texte encodé : " + encodedText);

        // Décodage
        HuffmanDecoder decoder = new HuffmanDecoder(tree.getRoot());
        String decodedText = decoder.decode(encodedText);
        System.out.println("Texte décodé : " + decodedText);
    }
}