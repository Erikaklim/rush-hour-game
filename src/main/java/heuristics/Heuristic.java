package heuristics;

import solver.Board;

public interface Heuristic {
    int calculateH(Board board);
}
