import solver.AStar;
import solver.Board;
import solver.Puzzle;
import heuristics.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Puzzle puzzle = new Puzzle("puzzles.txt");

        Heuristic heuristic1 = new BlockingHeuristic();
        Heuristic heuristic2 = new MovementPotentialHeuristic();
        Heuristic heuristic3 = new ChainBlockingHeuristic();
        solveOnePuzzle(heuristic3, puzzle,0, false, true);
//      solveAllPuzzles(heuristic1, puzzle, false, true);

    }

    private static void printSolution(List<Board> solutionPath){
        if (solutionPath != null) {
            System.out.println("Solution found in " + (solutionPath.size() - 1) + " moves.");
            int step = 0;
            for (Board board : solutionPath) {
                System.out.println("Step " + step + ":");
                board.printGrid();
                step++;
            }
        } else {
            System.out.println("No solution found.");
        }

    }

    private static void printStats(int numOfCars, int nodesExpanded, int depth, long time, long memoryUsage){
        System.out.println("Number of cars: " + numOfCars);
        System.out.println("Nodes expanded: " + nodesExpanded);
        System.out.println("Depth: " + depth);
        System.out.println("Time taken: " + time + "ms");
        double memoryUsageInMB = (double) memoryUsage / (1024 * 1024);
        System.out.println("Memory usage: " + memoryUsageInMB + " MB");
        System.out.println("----------------------------------------");
    }

    private static void solveOnePuzzle(Heuristic heuristic, Puzzle puzzle, int id, boolean printBoard, boolean printStats){
//        Board board = puzzle.getRandomPuzzleBoard();
        Board board = puzzle.getPuzzleById(id);
        solve(heuristic, new ArrayList<>(Collections.singletonList(board)), printBoard, printStats);
    }

    private static void solveAllPuzzles(Heuristic heuristic, Puzzle puzzle, boolean printBoards, boolean printStats) {
        List<Board> boards = puzzle.getAllPuzzleBoards();
        solve(heuristic, boards, printBoards, printStats);

    }

    private static void solve(Heuristic heuristic, List<Board> boards, boolean printBoards, boolean printStats) {

        long totalNumOfCars = 0;
        long totalNodesExpanded = 0;
        long totalDepth = 0;
        long totalTimeTaken = 0;
        long totalMemoryUsage = 0;

        int puzzleNum = 1;
        for (Board board : boards) {
            System.out.println("Puzzle " + puzzleNum + ":");
            puzzleNum++;
            AStar solver = new AStar();

            long startTime = System.currentTimeMillis();
            long memoryUsageBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            List<Board> solutionPath = solver.solve(board, heuristic);

            long endTime = System.currentTimeMillis();
            long memoryUsageAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

            int numOfCars = board.getVehicles().size();
            int nodesExpanded = solver.getNodesExpanded();
            int depth = solutionPath.size() - 1;
            long timeTaken = endTime - startTime;
            long memoryUsed = Math.abs(memoryUsageAfter - memoryUsageBefore);

            if(printStats) printStats(numOfCars ,nodesExpanded, depth, timeTaken, memoryUsed);

            if(printBoards) printSolution(solutionPath);

            totalNumOfCars += numOfCars;
            totalNodesExpanded += nodesExpanded;
            totalDepth += depth;
            totalTimeTaken += timeTaken;
            totalMemoryUsage += memoryUsed;

        }

        int numPuzzles = boards.size();
        if (numPuzzles > 0) {
            long avgNumOfCars = totalNumOfCars / numPuzzles;
            long avgNodesExpanded = totalNodesExpanded / numPuzzles;
            double avgDepth = (double)totalDepth / (double)numPuzzles;
            long avgTimeTaken = totalTimeTaken / numPuzzles;
            double avgMemoryUsageInMB = (double) totalMemoryUsage / numPuzzles / (1024 * 1024); // Convert to MB

            System.out.println("Average Stats:");
            System.out.println("Number of puzzles: " + numPuzzles);
            System.out.println("Average Number of Cars: " + avgNumOfCars);
            System.out.println("Average Nodes Expanded: " + avgNodesExpanded);
            System.out.println("Average Depth: " + avgDepth);
            System.out.println("Average Time Taken: " + avgTimeTaken + "ms");
            System.out.println("Average Memory Usage: " + avgMemoryUsageInMB + " MB");
        } else {
            System.out.println("No puzzles to solve.");
        }
    }

}


