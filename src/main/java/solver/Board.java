package solver;

import java.util.*;

public class Board implements Comparable<Board> {
    public static final int TARGET_VEHICLE_ID = 1;
    private int boardSize;
    private int[][] grid;
    private Map<Integer, Vehicle> vehicles;

    public Board(Map<Integer, Vehicle> vehicles, int boardSize) {
        this.vehicles = vehicles;
        this.boardSize = boardSize;
        grid = new int[boardSize][boardSize];
        initializeGrid();
    }

    private void initializeGrid(){
        for (Vehicle v : vehicles.values()){
            manipulateVehicleOnGrid(v, true);
        }
    }

    public Map<Integer, Vehicle> getVehicles() {
        return vehicles;
    }

    public void manipulateVehicleOnGrid(Vehicle v, boolean place){
        int x = v.getStartX();
        int y = v.getStartY();
        for(int i = 0; i < v.getLength(); i++){
            if(place){
                grid[y][x] = v.getId();
            }else{
                grid[y][x] = 0;
            }
            if (v.getIsHorizontal()) {
                x++;
            } else {
                y++;
            }
        }
    }

    public boolean isGoal(){
        Vehicle v = getRedCar();
        return v.getStartX() + v.getLength() == boardSize;
    }

    public List<Board> expand(){
        List<Board> successors = new ArrayList<>();

        for(Vehicle v : vehicles.values()){
            successors.addAll(generateMoves(v, 1));
            successors.addAll(generateMoves(v, -1));
        }
        return successors;
    }

    public List<Board> generateMoves(Vehicle v, int direction){
        List<Board> moves = new ArrayList<>();
        int steps = 1;

        while (true) {
            if (canMove(v, direction, steps)) {
                Board newBoard = this.clone();
                newBoard.moveVehicle(v.getId(), direction, steps);
                moves.add(newBoard);
                steps++;
            } else {
                break;
            }
        }

        return moves;
    }
//public List<Board> generateMoves(Vehicle v, int direction) {
//    List<Board> moves = new ArrayList<>();
//    int steps = 1;
//
//    while (steps <= boardSize) { // 🛑 prevent infinite expansion
//        if (canMove(v, direction, steps)) {
//            Board newBoard = this.clone();
//            newBoard.moveVehicle(v.getId(), direction, steps);
//            moves.add(newBoard);
//            steps++;
//        } else {
//            break;
//        }
//    }
//
//    return moves;
//}


    public boolean canMove(Vehicle v, int direction, int steps){
        int x = v.getStartX();
        int y = v.getStartY();

        int checkX = x;
        int checkY = y;

        for (int i = 1; i <= steps; i++) {
            if (v.getIsHorizontal()) {
                if (direction == 1) {
                    checkX = x + v.getLength() - 1 + i;
                } else {
                    checkX = x - i;
                }
                checkY = y;
            } else {
                if (direction == 1) {
                    checkY = y + v.getLength() - 1 + i;
                } else {
                    checkY = y - i;
                }
                checkX = x;
            }

            if (!isValidMove(checkX, checkY)) {
                return false;
            }
        }

        return true;
    }


    public void moveVehicle(int id, int direction, int steps){
        Vehicle v = vehicles.get(id);
        manipulateVehicleOnGrid(v, false);

        if (v.getIsHorizontal()) {
            v.setStartX(v.getStartX() + direction * steps);
        } else {
            v.setStartY(v.getStartY() + direction * steps);
        }

        manipulateVehicleOnGrid(v, true);
    }

    private boolean isValidMove(int x, int y){
        return x >= 0 && y >= 0 && x < boardSize && y < boardSize
                && isCellEmpty(x, y);
    }

    public boolean isCellEmpty(int x, int y){
        return grid[y][x] == 0;
    }
    public Vehicle getRedCar(){
        return vehicles.get(TARGET_VEHICLE_ID);
    }
    public int[][] getGrid(){
        return grid;
    }
    public int getBoardSize() {
        return boardSize;
    }

    @Override
    public Board clone(){
        Map<Integer, Vehicle> clonedVehicles = new HashMap<>();
        for(Map.Entry<Integer, Vehicle> entry : vehicles.entrySet()){
            clonedVehicles.put(entry.getKey(), entry.getValue().clone());
        }
        Board newBoard = new Board(clonedVehicles, boardSize);

        for(int y = 0; y < boardSize; y++){
            newBoard.grid[y] = this.grid[y].clone();
        }
        return newBoard;
    }

    public void printGrid() {
        System.out.print(" x ");
        for (int x = 0; x < boardSize; x++) {
            System.out.print(x + " ");
        }
        System.out.println();

        System.out.print("y ");
        for (int x = 0; x < boardSize; x++) {
            System.out.print("‾‾");
        }
        System.out.println("‾");

        for (int y = 0; y < boardSize; y++) {
            System.out.print(y + "| ");

            for (int x = 0; x < boardSize; x++) {
                int cellValue = grid[y][x];
                if (cellValue == 0) {
                    System.out.print(". ");
                } else if (cellValue == TARGET_VEHICLE_ID) {
                    System.out.print("X ");
                } else {
                    System.out.print(cellValue + " ");
                }
            }

            System.out.println("| ");
        }
        System.out.print("  ");
        for (int x = 0; x < boardSize; x++) {
            System.out.print("‾‾");
        }
        System.out.println("‾\n");
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Board)) return false;
        Board other = (Board) obj;
        return Arrays.deepEquals(this.grid, other.grid);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(grid);
    }


//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj) return true;
//        if (!(obj instanceof Board)) return false;
//        Board other = (Board) obj;
//        return Arrays.deepEquals(this.grid, other.grid);
//    }
//
//    @Override
//    public int hashCode() {
//        return Arrays.deepHashCode(grid);
//    }

    @Override
    public int compareTo(Board other) {
        String thisState = this.serializeBoardState();
        String otherState = other.serializeBoardState();

        return thisState.compareTo(otherState);
    }

    public String serializeBoardState() {
        StringBuilder sb = new StringBuilder(boardSize * boardSize);
        for (int y = 0; y < boardSize; y++) {
            for (int x = 0; x < boardSize; x++) {
                sb.append(grid[y][x]);
            }
        }
        return sb.toString();
    }

    public Vehicle getVehicleById(int id){
        return vehicles.get(id);
    }



}

