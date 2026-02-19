
import java.util.concurrent.ThreadLocalRandom;

public class App {
    public static void main(String[] args) throws Exception {
        Cube cube = new Cube();
        cube.displayCubeTerminal();
        cube.randomizeCube(50);
        cube.displayCubeTerminal();
    }
}

class Cube {
    private final char[] cube = {
        'w', 'w', 'w', 
        'w', 'w', 'w', 
        'w', 'w', 'w',
        // ^ Up Face
        'y', 'y', 'y', 
        'y', 'y', 'y', 
        'y', 'y', 'y',
        // ^ Down Face
        'g', 'g', 'g', 
        'g', 'g', 'g', 
        'g', 'g', 'g',
        // ^ Front Face
        'o', 'o', 'o', 
        'o', 'o', 'o', 
        'o', 'o', 'o',
        // ^ Left Face
        'b', 'b', 'b', 
        'b', 'b', 'b', 
        'b', 'b', 'b',
        // ^ Back Face
        'r', 'r', 'r', 
        'r', 'r', 'r', 
        'r', 'r', 'r',
        // ^ Right Face
    };

    private static final int[][] uSwitches = {
        {18, 27}, {19, 28}, {20, 29}, 
        {27, 36}, {28, 37}, {29, 38}, 
        {36, 45}, {37, 46}, {38, 47}, 
        {45, 18}, {46, 19}, {47, 20}, 
        {0, 2}, {1, 5}, {2, 8}, {5, 7}, 
        {8, 6}, {7, 3}, {6, 0}, {3, 1}
    };

    private static final int[][] dSwitches = {
        {33, 24}, {34, 25}, {35, 26},
        {42, 33}, {43, 34}, {44, 35},
        {51, 42}, {52, 43}, {53, 44},
        {24, 51}, {25, 52}, {26, 53},
        {9, 11}, {10, 14}, {11, 17}, {14, 16},
        {17, 15}, {16, 12}, {15, 9}, {12, 10}
    };

    private static final int[][] fSwitches = {
        {6, 45}, {7, 48}, {8, 51}, 
        {45, 11}, {48, 10}, {51, 9},
        {11, 35}, {10, 32}, {9, 29},
        {35, 6}, {32, 7}, {29, 8},
        {18, 20}, {19, 23}, {20, 26}, {23, 25},
        {26, 24}, {25, 21}, {24, 18}, {21, 19},
    };

    private static final int[][] bSwitches = {
        {0, 33}, {1, 30}, {2, 27},
        {33, 17}, {30, 16}, {27, 15},
        {17, 47}, {16, 50}, {15, 53}, 
        {47, 0}, {50, 1}, {53, 2},
        {36, 38}, {37, 41}, {38, 44}, {41, 43},
        {44, 42}, {43, 39}, {42, 36}, {39, 37}
    };

    private static final int[][] lSwitches = {
        {0, 18}, {3, 21}, {6, 24}, 
        {18, 9}, {21, 12}, {24, 15},
        {9, 44}, {12, 41}, {15, 38},
        {44, 0}, {41, 3}, {38, 6},
        {27, 29}, {28, 32}, {29, 35}, {32, 34},
        {35, 33}, {34, 30}, {33, 27}, {30, 28}
    };

    private static final int[][] rSwitches = {
        {2, 42}, {5, 39}, {8, 36},
        {42, 11}, {39, 14}, {36, 17},
        {11, 20}, {14, 23}, {17, 26},
        {20, 2}, {23, 5}, {26, 8},
        {45, 47}, {46, 50}, {47, 53}, {50, 52},
        {53, 51}, {52, 48}, {51, 45}, {48, 46}
    };

    public void displayCubeTerminal() {
        StringBuilder displayString = new StringBuilder();

        for (int i = 53; i >=0 ; i-=9) {
            for (int j = 1; j < 10; j++) {
                displayString.append(cube[i - 9 + j]);
                if (j % 3 != 0) displayString.append('|');
                else displayString.append('\n');
                if (j == 9 && i > 9) {
                    displayString.append('\n');
                }              
            }
        }
        System.out.print(displayString);
    }

    public void randomizeCube(int turns) {
        for (int i = 0; i < turns; i++) {
            int turn = ThreadLocalRandom.current().nextInt(1, 13);

            switch (turn) {
                case 1 -> u();
                case 2 -> U();
                case 3 -> d();
                case 4 -> D();
                case 5 -> f();
                case 6 -> F();
                case 7 -> b();
                case 8 -> B();
                case 9 -> l();
                case 10 -> L();
                case 11 -> r();
                case 12 -> R();
                default -> throw new AssertionError();
            }
        }
    }

    private void genericClockwiseTurn(int[][] switches) {
        char[] oldCube = cube.clone();
    
        for (int[] i : switches) {
            cube[i[1]] = oldCube[i[0]];            
        }
    }

    private void genericCounterclockwiseTurn(int[][] switches) {
        char[] oldCube = cube.clone();
    
        for (int[] i : switches) {
            cube[i[0]] = oldCube[i[1]];            
        }
    }

    public void u() {
        genericClockwiseTurn(uSwitches);
    }

    public void U() {
        genericCounterclockwiseTurn(uSwitches);
    }

    public void d() {
        genericClockwiseTurn(dSwitches);
    }

    public void D() {
        genericCounterclockwiseTurn(dSwitches);
    }

    public void f() {
        genericClockwiseTurn(fSwitches);
    }

    public void F() {
        genericCounterclockwiseTurn(fSwitches);
    }

    public void b() {
        genericClockwiseTurn(bSwitches);
    }

    public void B() {
        genericCounterclockwiseTurn(bSwitches);
    }

    public void l() {
        genericClockwiseTurn(lSwitches);
    }

    public void L() {
        genericCounterclockwiseTurn(lSwitches);
    }

    public void r() {
        genericClockwiseTurn(rSwitches);
    }

    public void R() {
        genericCounterclockwiseTurn(rSwitches);
    }
}