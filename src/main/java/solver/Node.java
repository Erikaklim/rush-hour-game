package solver;

import heuristics.Heuristic;

import java.util.ArrayList;
import java.util.List;

public class Node implements Comparable<Node> {
    private Board board;
    private Node parent;

    private int g;
    private int h;

    public Node(Board board, Node parent, int g, int h) {
        this.board = board;
        this.parent = parent;
        this.g = g;
        this.h = h;
    }

    public List<Node> expand(Heuristic h){
        List<Node> successors = new ArrayList<>();
        for(Board successorBoard : board.expand()){
            int tentativeG = g + 1;
            int successorH = h.calculateH(successorBoard);
            Node successor = new Node(successorBoard, this, tentativeG, successorH);
            successors.add(successor);
        }
        return successors;
    }

    @Override
    public int compareTo(Node other) {
        int fComparison = Integer.compare(this.getF(), other.getF());
        if (fComparison != 0) {
            return fComparison;
        } else {
            // To ensure nodes with different boards are not considered equal,
            // compare the board states
            return this.board.compareTo(other.board);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Node)) return false;
        Node other = (Node) obj;
        return this.board.equals(other.board);
    }

    @Override
    public int hashCode() {
        return this.board.hashCode();
    }


    public int getG(){
        return g;
    }
    public int getH(){
        return h;
    }

    public void setG(int g){
        this.g = g;
    }

    public void setH(int h){
        this.h = h;
    }

    public void setParent(Node parent){
        this.parent = parent;
    }

    public int getF(){
        return g + h;
    }

    public Board getBoard(){
        return board;
    }

    public Node getParent(){
        return parent;
    }

}
