package evaluation;

import solver.Board;
import solver.Vehicle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.GZIPOutputStream;

public class DiversityScorer {

    // ==== Grid Difference ====

    public static double boardDifference(Board b1, Board b2) {
        int[][] grid1 = b1.getGrid();
        int[][] grid2 = b2.getGrid();
        int size = b1.getBoardSize();

        int diff = 0;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (grid1[y][x] != grid2[y][x]) {
                    diff++;
                }
            }
        }
        return (double) diff / (size * size);
    }

    public static double averagePairwiseGridDiversity(List<Board> boards) {
        double total = 0.0;
        int count = 0;
        for (int i = 0; i < boards.size(); i++) {
            for (int j = i + 1; j < boards.size(); j++) {
                total += boardDifference(boards.get(i), boards.get(j));
                count++;
            }
        }
        return count == 0 ? 0 : total / count;
    }

    // ==== NCD Compression Difference ====

    public static double normalizedCompressionDistance(String s1, String s2) throws IOException {
        byte[] c1 = compress(s1.getBytes());
        byte[] c2 = compress(s2.getBytes());
        byte[] c12 = compress((s1 + s2).getBytes());

        int c1Len = c1.length;
        int c2Len = c2.length;
        int c12Len = c12.length;

        return (double)(c12Len - Math.min(c1Len, c2Len)) / Math.max(c1Len, c2Len);
    }

    private static byte[] compress(byte[] data) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
        GZIPOutputStream gzip = new GZIPOutputStream(bos);
        gzip.write(data);
        gzip.close();
        return bos.toByteArray();
    }

    public static double averagePairwiseNCD(List<Board> boards) throws IOException {
        List<String> serialized = new ArrayList<>();
        for (Board b : boards) {
            serialized.add(serializeBoard(b));
        }

        double total = 0.0;
        int count = 0;
        for (int i = 0; i < serialized.size(); i++) {
            for (int j = i + 1; j < serialized.size(); j++) {
                total += normalizedCompressionDistance(serialized.get(i), serialized.get(j));
                count++;
            }
        }
        return count == 0 ? 0 : total / count;
    }

    private static String serializeBoard(Board board) {
        StringBuilder sb = new StringBuilder();
        int[][] grid = board.getGrid();
        int size = board.getBoardSize();
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                sb.append(grid[y][x]);
            }
        }
        return sb.toString();
    }

    // ==== Vehicle Graph Blocking ====

    public static Map<Integer, Set<Integer>> buildBlockingGraph(Board board) {
        Map<Integer, Set<Integer>> graph = new HashMap<>();
        for (Vehicle v : board.getVehicles().values()) {
            graph.putIfAbsent(v.getId(), new HashSet<>());
            List<Integer> blockers = findBlockingVehicles(v, board);
            graph.get(v.getId()).addAll(blockers);
        }
        return graph;
    }

    private static List<Integer> findBlockingVehicles(Vehicle vehicle, Board board) {
        List<Integer> blockers = new ArrayList<>();
        int[][] grid = board.getGrid();
        int boardSize = board.getBoardSize();

        if (vehicle.getIsHorizontal()) {
            int left = vehicle.getStartX() - 1;
            int right = vehicle.getStartX() + vehicle.getLength();
            int y = vehicle.getStartY();
            if (left >= 0 && grid[y][left] != 0) blockers.add(grid[y][left]);
            if (right < boardSize && grid[y][right] != 0) blockers.add(grid[y][right]);
        } else {
            int up = vehicle.getStartY() - 1;
            int down = vehicle.getStartY() + vehicle.getLength();
            int x = vehicle.getStartX();
            if (up >= 0 && grid[up][x] != 0) blockers.add(grid[up][x]);
            if (down < boardSize && grid[down][x] != 0) blockers.add(grid[down][x]);
        }

        return blockers;
    }

    public static double averageGraphDifference(List<Board> boards) {
        List<Map<Integer, Set<Integer>>> graphs = new ArrayList<>();
        for (Board b : boards) {
            graphs.add(buildBlockingGraph(b));
        }

        double total = 0.0;
        int count = 0;
        for (int i = 0; i < graphs.size(); i++) {
            for (int j = i + 1; j < graphs.size(); j++) {
                total += graphDifference(graphs.get(i), graphs.get(j));
                count++;
            }
        }
        return count == 0 ? 0 : total / count;
    }

    private static int graphDifference(Map<Integer, Set<Integer>> g1, Map<Integer, Set<Integer>> g2) {
        Set<String> edges1 = new HashSet<>();
        for (var entry : g1.entrySet()) {
            for (Integer to : entry.getValue()) {
                edges1.add(entry.getKey() + "->" + to);
            }
        }

        Set<String> edges2 = new HashSet<>();
        for (var entry : g2.entrySet()) {
            for (Integer to : entry.getValue()) {
                edges2.add(entry.getKey() + "->" + to);
            }
        }

        Set<String> union = new HashSet<>(edges1);
        union.addAll(edges2);
        Set<String> intersection = new HashSet<>(edges1);
        intersection.retainAll(edges2);

        return union.size() - intersection.size(); // edge difference
    }

    // ==== Symmetry Checking ====

    public static boolean isHorizontallySymmetric(Board board) {
        int[][] grid = board.getGrid();
        int size = board.getBoardSize();

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size / 2; x++) {
                if (grid[y][x] != grid[y][size - 1 - x]) {
                    return false;
                }
            }
        }
        return true;
    }

    public static double percentageSymmetric(List<Board> boards) {
        long symmetric = boards.stream().filter(DiversityScorer::isHorizontallySymmetric).count();
        return (double) symmetric / boards.size();
    }

    // ==== Path Entropy & Branching Factor ====

    public static double averagePathEntropy(List<List<Board>> solutionPaths) {
        double totalEntropy = 0.0;
        int totalBoards = 0;

        for (List<Board> path : solutionPaths) {
            for (Board board : path) {
                List<Board> moves = board.expand();
                int moveCount = moves.size();
                if (moveCount > 0) {
                    totalEntropy += Math.log(moveCount) / Math.log(2);
                }
                totalBoards++;
            }
        }

        return totalBoards == 0 ? 0 : totalEntropy / totalBoards;
    }

    public static double averageBranchingFactor(List<List<Board>> solutionPaths) {
        double totalBranches = 0.0;
        int totalBoards = 0;

        for (List<Board> path : solutionPaths) {
            for (Board board : path) {
                int moves = board.expand().size();
                totalBranches += moves;
                totalBoards++;
            }
        }

        return totalBoards == 0 ? 0 : totalBranches / totalBoards;
    }

    // ==== Final Report ====

    public static void printDiversityReport(
            List<Board> boards,
            List<List<Board>> solutionPaths
    ) {
        try {
            double avgGridDiff = averagePairwiseGridDiversity(boards);
            double avgNCD = averagePairwiseNCD(boards);
            double avgGraphDiff = averageGraphDifference(boards);
            double percentSymmetric = percentageSymmetric(boards);
            double avgPathEntropy = averagePathEntropy(solutionPaths);
            double avgBranchingFactor = averageBranchingFactor(solutionPaths);

            System.out.println("=== Advanced Diversity Report ===");
            System.out.printf("Average Grid Difference: %.4f%n", avgGridDiff);
            System.out.printf("Average Normalized Compression Distance (NCD): %.4f%n", avgNCD);
            System.out.printf("Average Vehicle Graph Difference: %.4f%n", avgGraphDiff);
            System.out.printf("Percentage of Symmetric Puzzles: %.2f%%%n", percentSymmetric * 100);
            System.out.printf("Average Path Entropy (bits): %.4f%n", avgPathEntropy);
            System.out.printf("Average Branching Factor: %.4f%n", avgBranchingFactor);
            System.out.println("==================================");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

