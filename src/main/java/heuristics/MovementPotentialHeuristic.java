package heuristics;

import solver.Board;
import solver.Vehicle;

import java.util.ArrayList;
import java.util.List;

public class MovementPotentialHeuristic implements Heuristic {

    @Override
    public int calculateH(Board board) {
        Vehicle redCar = board.getRedCar();
        int h = 0;

        List<Integer> blockingVehicles = getBlockingVehicleIds(board, redCar);

        for (Integer vehicleId : blockingVehicles) {
            Vehicle blockingVehicle = board.getVehicleById(vehicleId);
            h += canVehicleBeMoved(blockingVehicle, board) ? 1 : 2;
        }

        return h;
    }

    private boolean canVehicleBeMoved(Vehicle vehicle, Board board) {
        if (vehicle.getIsHorizontal()) {
            return canMove(vehicle, board, -1, 0) || canMove(vehicle, board, 1, 0);
        } else {
            return canMove(vehicle, board, 0, -1) || canMove(vehicle, board, 0, 1);
        }
    }
    private boolean canMove(Vehicle vehicle, Board board, int dx, int dy) {
        int x, y;
        int[][] grid = board.getGrid();

        if (dx == -1 || dy == -1) {
            x = vehicle.getStartX() + (dx == -1 ? -1 : 0);
            y = vehicle.getStartY() + (dy == -1 ? -1 : 0);
        } else {
            x = vehicle.getIsHorizontal() ? vehicle.getStartX() + vehicle.getLength() : vehicle.getStartX();
            y = vehicle.getIsHorizontal() ? vehicle.getStartY() : vehicle.getStartY() + vehicle.getLength();
        }

        if (x >= 0 && x < board.getBoardSize() && y >= 0 && y < board.getBoardSize()) {
            return grid[y][x] == 0;
        }

        return false;
    }

    private List<Integer> getBlockingVehicleIds(Board board, Vehicle vehicle) {
        List<Integer> blockingVehicleIds = new ArrayList<>();
        int x = vehicle.getStartX() + vehicle.getLength();
        int y = vehicle.getStartY();
        int[][] grid = board.getGrid();

        while (x < board.getBoardSize()) {
            int cellValue = grid[y][x];
            if (cellValue != 0 && !blockingVehicleIds.contains(cellValue)) {
                blockingVehicleIds.add(cellValue);
            }
            x++;
        }
        return blockingVehicleIds;
    }
}

