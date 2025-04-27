package generator.evolution;

import heuristics.BlockingHeuristic;
import heuristics.Heuristic;
import io.jenetics.*;
import io.jenetics.engine.*;
import solver.AStar;
import solver.Board;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EvolutionMain {

    public static void main(String[] args) {
        Random rng = new Random();
        int boardSize = 6;

        List<Board> easyPuzzles = new ArrayList<>();
        List<Board> mediumPuzzles = new ArrayList<>();
        List<Board> hardPuzzles = new ArrayList<>();
        List<Board> veryHardPuzzles = new ArrayList<>();
        List<Board> extremePuzzles = new ArrayList<>();

        for(int i = 0; i < 50; i++) {


            BoardFactory boardFactory = new BoardFactory(boardSize, rng);
            FitnessFunction fitnessFunction = new FitnessFunction();
            BoardAlterer boardAlterer = new BoardAlterer();

            Engine<AnyGene<Board>, Double> engine = Engine.builder(fitnessFunction, boardFactory::create)
                    .alterers(
                            boardAlterer,
                            new Mutator<>(0.1)
                    )
                    .offspringSelector(new TournamentSelector<>())
                    .survivorsSelector(new TournamentSelector<>())
                    .populationSize(50)
                    .maximizing()
                    .build();

            EvolutionStatistics<Double, ?> statistics = EvolutionStatistics.ofNumber();

            Phenotype<AnyGene<Board>, Double> best = engine.stream()
                    .limit(Limits.byFixedGeneration(200))
                    .peek(statistics)
                    .collect(EvolutionResult.toBestPhenotype());

            System.out.println("Best evolved Rush Hour board:");
            System.out.println("Fitness: " + best.fitness());
//        best.genotype().chromosome().gene().allele().printGrid(); // ✅ clean

            Board board = best.genotype().chromosome().gene().allele();
            board.printGrid();
            AStar solver = new AStar();
            Heuristic heuristic = new BlockingHeuristic();
            List<Board> solution = solver.solve(board, heuristic);
            if (solution == null) {
                System.out.println("No solution found.");
            } else {
                System.out.println("Solution found with " + solution.size() + " moves:");
            }

            int depth = solution.size();

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

        System.out.println("🟢 Easy:   " + easyPuzzles.size());
        System.out.println("🟡 Medium: " + mediumPuzzles.size());
        System.out.println("🔴 Hard:   " + hardPuzzles.size());
        System.out.println("⚫ Very Hard: " + veryHardPuzzles.size());
        System.out.println("⚪ Extreme: " + extremePuzzles.size());


//        System.out.println(statistics);
    }
}
