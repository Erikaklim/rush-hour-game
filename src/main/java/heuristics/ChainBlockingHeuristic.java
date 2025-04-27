package heuristics;

import solver.Board;
import solver.Vehicle;

import java.util.*;

public class ChainBlockingHeuristic implements Heuristic {
    int blockingVehicles = 0;
    List<Integer> blockVehicles = new ArrayList<>();

    @Override
    public int calculateH(Board board) {
        Vehicle redCar = board.getRedCar();
        int[][] grid = board.getGrid();
        int boardSize = board.getBoardSize();

        Set<Integer> visitedVehicles = new HashSet<>();

        int x = redCar.getStartX() + redCar.getLength();
        int y = redCar.getStartY();
        visitedVehicles.add(redCar.getId());

        while (x < boardSize) {
            int vehicleId = grid[y][x];
            if (isValidVehicle(vehicleId, visitedVehicles)) {
                Vehicle vehicle = board.getVehicleById(vehicleId);
                if (vehicle == null) {
                    x++;
                    continue;
                }
                blockingVehicles++;
                blockVehicles.add(vehicleId);
                exploreBlockingVehicles(vehicleId, board, visitedVehicles);
            }
            x++;
        }

        return blockingVehicles;
    }

    private void exploreBlockingVehicles(int vehicleId, Board board, Set<Integer> visitedVehicles) {
        Queue<Integer> queue = new LinkedList<>();
        queue.add(vehicleId);

        while (!queue.isEmpty()) {
            int currentVehicleId = queue.poll();
            Vehicle currentVehicle = board.getVehicleById(currentVehicleId);
            if (currentVehicle == null) continue;
            addBlockingVehiclesToQueue(currentVehicle, board, visitedVehicles, queue);
        }
    }

    private void addBlockingVehiclesToQueue(Vehicle vehicle, Board board, Set<Integer> visitedVehicles, Queue<Integer> queue) {
        int[][] grid = board.getGrid();
        int boardSize = board.getBoardSize();
        List<int[]> blockingCells = getBlockingCells(vehicle, boardSize);
        for (int[] cell : blockingCells) {
            int blockerId = grid[cell[1]][cell[0]];
            if (isValidVehicle(blockerId, visitedVehicles)) {
                Vehicle blocker = board.getVehicleById(blockerId);
                if (blocker == null) continue;
                if (!canClearPath(blocker, cell, board)) {
                    queue.add(blockerId);
                    blockingVehicles++;
                    blockVehicles.add(blockerId);
                }
            }
        }
    }

    private boolean canClearPath(Vehicle blocker, int[] cell, Board board) {
        if (blocker == null) return true;

        int startX = blocker.getStartX();
        int startY = blocker.getStartY();
        int endX = startX + blocker.getLength() - 1;
        int endY = startY + blocker.getLength() - 1;

        if (blocker.getIsHorizontal()) {
            if (blocker.getLength() == 2) {
                if (cell[0] == startX) {
                    return canMove(blocker, board, 1) || canMove(blocker, board, -2);
                }
                if (cell[0] == endX) {
                    return canMove(blocker, board, -1) || canMove(blocker, board, 2);
                }
            } else {
                if (cell[0] == startX) {
                    return canMove(blocker, board, 1) || canMove(blocker, board, -3);
                }
                if (cell[0] == endX) {
                    return canMove(blocker, board, -1) || canMove(blocker, board, 3);
                }
                int middle = startX + 1;
                if (cell[0] == middle) {
                    return canMove(blocker, board, 2) || canMove(blocker, board, -2);
                }
            }
        } else {
            if (blocker.getLength() == 2) {
                if (cell[1] == startY) {
                    return canMove(blocker, board, 1) || canMove(blocker, board, -2);
                }
                if (cell[1] == endY) {
                    return canMove(blocker, board, -1) || canMove(blocker, board, 2);
                }
            } else {
                if (cell[1] == startY) {
                    return canMove(blocker, board, 1) || canMove(blocker, board, -3);
                }
                if (cell[1] == endY) {
                    return canMove(blocker, board, -1) || canMove(blocker, board, 3);
                }
                int middle = startY + 1;
                if (cell[1] == middle) {
                    return canMove(blocker, board, 2) || canMove(blocker, board, -2);
                }
            }
        }

        return false;
    }

    private boolean canMove(Vehicle vehicle, Board board, int steps) {
        int[][] grid = board.getGrid();
        int boardSize = board.getBoardSize();

        int startX = vehicle.getStartX();
        int startY = vehicle.getStartY();
        int length = vehicle.getLength();

        if (vehicle.getIsHorizontal()) {
            if (steps > 0) {
                for (int i = 1; i <= steps; i++) {
                    int x = startX + length - 1 + i;
                    if (x >= boardSize || grid[startY][x] != 0) {
                        return false;
                    }
                }
            } else {
                for (int i = 1; i <= Math.abs(steps); i++) {
                    int x = startX - i;
                    if (x < 0 || grid[startY][x] != 0) {
                        return false;
                    }
                }
            }
        } else {
            if (steps > 0) {
                for (int i = 1; i <= steps; i++) {
                    int y = startY + length - 1 + i;
                    if (y >= boardSize || grid[y][startX] != 0) {
                        return false;
                    }
                }
            } else {
                for (int i = 1; i <= Math.abs(steps); i++) {
                    int y = startY - i;
                    if (y < 0 || grid[y][startX] != 0) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private List<int[]> getBlockingCells(Vehicle vehicle, int boardSize) {
        List<int[]> cells = new ArrayList<>();
        int startX = vehicle.getStartX();
        int startY = vehicle.getStartY();
        int length = vehicle.getLength();

        if (vehicle.getIsHorizontal()) {
            if (startX - 1 >= 0) {
                cells.add(new int[]{startX - 1, startY});
            }
            if (startX + length < boardSize) {
                cells.add(new int[]{startX + length, startY});
            }
        } else {
            if (startY - 1 >= 0) {
                cells.add(new int[]{startX, startY - 1});
            }
            if (startY + length < boardSize) {
                cells.add(new int[]{startX, startY + length});
            }
        }

        return cells;
    }

    private boolean isValidVehicle(int vehicleId, Set<Integer> visitedVehicles) {
        return vehicleId != 0 && visitedVehicles.add(vehicleId);
    }
}



//package heuristics;
//import algorithm.Board;
//import algorithm.Vehicle;
//
//import java.util.*;
//
//import java.util.HashSet;
//import java.util.LinkedList;
//import java.util.Queue;
//import java.util.Set;
//
//
//public class ChainBlockingHeuristic implements Heuristic {
//    int blockingVehicles = 0;
//    List<Integer> blockVehicles = new ArrayList<>();
//
//    @Override
//    public int calculateH(Board board) {
//        Vehicle redCar = board.getRedCar();
//        int[][] grid = board.getGrid();
//        int boardSize = board.getBoardSize();
//
//        Set<Integer> visitedVehicles = new HashSet<>();
//
//        int x = redCar.getStartX() + redCar.getLength();
//        int y = redCar.getStartY();
//        visitedVehicles.add(redCar.getId());
//
//        while (x < boardSize) {
//            int vehicleId = grid[y][x];
//            if (isValidVehicle(vehicleId, visitedVehicles)) {
//                blockingVehicles++;
//                blockVehicles.add(vehicleId);
//                exploreBlockingVehicles(vehicleId, board, visitedVehicles);
//            }
//            x++;
//        }
//
//        return blockingVehicles;
//    }
//    private void exploreBlockingVehicles(int vehicleId, Board board, Set<Integer> visitedVehicles) {
//        Queue<Integer> queue = new LinkedList<>();
//        queue.add(vehicleId);
//
//        while (!queue.isEmpty()) {
//            int currentVehicleId = queue.poll();
//            Vehicle currentVehicle = board.getVehicleById(currentVehicleId);
//            addBlockingVehiclesToQueue(currentVehicle, board, visitedVehicles, queue);
//        }
//    }
//
//    private void addBlockingVehiclesToQueue(Vehicle vehicle, Board board, Set<Integer> visitedVehicles, Queue<Integer> queue) {
//        int[][] grid = board.getGrid();
//        int boardSize = board.getBoardSize();
//        List<int[]> blockingCells = getBlockingCells(vehicle, boardSize);
//        for (int[] cell : blockingCells) {
//            int blockerId = grid[cell[1]][cell[0]];
//            if (isValidVehicle(blockerId, visitedVehicles)) {
//                if(!canClearPath(board.getVehicleById(blockerId), cell, board)) {
//                    queue.add(blockerId);
//                    blockingVehicles++;
//                    blockVehicles.add(blockerId);
//
//                }
//            }
//        }
//    }
//
//
//    private boolean canClearPath(Vehicle blocker, int[] cell, Board board){
//        int startX = blocker.getStartX();
//        int startY = blocker.getStartY();
//        int endX = startX + blocker.getLength() - 1;
//        int endY = startY + blocker.getLength() - 1;
//
//        if(blocker.getIsHorizontal()){
//            if(blocker.getLength() == 2){
//                if(cell[0] == startX){
//                    return canMove(blocker, board, 1) || canMove(blocker, board, -2);
//                }
//                if(cell[0] == endX){
//                    return canMove(blocker, board, -1) || canMove(blocker, board, 2);
//                }
//            }else{
//                if(cell[0] == startX){
//                    return canMove(blocker, board, 1)|| canMove(blocker, board, -3);
//                }
//                if(cell[0] == endX){
//                    return canMove(blocker, board, -1) ||  canMove(blocker, board, 3);
//                }
//                int middle = startX + 1;
//                if(cell[0] == middle){
//                    return canMove(blocker, board, 2) || canMove(blocker, board, 2);
//                }
//            }
//        }else{
//            if(blocker.getLength() == 2){
//                if(cell[1] == startY){
//                    return canMove(blocker, board, 1) || canMove(blocker, board, -2);
//                }
//                if(cell[1] == endY){
//                    return canMove(blocker, board, -1) || canMove(blocker, board, 2);
//                }
//            }else{
//                if(cell[1] == startY){
//                    return canMove(blocker, board, 1) || canMove(blocker, board, -3);
//                }
//                if(cell[1] == endY){
//                    return canMove(blocker, board, -1) || canMove(blocker, board, 3);
//                }
//                int middle = startY + 1;
//                if(cell[1] == middle){
//                    return canMove(blocker, board, 2) || canMove(blocker, board, 2);
//                }
//            }
//        }
//
//        return false;
//    }
//
//    private boolean canMove(Vehicle vehicle, Board board, int steps) {
//        int[][] grid = board.getGrid();
//        int boardSize = board.getBoardSize();
//
//        int startX = vehicle.getStartX();
//        int startY = vehicle.getStartY();
//        int length = vehicle.getLength();
//
//        if (vehicle.getIsHorizontal()) {
//            if (steps > 0) {
//                for (int i = 1; i <= steps; i++) {
//                    int x = startX + length - 1 + i;
//                    if (x >= boardSize || grid[startY][x] != 0) {
//                        return false;
//                    }
//                }
//            } else {
//                for (int i = 1; i <= Math.abs(steps); i++) {
//                    int x = startX - i;
//                    if (x < 0 || grid[startY][x] != 0) {
//                        return false;
//                    }
//                }
//            }
//        } else {
//            if (steps > 0) {
//                for (int i = 1; i <= steps; i++) {
//                    int y = startY + length - 1 + i;
//                    if (y >= boardSize || grid[y][startX] != 0) {
//                        return false;
//                    }
//                }
//            } else {
//                for (int i = 1; i <= Math.abs(steps); i++) {
//                    int y = startY - i;
//                    if (y < 0 || grid[y][startX] != 0) {
//                        return false;
//                    }
//                }
//            }
//        }
//
//        return true;
//    }
//
//    private List<int[]> getBlockingCells (Vehicle vehicle, int boardSize){
//        List<int[]> cells = new ArrayList<>();
//        int startX = vehicle.getStartX();
//        int startY = vehicle.getStartY();
//        int length = vehicle.getLength();
//
//        if (vehicle.getIsHorizontal()) {
//            if (startX - 1 >= 0) {
//                cells.add(new int[]{startX - 1, startY});
//            }
//            if (startX + length < boardSize) {
//                cells.add(new int[]{startX + length, startY});
//            }
//        } else {
//            if (startY - 1 >= 0) {
//                cells.add(new int[]{startX, startY - 1});
//            }
//            if (startY + length < boardSize) {
//                cells.add(new int[]{startX, startY + length});
//            }
//        }
//
//        return cells;
//    }
//    private boolean isValidVehicle ( int vehicleId, Set<Integer > visitedVehicles){
//        return vehicleId != 0 && visitedVehicles.add(vehicleId);
//    }
//
//}