import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

public class KociembaSolver {
    // Move tables for all 6 coordinates
    private final static int[] cornerOriMoveTable = new int[2187 * 18];
    private final static int[] edgeOriMoveTable = new int[2048 * 18];
    private final static int[] UDSliceMoveTable = new int[495 * 18];
    private final static int[] cornerPermMoveTable = new int[40320 * 18];
    private final static int[] P2EdgePermMoveTable = new int[40320 * 18];
    private final static int[] P2UDPermMoveTable = new int[24 * 18];

    // Prune tables for coordinate combinations
    private final static byte[] cornerOriUDSlicePruneTable = new byte[2187 * 495];
    private final static byte[] edgeOriUDSlicePruneTable = new byte[2048 * 495];
    private final static byte[] cornerPermP2UDPermPruneTable = new byte[40320 * 24];
    private final static byte[] P2EdgePermP2UDPermPruneTable = new byte[40320 * 24];
    static {
        Arrays.fill(cornerOriUDSlicePruneTable, (byte)-1);
        Arrays.fill(edgeOriUDSlicePruneTable, (byte)-1);
        Arrays.fill(cornerPermP2UDPermPruneTable, (byte)-1);
        Arrays.fill(P2EdgePermP2UDPermPruneTable, (byte)-1);
    }
    
    private static final int[] phase2Moves = new int[]{0, 1, 6, 7, 8, 9, 10, 11, 12, 13};
    private static final Queue<int[]> p1Solves = new ArrayDeque<>();

    private static boolean tablesGenerated = false;

    public static void buildTables() {
        if (!tablesGenerated) {
            generateMoveTables();
            generatePruneTables();

            tablesGenerated = true;
        }
    }

    private static void populateMoveTable(int[] table, int size, IntFunction<CubieCube> fromCoord, ToIntFunction<CubieCube> getCoord) {
        for (int coord = 0; coord < size; coord++) {
            CubieCube testCube = fromCoord.apply(coord);

            for (int m = 0; m < 6; m++) {
                for (int p = 0; p < 4; p++) {
                    testCube.move(m);
                    if (p != 3) {
                        table[coord * 18 + (p * 6 + m)] = getCoord.applyAsInt(testCube);
                    }    
                }
            }
        }        
    }

    private static void generateMoveTables() {
        populateMoveTable(cornerOriMoveTable, 2187, CubieCube::fromCornerOriCoord, CubieCube::getCornerOriCoord);
        populateMoveTable(edgeOriMoveTable, 2048, CubieCube::fromEdgeOriCoord, CubieCube::getEdgeOriCoord);
        populateMoveTable(UDSliceMoveTable, 495, CubieCube::fromUDSliceCoord, CubieCube::getUDSliceCoord);
        populateMoveTable(cornerPermMoveTable, 40320, CubieCube::fromCornerPermCoord, CubieCube::getCornerPermCoord);
        populateMoveTable(P2EdgePermMoveTable, 40320, CubieCube::fromP2EdgePermCoord, CubieCube::getP2EdgePermCoord);
        populateMoveTable(P2UDPermMoveTable, 24, CubieCube::fromP2UDPermCoord, CubieCube::getP2UDPermCoord);
    }

    private static void populatePruneTable(byte[] pruneTable, int[] moveTableOne, int[] moveTableTwo, int width, boolean isPhaseTwo) {
        Queue<int[]> coordQueue = new ArrayDeque<>();

        coordQueue.add(new int[]{0, 0});
        pruneTable[0] = 0;

        while (!coordQueue.isEmpty()) {
            int[] temp = coordQueue.poll();
            int coordOne = temp[0] / width;
            int coordTwo = temp[0] % width;

            int[] moves = isPhaseTwo ? phase2Moves : 
            new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17};

            for (int i : moves) {
                int nextCoordOne = moveTableOne[coordOne * 18 + i]; 
                int nextCoordTwo = moveTableTwo[coordTwo * 18 + i];
                
                if (pruneTable[nextCoordOne * width + nextCoordTwo] == -1) {
                    pruneTable[nextCoordOne * width + nextCoordTwo] = (byte)(temp[1] + 1);
                    coordQueue.add(new int[]{nextCoordOne * width + nextCoordTwo, temp[1] + 1});
                }
            }
        }        
    }

    public static void generatePruneTables() {
        populatePruneTable(cornerOriUDSlicePruneTable, cornerOriMoveTable, UDSliceMoveTable, 495, false);
        populatePruneTable(edgeOriUDSlicePruneTable, edgeOriMoveTable, UDSliceMoveTable, 495, false);
        populatePruneTable(P2EdgePermP2UDPermPruneTable, P2EdgePermMoveTable, P2UDPermMoveTable, 24, true);
        populatePruneTable(cornerPermP2UDPermPruneTable, cornerPermMoveTable, P2UDPermMoveTable, 24, true);
    }

    public static int[] solveCube(CubieCube cube) {
        p1Solves.clear();
        
        int[] state = new int[]{cube.getCornerOriCoord(), cube.getEdgeOriCoord(), cube.getUDSliceCoord()};
        byte bound = (byte)Math.max(cornerOriUDSlicePruneTable[state[0] * 495 + state[2]], edgeOriUDSlicePruneTable[state[1] * 495 + state[2]]);       

        int recursionLimit = 12;

        int shortestPath = Integer.MAX_VALUE;
        int[] outMoves = null;

        while (true) {
            int[] moves = new int[20];
            int result = P1ida(state, 0, bound, -1, moves);

            while (!p1Solves.isEmpty()) {
                int[] p1Solve = p1Solves.poll();
                CubieCube p2Cube = CubieCube.copyCube(cube);
                for (int m : p1Solve) {
                    p2Cube.move(m);
                }

                int[] p2State = new int[]{p2Cube.getCornerPermCoord(), p2Cube.getP2EdgePermCoord(), p2Cube.getP2UDPermCoord()};
                byte p2Bound = (byte)Math.max(cornerPermP2UDPermPruneTable[p2State[0] * 24 + p2State[2]], P2EdgePermP2UDPermPruneTable[p2State[1] * 24 + p2State[2]]);  
                int lastP1Move = p1Solve.length > 0 ? p1Solve[p1Solve.length - 1] : -1;
                int[] p2Solve = new int[20];
                while (true) {
                    int result2 = P2ida(p2State, 0, p2Bound, lastP1Move, p2Solve);
                    if (result2 == 0) break;
                    p2Bound = (byte)result2;
                }
                if (p1Solve.length + p2Solve[0] < shortestPath) {
                    shortestPath = p1Solve.length + p2Solve[0];
                    outMoves = new int[p1Solve.length + p2Solve[0]];

                    System.arraycopy(p1Solve, 0, outMoves, 0, p1Solve.length);
                    System.arraycopy(p2Solve, 1, outMoves, p1Solve.length, p2Solve[0]);
                }
            }
            if (result == recursionLimit) break;
            bound = (byte)result;
        }
        System.out.println("Solve length: " + shortestPath + " moves");
        return outMoves;
    }

    private static int P1ida(int[] state, int depth, int bound, int lastMove, int[] moves) {
        byte heuristic = (byte)Math.max(cornerOriUDSlicePruneTable[state[0] * 495 + state[2]], edgeOriUDSlicePruneTable[state[1] * 495 + state[2]]);

        if (state[0] == 0 && state[1] == 0 && state[2] == 0) {
            p1Solves.add(Arrays.copyOf(moves, depth));
            return Integer.MAX_VALUE;
        }
 
        if (depth == bound) return heuristic;

        int minimum = Integer.MAX_VALUE;

        for (int m = 0; m < 18; m++) {
            int currentFace = m % 6;
            int lastFace = lastMove % 6;
            if (lastMove != -1 && currentFace == lastFace) continue;
            if (lastMove != -1 && currentFace / 2 == lastFace / 2 && currentFace < lastFace) continue;

            int[] nextState = new int[]{cornerOriMoveTable[state[0] * 18 + m], edgeOriMoveTable[state[1] * 18 + m], UDSliceMoveTable[state[2] * 18 + m]};

            byte nextHeuristic = (byte)Math.max(cornerOriUDSlicePruneTable[nextState[0] * 495 + nextState[2]], edgeOriUDSlicePruneTable[nextState[1] * 495 + nextState[2]]);

            if (nextHeuristic + depth + 1 > bound) {
                minimum = Math.min(minimum, nextHeuristic + depth + 1);
                continue;
            }

            moves[depth] = m;
            int result = P1ida(nextState, depth + 1, bound, m, moves);
            if (result == 0) return 0;
            minimum = Math.min(minimum, result);
        }
        return minimum;
    }

    private static int P2ida(int[] state, int depth, int bound, int lastMove, int[] moves) {
        byte heuristic = (byte)Math.max(cornerPermP2UDPermPruneTable[state[0] * 24 + state[2]], P2EdgePermP2UDPermPruneTable[state[1] * 24 + state[2]]);

        if (state[0] == 0 && state[1] == 0 && state[2] == 0) {
            moves[0] = depth;
            return 0;
        }
        if (depth == bound) return heuristic;

        int minimum = Integer.MAX_VALUE;

        for (int m : phase2Moves) {
            int currentFace = m % 6;
            int lastFace = lastMove % 6;
            if (lastMove != -1 && currentFace == lastFace) continue;
            if (lastMove != -1 && currentFace / 2 == lastFace / 2 && currentFace < lastFace) continue;

            int[] nextState = new int[]{cornerPermMoveTable[state[0] * 18 + m], P2EdgePermMoveTable[state[1] * 18 + m], P2UDPermMoveTable[state[2] * 18 + m]};

            byte nextHeuristic = (byte)Math.max(cornerPermP2UDPermPruneTable[nextState[0] * 24 + nextState[2]], P2EdgePermP2UDPermPruneTable[nextState[1] * 24 + nextState[2]]);

            if (nextHeuristic + depth + 1 > bound) {
                minimum = Math.min(minimum, nextHeuristic + depth + 1);
                continue;
            }

            moves[depth + 1] = m;
            int result = P2ida(nextState, depth + 1, bound, m, moves);
            if (result == 0) return 0;
            minimum = Math.min(minimum, result);
        }
        return minimum;
    }
}