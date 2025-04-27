package generator.evolution;

import io.jenetics.AnyChromosome;
import io.jenetics.AnyGene;
import io.jenetics.Genotype;
import solver.Board;
import solver.Vehicle;

import java.util.*;
import java.util.function.Predicate;

public class BoardFactory {

    private final int boardSize;
    private final Random rng;

    public BoardFactory(int boardSize, Random rng) {
        this.boardSize = boardSize;
        this.rng = rng;
    }

    public Genotype<AnyGene<Board>> create() {
        return Genotype.of(
                AnyChromosome.of(
                        this::generateRandomBoard,
                        Predicate.not(Objects::isNull),
                        1
                )
        );
    }

    private Board generateRandomBoard() {
        Map<Integer, Vehicle> vehicles = new HashMap<>();
        int id = 1;

        int redCarY = rng.nextInt(boardSize);
        Vehicle redCar = new Vehicle(id++, 0, redCarY, 2, true);
        vehicles.put(redCar.getId(), redCar);

        int vehicleCount = 8 + rng.nextInt(6);

        for (int i = 0; i < vehicleCount; i++) {
            boolean horizontal = rng.nextBoolean();
            int length = rng.nextBoolean() ? 2 : 3;
            int x = rng.nextInt(boardSize - (horizontal ? length : 0));
            int y = rng.nextInt(boardSize - (horizontal ? 0 : length));
            Vehicle newVehicle = new Vehicle(id, x, y, length, horizontal);

            Board tempBoard = new Board(new HashMap<>(vehicles), boardSize);
            if (tempBoard.isCellEmpty(x, y)) {
                vehicles.put(id++, newVehicle);
            }
        }

        return new Board(vehicles, boardSize);
    }
}
