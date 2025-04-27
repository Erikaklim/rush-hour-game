package generator.evolution;

import io.jenetics.AnyGene;
import io.jenetics.Genotype;
import solver.Board;
import solver.AStar;
import heuristics.BlockingHeuristic;

import java.util.List;
import java.util.function.Function;

public class FitnessFunction implements Function<Genotype<AnyGene<Board>>, Double> {

    @Override
    public Double apply(Genotype<AnyGene<Board>> genotype) {
        Board board = genotype.chromosome().gene().allele(); // ✅ correct in 8.2

        AStar solver = new AStar();
        BlockingHeuristic heuristic = new BlockingHeuristic();
        List<Board> solution = solver.solve(board, heuristic);

        if (solution == null) {
            return -10000.0; // Punish unsolvable
        } else {
            return (double) solution.size(); // Reward longer solutions
        }
    }
}
