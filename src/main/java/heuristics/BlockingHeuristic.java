package heuristics;

import solver.Board;
import solver.Vehicle;

public class BlockingHeuristic implements Heuristic{
    @Override
    public int calculateH(Board board) {
        Vehicle redCar = board.getRedCar();
        int blockingVehicles = 0;
        int x = redCar.getStartX() + redCar.getLength();
        int y = redCar.getStartY();
        while(x < board.getBoardSize()){
            if(board.getGrid()[y][x] != 0){
                blockingVehicles++;
            }
            x++;
        }

        return blockingVehicles;
    }
}
