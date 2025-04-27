package generator.evolution;

import io.jenetics.Alterer;
import io.jenetics.AltererResult;
import io.jenetics.AnyChromosome;
import io.jenetics.AnyGene;
import io.jenetics.Genotype;
import io.jenetics.Phenotype;
import io.jenetics.util.ISeq;
import io.jenetics.util.Seq;
import solver.Board;
import solver.Vehicle;

import java.util.*;
import java.util.function.Predicate;

public class BoardAlterer implements Alterer<AnyGene<Board>, Double> {

    private final Random rng = new Random();

    @Override
    public AltererResult<AnyGene<Board>, Double> alter(Seq<Phenotype<AnyGene<Board>, Double>> population, long generation) {
        List<Phenotype<AnyGene<Board>, Double>> newPopulation = new ArrayList<>();

        for (Phenotype<AnyGene<Board>, Double> pt : population) {
            Board originalBoard = pt.genotype().chromosome().gene().allele();
            Board mutatedBoard = originalBoard.clone();

            List<Vehicle> vehicles = new ArrayList<>(mutatedBoard.getVehicles().values());
            Vehicle vehicle = vehicles.get(rng.nextInt(vehicles.size()));
            int direction = rng.nextBoolean() ? 1 : -1;

            if (mutatedBoard.canMove(vehicle, direction, 1)) {
                mutatedBoard.moveVehicle(vehicle.getId(), direction, 1);
            }

            Genotype<AnyGene<Board>> newGenotype = Genotype.of(
                    AnyChromosome.of(
                            () -> mutatedBoard,
                            Predicate.not(Objects::isNull),
                            1
                    )
            );

            Phenotype<AnyGene<Board>, Double> newPt = Phenotype.of(newGenotype, generation);
            newPopulation.add(newPt);
        }

        // ✅ Correct for Jenetics 8.2: manually converting List -> Array -> ISeq
        ISeq<Phenotype<AnyGene<Board>, Double>> result = ISeq.of(newPopulation.toArray(Phenotype[]::new));

        return new AltererResult<>(result, 1);
    }
}


