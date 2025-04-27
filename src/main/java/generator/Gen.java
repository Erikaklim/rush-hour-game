package generator;

import solver.AStar;
import solver.Board;
import solver.Vehicle;
import heuristics.BlockingHeuristic;
import heuristics.ChainBlockingHeuristic;
import heuristics.Heuristic;

import java.util.*;

public class Gen {
    private static final int BOARD_SIZE = 6;
    private static final int RED_CAR_ID = 1;
    private static final int EXIT_ROW = 2;
    private static final Random random = new Random();

    public static Board generateLevel(int maxVehicles, int minDepth) {
        boolean[][] occupied = new boolean[BOARD_SIZE][BOARD_SIZE];
        int[] rowFill = new int[BOARD_SIZE];
        int[] colFill = new int[BOARD_SIZE];
        Map<Integer, Set<Boolean>> rowOrientation = new HashMap<>();
        Map<Integer, Set<Boolean>> colOrientation = new HashMap<>();
        Map<Integer, Vehicle> vehicles = new HashMap<>();
        int nextId = RED_CAR_ID + 1;

        // Place red car
        int redX = random.nextInt(BOARD_SIZE - 2);
        Vehicle redCar = new Vehicle(RED_CAR_ID, redX, EXIT_ROW, 2, true);
        vehicles.put(RED_CAR_ID, redCar);
        placeVehicleOnGrid(redCar, occupied, rowFill, colFill, rowOrientation, colOrientation);

        // Place vertical blocker intersecting row 2
        boolean placedBlocker = false;
        List<Integer> candidateXs = new ArrayList<>();
        for (int x = redX + 2; x < BOARD_SIZE; x++) candidateXs.add(x);
        Collections.shuffle(candidateXs);

        for (int x : candidateXs) {
            for (int y = EXIT_ROW - 1; y <= EXIT_ROW; y++) {
                if (y < 0 || y + 1 >= BOARD_SIZE) continue;
                if (!occupied[y][x] && !occupied[y + 1][x]) {
                    Vehicle blocker = new Vehicle(nextId++, x, y, 2, false);
                    vehicles.put(blocker.getId(), blocker);
                    placeVehicleOnGrid(blocker, occupied, rowFill, colFill, rowOrientation, colOrientation);
                    placedBlocker = true;
                    break;
                }
            }
            if (placedBlocker) break;
        }

        if (!placedBlocker) return generateLevel(maxVehicles, minDepth);

        // Solve initial board
        Board board = new Board(cloneVehicleMap(vehicles), BOARD_SIZE);
        AStar solver = new AStar();
        Heuristic heuristic = new ChainBlockingHeuristic();
        List<Board> solution = solver.solve(board, heuristic);
        if (solution == null) return generateLevel(maxVehicles, minDepth);

        int currentDepth = solution.size() - 1;

        // Now try adding vehicles greedily
        int attempts = 0;
        while (vehicles.size() < maxVehicles + 1 && attempts < 2000) {
            attempts++;
            boolean horizontal = random.nextBoolean();
            int length = random.nextBoolean() ? 2 : 3;
            int startX = random.nextInt(BOARD_SIZE - (horizontal ? length - 1 : 0));
            int startY = random.nextInt(BOARD_SIZE - (!horizontal ? length - 1 : 0));

            if (horizontal && startY == EXIT_ROW) continue;

            Vehicle candidate = new Vehicle(nextId, startX, startY, length, horizontal);
            if (!isValidPlacement(candidate, occupied, rowFill, colFill, rowOrientation, colOrientation)) continue;

            // Simulate placement
            Map<Integer, Vehicle> tempVehicles = cloneVehicleMap(vehicles);
            tempVehicles.put(nextId, candidate);
            Board tempBoard = new Board(tempVehicles, BOARD_SIZE);
            AStar tempSolver = new AStar();
            List<Board> newSolution = tempSolver.solve(tempBoard, heuristic);

            if (newSolution != null && newSolution.size() - 1 >= currentDepth) {
                // Accept car
                vehicles.put(nextId++, candidate);
                placeVehicleOnGrid(candidate, occupied, rowFill, colFill, rowOrientation, colOrientation);
                currentDepth = newSolution.size() - 1;
            }
        }

        // Accept only puzzles above desired difficulty
        if (currentDepth >= minDepth) {
            return new Board(vehicles, BOARD_SIZE);
        } else {
            return generateLevel(maxVehicles, minDepth); // try again
        }
    }

    private static Map<Integer, Vehicle> cloneVehicleMap(Map<Integer, Vehicle> original) {
        Map<Integer, Vehicle> clone = new HashMap<>();
        for (Map.Entry<Integer, Vehicle> entry : original.entrySet()) {
            clone.put(entry.getKey(), entry.getValue().clone());
        }
        return clone;
    }

    private static boolean isValidPlacement(Vehicle v, boolean[][] occupied, int[] rowFill, int[] colFill,
                                            Map<Integer, Set<Boolean>> rowOrientation, Map<Integer, Set<Boolean>> colOrientation) {
        int x = v.getStartX();
        int y = v.getStartY();
        boolean isHorizontal = v.getIsHorizontal();

        int[] tempRowFill = rowFill.clone();
        int[] tempColFill = colFill.clone();
        Map<Integer, Set<Boolean>> tempRowOrientation = cloneOrientationMap(rowOrientation);
        Map<Integer, Set<Boolean>> tempColOrientation = cloneOrientationMap(colOrientation);

        for (int i = 0; i < v.getLength(); i++) {
            if (x >= BOARD_SIZE || y >= BOARD_SIZE || occupied[y][x]) return false;

            tempRowFill[y]++;
            tempColFill[x]++;
            tempRowOrientation.computeIfAbsent(y, k -> new HashSet<>()).add(isHorizontal);
            tempColOrientation.computeIfAbsent(x, k -> new HashSet<>()).add(isHorizontal);

            if (tempRowFill[y] == BOARD_SIZE && tempRowOrientation.get(y).size() == 1 && tempRowOrientation.get(y).contains(true)) return false;
            if (tempColFill[x] == BOARD_SIZE && tempColOrientation.get(x).size() == 1 && tempColOrientation.get(x).contains(false)) return false;

            if (isHorizontal) x++;
            else y++;
        }

        return true;
    }

    private static Map<Integer, Set<Boolean>> cloneOrientationMap(Map<Integer, Set<Boolean>> original) {
        Map<Integer, Set<Boolean>> clone = new HashMap<>();
        for (Map.Entry<Integer, Set<Boolean>> entry : original.entrySet()) {
            clone.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        return clone;
    }

    private static void placeVehicleOnGrid(Vehicle v, boolean[][] occupied, int[] rowFill, int[] colFill,
                                           Map<Integer, Set<Boolean>> rowOrientation, Map<Integer, Set<Boolean>> colOrientation) {
        int x = v.getStartX();
        int y = v.getStartY();
        boolean isHorizontal = v.getIsHorizontal();

        for (int i = 0; i < v.getLength(); i++) {
            occupied[y][x] = true;
            rowFill[y]++;
            colFill[x]++;
            rowOrientation.computeIfAbsent(y, k -> new HashSet<>()).add(isHorizontal);
            colOrientation.computeIfAbsent(x, k -> new HashSet<>()).add(isHorizontal);

            if (isHorizontal) x++;
            else y++;
        }
    }

    // ------------------------------------------
    // ✅ Test Main Method
    // ------------------------------------------
//    public static void main(String[] args) {
//        int numLevels = 10000;
//        int maxCars = 12;
//        int minSteps = 20;
//
//        for (int i = 1; i <= numLevels; i++) {
//            Board board = generateLevel(maxCars, minSteps);
//            System.out.println("=== Puzzle " + i + " ===");
//            board.printGrid();
//
//            AStar solver = new AStar();
//            List<Board> solution = solver.solve(board, new ChainBlockingHeuristic());
//
//            if (solution != null) {
//                System.out.println("✅ Solved in " + (solution.size() - 1) + " steps");
//            } else {
//                System.out.println("❌ Unsolvable puzzle (unexpected)");
//            }
//
//            System.out.println();
//        }
//    }

public static void main(String[] args) {
        int totalToGenerate = 100;
        int maxCars = 10;
        int minSteps = 10;

        List<Board> easyPuzzles = new ArrayList<>();
        List<Board> mediumPuzzles = new ArrayList<>();
        List<Board> hardPuzzles = new ArrayList<>();
        List<Board> veryHardPuzzles = new ArrayList<>();
        List<Board> extremePuzzles = new ArrayList<>();

        int attempts = 0;

        while ((easyPuzzles.size() + mediumPuzzles.size() + hardPuzzles.size()) < totalToGenerate) {
            Board board = generateLevel(maxCars, minSteps);
            AStar solver = new AStar();
            Heuristic heuristic = new BlockingHeuristic();
            List<Board> solution = solver.solve(board, heuristic);
            attempts++;

            if (solution == null) continue;

            int depth = solution.size() - 1;

            if (depth <= 4) continue; // too trivial, skip

            if (depth <= 15) {
                easyPuzzles.add(board);
            } else if (depth <= 25) {
                mediumPuzzles.add(board);
            } else if (depth <= 35) {
                hardPuzzles.add(board);
            } else if (depth <= 45){
                veryHardPuzzles.add(board);
            } else {
                extremePuzzles.add(board);
            }
        }

        System.out.println("Generated after " + attempts + " attempts:");
        System.out.println("🟢 Easy:   " + easyPuzzles.size());
        System.out.println("🟡 Medium: " + mediumPuzzles.size());
        System.out.println("🔴 Hard:   " + hardPuzzles.size());
        System.out.println("⚫ Very Hard: " + veryHardPuzzles.size());
        System.out.println("⚪ Extreme: " + extremePuzzles.size());

        // Preview one of each
        if (!easyPuzzles.isEmpty()) {
            System.out.println("\nExample EASY puzzle:");
            easyPuzzles.get(0).printGrid();
        }
        if (!mediumPuzzles.isEmpty()) {
            System.out.println("\nExample MEDIUM puzzle:");
            mediumPuzzles.get(0).printGrid();
        }
        if (!hardPuzzles.isEmpty()) {
            System.out.println("\nExample HARD puzzle:");
            hardPuzzles.get(0).printGrid();
        }
    }

}

