package generator;//package generator;
//
//import org.chocosolver.solver.Model;
//import org.chocosolver.solver.Solver;
//import org.chocosolver.solver.variables.*;
//import org.chocosolver.solver.constraints.nary.cnf.LogOp;
//
//import java.util.*;
//
//public class ChocoGenerator {
//    private static final int BOARD_SIZE = 6;
//    private static final int RED_CAR_ID = 1;
//    private static final int EXIT_ROW = 2;
//    private static final int MAX_VEHICLES = 12;
//    private static final int MAX_ATTEMPTS = 1000;
//
//    static class Car {
//        int id;
//        IntVar x, y;
//        IntVar length;
//        BoolVar isHorizontal;
//        SetVar occupied;
//
//        Car(int id) {
//            this.id = id;
//        }
//    }
//
//    public static void main(String[] args) {
//        for (int i = 1; i <= 5; i++) {
//            generateAndPrintLevel(i);
//        }
//    }
//
//    private static void generateAndPrintLevel(int puzzleNumber) {
//        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
//            Model model = new Model("RushHour-Level-" + puzzleNumber);
//
//            List<Car> cars = new ArrayList<>();
//
//            // 1. Red car (always horizontal at EXIT_ROW)
//            Car red = new Car(RED_CAR_ID);
//            red.x = model.intVar("red_x", 0, BOARD_SIZE - 2);
//            red.y = model.intVar(EXIT_ROW);
//            red.length = model.intVar(2); // fixed
//            red.isHorizontal = model.boolVar(true);
//            red.occupied = createOccupiedSet(model, red);
//            cars.add(red);
//
//            // 2. Add random cars (IDs 2+)
//            for (int id = 2; id <= MAX_VEHICLES; id++) {
//                Car c = new Car(id);
//                c.length = model.intVar("len_" + id, 2, 3);
//                c.isHorizontal = model.boolVar("isHorizontal_" + id);
//                c.x = model.intVar("x_" + id, 0, BOARD_SIZE - 1);
//                c.y = model.intVar("y_" + id, 0, BOARD_SIZE - 1);
//                c.occupied = createOccupiedSet(model, c);
//                constrainBounds(model, c);
//                cars.add(c);
//            }
//
//            // 3. Disjoint occupied cells (no overlap)
//            SetVar[] allOccupied = cars.stream().map(c -> c.occupied).toArray(SetVar[]::new);
//            model.allDisjoint(allOccupied).post();
//
//            // 4. Blocking chain modeling: red car must be blocked by at least one car
//            model.min(red.x).le(BOARD_SIZE - 2).post(); // red must not start at exit
//
//            // 5. Solve and print
//            Solver solver = model.getSolver();
//            if (solver.solve()) {
//                System.out.println("=== Puzzle " + puzzleNumber + " ===");
//                printSolution(cars);
//                return;
//            }
//        }
//
//        System.out.println("❌ Failed to generate Puzzle " + puzzleNumber);
//    }
//
//    private static void constrainBounds(Model model, Car car) {
//        IntVar boardLimit = model.intVar(0, BOARD_SIZE - 1);
//
//        // X + len <= 6 if horizontal, else Y + len <= 6
//        IntVar xEnd = model.intVar("xEnd_" + car.id, 0, BOARD_SIZE);
//        IntVar yEnd = model.intVar("yEnd_" + car.id, 0, BOARD_SIZE);
//        model.arithm(xEnd, "=", car.x, "+", car.length, "-", 1).post();
//        model.arithm(yEnd, "=", car.y, "+", car.length, "-", 1).post();
//
//        model.ifThen(
//                car.isHorizontal,
//                model.arithm(xEnd, "<", BOARD_SIZE)
//        );
//        model.ifThen(
//                model.not(car.isHorizontal),
//                model.arithm(yEnd, "<", BOARD_SIZE)
//        );
//    }
//
//    private static SetVar createOccupiedSet(Model model, Car car) {
//        int maxCells = BOARD_SIZE * BOARD_SIZE;
//        IntVar[] indices = new IntVar[car.length.getUB()];
//        for (int i = 0; i < indices.length; i++) {
//            IntVar index;
//            if (car.isHorizontal == null) {
//                // Red car
//                index = model.intOffsetView(car.x, i);
//            } else {
//                IntVar dx = model.intVar("dx_" + car.id + "_" + i, 0, BOARD_SIZE - 1);
//                IntVar dy = model.intVar("dy_" + car.id + "_" + i, 0, BOARD_SIZE - 1);
//
//                // dx = x + i if horizontal
//                model.ifThenElse(
//                        car.isHorizontal,
//                        model.arithm(dx, "=", car.x, "+", i),
//                        model.arithm(dx, "=", car.x)
//                );
//
//                // dy = y + i if vertical
//                model.ifThenElse(
//                        car.isHorizontal,
//                        model.arithm(dy, "=", car.y),
//                        model.arithm(dy, "=", car.y, "+", i)
//                );
//
//                index = model.intVar("cell_" + car.id + "_" + i, 0, maxCells - 1);
//                model.scalar(new IntVar[]{dy, dx}, new int[]{BOARD_SIZE, 1}, "=", index).post();
//            }
//            indices[i] = model.intVar("idx_" + car.id + "_" + i, 0, BOARD_SIZE * BOARD_SIZE - 1);
//        }
//
//        SetVar occupied = model.setVar("occ_" + car.id, new int[]{}, range(0, maxCells - 1));
//        model.setBoolsChanneling(indices, occupied).post();
//        return occupied;
//    }
//
//    private static int[] range(int start, int end) {
//        return java.util.stream.IntStream.rangeClosed(start, end).toArray();
//    }
//
//    private static void printSolution(List<Car> cars) {
//        String[][] grid = new String[BOARD_SIZE][BOARD_SIZE];
//        for (String[] row : grid) Arrays.fill(row, ".");
//
//        for (Car car : cars) {
//            try {
//                int id = car.id;
//                int len = car.length.getValue();
//                int x = car.x.getValue();
//                int y = car.y.getValue();
//                boolean horizontal = car.isHorizontal.getValue() == 1;
//
//                for (int i = 0; i < len; i++) {
//                    int px = horizontal ? x + i : x;
//                    int py = horizontal ? y : y + i;
//                    if (px >= 0 && px < BOARD_SIZE && py >= 0 && py < BOARD_SIZE)
//                        grid[py][px] = (id == RED_CAR_ID) ? "X" : String.valueOf(id);
//                }
//            } catch (Exception e) {
//                System.out.println("Error rendering car " + car.id);
//            }
//        }
//
//        System.out.print(" x ");
//        for (int x = 0; x < BOARD_SIZE; x++) System.out.print(x + " ");
//        System.out.println("\ny " + "‾‾".repeat(BOARD_SIZE) + "‾");
//
//        for (int y = 0; y < BOARD_SIZE; y++) {
//            System.out.print(y + "| ");
//            for (int x = 0; x < BOARD_SIZE; x++) {
//                System.out.print(grid[y][x] + " ");
//            }
//            System.out.println("|");
//        }
//        System.out.print("  ");
//        System.out.println("‾‾".repeat(BOARD_SIZE) + "‾\n");
//    }
//}


