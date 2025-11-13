package hery.itu.huffman;

public class HuffmanNode implements Comparable<HuffmanNode> {
    private char character;
    private int frequency;
    private HuffmanNode left;
    private HuffmanNode right;

    public HuffmanNode(char character, int frequency) {
        this.character = character;
        this.frequency = frequency;
    }

    public char getCharacter() { return character; }
    public void setCharacter(char character) { this.character = character; }
    public int getFrequency() { return frequency; }
    public void setFrequency(int frequency) { this.frequency = frequency; }
    public HuffmanNode getLeft() { return left; }
    public void setLeft(HuffmanNode left) { this.left = left; }
    public HuffmanNode getRight() { return right; }
    public void setRight(HuffmanNode right) { this.right = right; }

    @Override
    public int compareTo(HuffmanNode other) {
        return Integer.compare(this.frequency, other.frequency);
    }
}