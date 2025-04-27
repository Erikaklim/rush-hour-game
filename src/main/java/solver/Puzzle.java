package solver;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Puzzle {
    private List<Board> puzzleBoards;

    public Puzzle(String filename) {
        InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
        if (is == null) {
            throw new IllegalArgumentException("File not found in resources: " + filename);
        }
        puzzleBoards = readFile(is);
    }

    private List<Board> readFile(InputStream inputStream) {
        List<Board> boards = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Puzzle")) {
                    // Read grid size
                    line = reader.readLine();
                    if (line == null) break;
                    int gridSize = Integer.parseInt(line.trim());

                    Map<Integer, Vehicle> vehicles = new HashMap<>();
                    int id = 1;

                    // Read vehicle data
                    while ((line = reader.readLine()) != null && !line.isEmpty() && !line.startsWith("Puzzle")) {
                        String[] parts = line.split(" ");
                        if (parts.length < 4) continue;

                        int x = Integer.parseInt(parts[0]);
                        int y = Integer.parseInt(parts[1]);
                        int length = Integer.parseInt(parts[2]);
                        boolean isHorizontal = parts[3].equalsIgnoreCase("h");

                        vehicles.put(id, new Vehicle(id, x, y, length, isHorizontal));
                        id++;
                    }

                    // Create and add the board
                    Board board = new Board(vehicles, gridSize);
                    boards.add(board);

                    // If we broke because we hit a new puzzle, put the line back
                    if (line != null && line.startsWith("Puzzle")) {
                        // This handles the edge case where there's no empty line between puzzles
                        // We'll process this line in the next iteration
                        continue;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to read puzzle file", e);
        }

        return boards;
    }
    public Board getRandomPuzzleBoard(){
        return puzzleBoards.get((int)(Math.random() * puzzleBoards.size()));
    }

    public List<Board> getAllPuzzleBoards(){
        return puzzleBoards;
    }

    public Board getPuzzleById(int id){
        return puzzleBoards.get(id);
    }

    public List<Board> getPuzzlesFromIndex(int begin, int end){
        List<Board> boards = new ArrayList<>();
        for(int i = begin; i < end; i++){
            boards.add(puzzleBoards.get(i));
        }
        return boards;
    }
}
