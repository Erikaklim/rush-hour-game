package solver;

import heuristics.Heuristic;

import java.util.*;

public class AStar {
    private IndexedPriorityQueue<Node> openQueue;
    private Set<Board> closedSet;
    private int nodesExpanded;

    public AStar() {
        openQueue = new IndexedPriorityQueue<>(Comparator.comparingInt(Node::getF));
        closedSet = new HashSet<>();
        nodesExpanded = 0;
    }

    public List<Board> solve(Board initialBoard, Heuristic heuristic) {
        int h = heuristic.calculateH(initialBoard);
        Node root = new Node(initialBoard, null, 0, h);
        openQueue.add(root);

        while (!openQueue.isEmpty()) {
            Node current = openQueue.poll();
            nodesExpanded ++;

            if (current.getBoard().isGoal()) {
                return constructPath(current);
            }

            closedSet.add(current.getBoard());

            for (Node successor : current.expand(heuristic)) {
                Board successorBoard = successor.getBoard();

                if (closedSet.contains(successorBoard)) {
                    continue;
                }

                if (!openQueue.contains(successor)) {
                    openQueue.add(successor);
                } else {
                    Node existingNode = openQueue.getElement(successor);
                    if (successor.getG() < existingNode.getG()) {
                        existingNode.setG(successor.getG());
                        existingNode.setH(successor.getH());
                        existingNode.setParent(current);
                        openQueue.update(existingNode);
                    }
                }
            }
        }
        return null;
    }

    private List<Board> constructPath(Node node) {
        List<Board> path = new ArrayList<>();
        while (node != null) {
            path.add(0, node.getBoard());
            node = node.getParent();
        }
        return path;
    }

    public int getNodesExpanded(){
        return nodesExpanded;
    }
}
