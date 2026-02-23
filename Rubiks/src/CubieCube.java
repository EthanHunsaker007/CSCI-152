import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;

public class CubieCube {
    // 0=URF, 1=UFL, 2=ULB, 3=UBR, 4=DFR, 5=DLF, 6=DBL, 7=DRB
    public final byte[] cornerPos = {0, 1, 2, 3, 4, 5, 6, 7};
    public final byte[] cornerOri = {0, 0, 0, 0, 0, 0, 0, 0};

    // 0=UR, 1=UF, 2=UL, 3=UB, 4=DR, 5=DF, 6=DL, 7=DB, 8=FR, 9=FL, 10=BL, 11=BR
    public final byte[] edgePos = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
    public final byte[] edgeOri = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    // Temporary Move Buffers
    private final byte[] tempCornerPos = new byte[8];
    private final byte[] tempCornerOri = new byte[8];
    private final byte[] tempEdgePos = new byte[12];
    private final byte[] tempEdgeOri = new byte[12];
    
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

    // Move order = U,  D,  L,  R,  F,  B
    private static final byte[][] cornerPosMoves = {
        {3, 0, 1, 2, 4, 5, 6, 7},
        {0, 1, 2, 3, 5, 6, 7, 4},
        {0, 2, 6, 3, 4, 1, 5, 7},
        {4, 1, 2, 0, 7, 5, 6, 3},
        {1, 5, 2, 3, 0, 4, 6, 7},
        {0, 1, 3, 7, 4, 5, 2, 6},
    };

    private static final byte[][] edgePosMoves = {
        {3, 0, 1, 2, 4, 5, 6, 7, 8, 9, 10, 11},
        {0, 1, 2, 3, 5, 6, 7, 4, 8, 9, 10, 11},
        {0, 1, 10, 3, 4, 5, 9, 7, 8, 2, 6, 11},
        {8, 1, 2, 3, 11, 5, 6, 7, 4, 9, 10, 0},
        {0, 9, 2, 3, 4, 8, 6, 7, 1, 5, 10, 11},
        {0, 1, 2, 11, 4, 5, 6, 10, 8, 9, 3, 7},
    };

    private static final byte[][] cornerOriMoves = {
        {0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0},
        {0, 1, 2, 0, 0, 2, 1, 0},
        {2, 0, 0, 1, 1, 0, 0, 2},
        {1, 2, 0, 0, 2, 1, 0, 0},
        {0, 0, 1, 2, 0, 0, 2, 1},
    };
    
    private static final byte[][] edgeOriMoves = {
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {0, 1, 0, 0, 0, 1, 0, 0, 1, 1, 0, 0},
        {0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 1},
    };

    private static final byte[][] cornerFaceletMap = {
        {8, 45, 20},
        {6, 18, 29},
        {0, 27, 38},
        {2, 36, 47},
        {11, 26, 51},
        {9, 35, 24},
        {15, 44, 33},
        {17, 53, 42},
    };

    private static final byte[][] edgeFaceletMap = {
        {5, 46},
        {7, 19},
        {3, 28},
        {1, 37},
        {14, 52},
        {10, 25},
        {12, 34},
        {16, 43},
        {23, 48},
        {21, 32},
        {41, 30},
        {39, 50}
    };

    private static final char[][] cornerColorMap = {
        {'w', 'r', 'g'},
        {'w', 'g', 'o'},
        {'w', 'o', 'b'},
        {'w', 'b', 'r'},
        {'y', 'g', 'r'},
        {'y', 'o', 'g'},
        {'y', 'b', 'o'},
        {'y', 'r', 'b'},
    };

    private static final char[][] edgeColorMap = {
        {'w', 'r'},
        {'w', 'g'},
        {'w', 'o'},
        {'w', 'b'},
        {'y', 'r'},
        {'y', 'g'},
        {'y', 'o'},
        {'y', 'b'},
        {'g', 'r'},
        {'g', 'o'},
        {'b', 'o'},
        {'b', 'r'},
    };

    private static final int[] phase2Moves = new int[]{0, 1, 6, 7, 8, 9, 10, 11, 12, 13};
    private static final int[] solveMoves = new int[30];
    private static int solveLength = 0;

    private static boolean tablesGenerated = false;

    public void move(int m) {
        int move = m % 6;
        int turns = m / 6 + 1;

        for (int t = 0; t < turns; t++) {
            for (int i = 0; i < 8; i++) {
                byte p = cornerPosMoves[move][i];
                tempCornerPos[i] = cornerPos[p];
                tempCornerOri[i] = (byte)((cornerOri[p] + cornerOriMoves[move][i]) % 3);
            }

            for (int i = 0; i < 12; i++) {
                byte p = edgePosMoves[move][i];
                tempEdgePos[i] = edgePos[p];
                tempEdgeOri[i] = (byte)((edgeOri[p] + edgeOriMoves[move][i]) % 2);
            }

            System.arraycopy(tempCornerPos, 0, cornerPos, 0, 8);
            System.arraycopy(tempCornerOri, 0, cornerOri, 0, 8);
            System.arraycopy(tempEdgePos, 0, edgePos, 0, 12);
            System.arraycopy(tempEdgeOri, 0, edgeOri, 0, 12);            
        }
    }

    public void randomizeCube(int turns) {
        for (int i = 0; i < turns; i++) {
            int turn = ThreadLocalRandom.current().nextInt(0, 18);
            move(turn);
        }
    }

    private static CubieCube copyCube(CubieCube cube) {
        CubieCube copyCube = new CubieCube();
        System.arraycopy(cube.cornerPos, 0, copyCube.cornerPos, 0, 8);
        System.arraycopy(cube.cornerOri, 0, copyCube.cornerOri, 0, 8);
        System.arraycopy(cube.edgePos, 0, copyCube.edgePos, 0, 12);
        System.arraycopy(cube.edgeOri, 0, copyCube.edgeOri, 0, 12);

        return copyCube;
    }

    public int getCornerOriCoord() {
        int coord = 0;
        for (int i = 0; i < 7; i++) {
            coord = (int)(coord * 3 + cornerOri[i]);
        }
        return coord;
    }

    public int getEdgeOriCoord() {
        int coord = 0;
        for (int i = 0; i < 11; i++) {
            coord = (int)(coord * 2 + edgeOri[i]);
        }
        return coord;
    }

    public int getUDSliceCoord() {
        int coord = 0;
        byte UDSlices = 3;
        for (int i = 11; i >= 0; i--) {
            if (UDSlices < 0) break;
            if (edgePos[i] == 11 || edgePos[i] == 10 || edgePos[i] == 9 || edgePos[i] == 8) UDSlices--;
            else coord += binomial(i, UDSlices);
        }
        return coord;
    }

    private int getCornerPermCoord() {
        int coord = 0;
        for (int i = 7; i > 0; i--) {
            int higherOrder = 0;
            for (int j = 0; j < i; j++) {
                if (cornerPos[j] > cornerPos[i]) higherOrder++;
            }
            coord = (coord + higherOrder) * i;
        }
        return coord;
    }

    private int getP2EdgePermCoord() {
        int coord = 0;
        for (int i = 7; i > 0; i--) {
            int higherOrder = 0;
            for (int j = 0; j < i; j++) {
                if (edgePos[j] > edgePos[i]) higherOrder++;
            }
            coord = (coord + higherOrder) * i;
        }
        return coord;
    }

    private int getP2UDPermCoord() {
        int coord = 0;
        for (int i = 11; i > 8; i--) {
            int higherOrder = 0;
            for (int j = 7; j < i; j++) {
                if (edgePos[j] > edgePos[i]) higherOrder++;
            }
            coord = (coord + higherOrder) * (i - 8);
        }
        return coord;        
    }

    private void fromCornerOriCoord(int coord) {
        int sum = 0;
        for (int i = 6; i >= 0; i--) {
            cornerOri[i] = (byte)(coord % 3);
            sum += cornerOri[i];
            coord /= 3;
        }
        cornerOri[7] = (byte)((3 - sum % 3) % 3);
    }

    private void fromEdgeOriCoord(int coord) {
        int sum = 0;
        for (int i = 10; i >= 0; i--) {
            edgeOri[i] = (byte)(coord % 2);
            sum += edgeOri[i];
            coord /= 2;
        }
        edgeOri[11] = (byte)((2 - sum % 2) % 2);
    }

    private void fromUDSliceCoord(int coord) {
        int k = 3;
        int nextEdge = 7;

        for (int i = 11; i >= 0; i--){
            int binomial = binomial(i, k);
            if (coord >= binomial) {
                edgePos[i] = (byte)(nextEdge);
                nextEdge--;
                coord -= binomial;
            } else {
                edgePos[i] = (byte)(8 + k);
                k--;
            }
        }
    }

    private void fromCornerPermCoord(int coord) {
        int[] perms = new int[8];
    
        for (int i = 1; i <= 7; i++) {
            perms[i] = coord % (i + 1);
            coord /= (i + 1);
        }

        List<Integer> corners = new ArrayList<>(Arrays.asList(0,1,2,3,4,5,6,7));

        for (int i = 7; i >= 0; i--) {
            int rank = corners.size() - 1 - perms[i];
            cornerPos[i] = corners.get(rank).byteValue();
            corners.remove(rank);
        }
    }

    private void fromP2EdgePermCoord(int coord) {
        int[] perms = new int[8];
    
        for (int i = 1; i <= 7; i++) {
            perms[i] = coord % (i + 1);
            coord /= (i + 1);
        }

        List<Integer> edges = new ArrayList<>(Arrays.asList(0,1,2,3,4,5,6,7));

        for (int i = 7; i >= 0; i--) {
            int rank = edges.size() - 1 - perms[i];
            edgePos[i] = edges.get(rank).byteValue();
            edges.remove(rank);
        }
    }

    private void fromP2UDPermCoord(int coord) {
        int[] perms = new int[4];

        for (int i = 1; i <= 3; i++) {
            perms[i] = coord % (i + 1);
            coord /= (i + 1);
        }

        List<Integer> edges = new ArrayList<>(Arrays.asList(8, 9, 10, 11));

        for (int i = 11; i >= 8; i--) {
            int rank = edges.size() - 1 - perms[i - 8];
            edgePos[i] = edges.get(rank).byteValue();
            edges.remove(rank);
        }
    }

    public static void buildTables() {
        if (!tablesGenerated) {
            generateMoveTables();
            generatePruneTables();
            tablesGenerated = true;
        }
    }

    private static void generateMoveTables() {
        for (int coord = 0; coord < 2187; coord++) {
            CubieCube testCube = new CubieCube();
            testCube.fromCornerOriCoord(coord);

            for (int m = 0; m < 6; m++) {
                for (int p = 0; p < 4; p++) {
                    testCube.move(m);
                    if (p != 3) {
                        cornerOriMoveTable[coord * 18 + (p * 6 + m)] = testCube.getCornerOriCoord();
                    }    
                }
            }
        }

        for (int coord = 0; coord < 2048; coord++) {
            CubieCube testCube = new CubieCube();
            testCube.fromEdgeOriCoord(coord);

            for (int m = 0; m < 6; m++) {
                for (int p = 0; p < 4; p++) {
                    testCube.move(m);
                    if (p != 3) {
                        edgeOriMoveTable[coord * 18 + (p * 6 + m)] = testCube.getEdgeOriCoord();
                    }    
                }
            }
        }

        for (int coord = 0; coord < 495; coord++) {
            CubieCube testCube = new CubieCube();
            testCube.fromUDSliceCoord(coord);

            for (int m = 0; m < 6; m++) {
                for (int p = 0; p < 4; p++) {
                    testCube.move(m);
                    if (p != 3) {
                        UDSliceMoveTable[coord * 18 + (p * 6 + m)] = testCube.getUDSliceCoord();
                    }    
                }
            }
        }

        for (int coord = 0; coord < 40320; coord++) {
            CubieCube testCube = new CubieCube();
            testCube.fromCornerPermCoord(coord);

            for (int m = 0; m < 6; m++) {
                for (int p = 0; p < 4; p++) {
                    testCube.move(m);
                    if (p != 3) {
                        cornerPermMoveTable[coord * 18 + (p * 6 + m)] = testCube.getCornerPermCoord();
                    }    
                }
            }
        }

        for (int coord = 0; coord < 40320; coord++) {
            CubieCube testCube = new CubieCube();
            testCube.fromP2EdgePermCoord(coord);

            for (int m = 0; m < 6; m++) {
                for (int p = 0; p < 4; p++) {
                    testCube.move(m);
                    if (p != 3) {
                        P2EdgePermMoveTable[coord * 18 + (p * 6 + m)] = testCube.getP2EdgePermCoord();
                    }    
                }
            }
        }

        for (int coord = 0; coord < 24; coord++) {
            CubieCube testCube = new CubieCube();
            testCube.fromP2UDPermCoord(coord);

            for (int m = 0; m < 6; m++) {
                for (int p = 0; p < 4; p++) {
                    testCube.move(m);
                    if (p != 3) {
                        P2UDPermMoveTable[coord * 18 + (p * 6 + m)] = testCube.getP2UDPermCoord();
                    }    
                }
            }
        }
    }

    public static void generatePruneTables() {
        generateMoveTables();
        Queue<int[]> coordQueue = new ArrayDeque<>();

        coordQueue.add(new int[]{0, 0});
        cornerOriUDSlicePruneTable[0] = 0;

        while (!coordQueue.isEmpty()) {
            int[] temp = coordQueue.poll();
            int cornerOriCoord = temp[0] / 495;
            int UDSliceCoord = temp[0] % 495;

            for (int i = 0; i < 18; i++) {
                int nextCornerOri = cornerOriMoveTable[cornerOriCoord * 18 + i]; 
                int nextUDSlice = UDSliceMoveTable[UDSliceCoord * 18 + i];
                
                if (cornerOriUDSlicePruneTable[nextCornerOri * 495 + nextUDSlice] == -1) {
                    cornerOriUDSlicePruneTable[nextCornerOri * 495 + nextUDSlice] = (byte)(temp[1] + 1);
                    coordQueue.add(new int[]{nextCornerOri * 495 + nextUDSlice, temp[1] + 1});
                }
            }
        }

        coordQueue.add(new int[]{0, 0});
        edgeOriUDSlicePruneTable[0] = 0;

        while (!coordQueue.isEmpty()) {
            int[] temp = coordQueue.poll();
            int edgeOriCoord = temp[0] / 495;
            int UDSliceCoord = temp[0] % 495;

            for (int i = 0; i < 18; i++) {
                int nextEdgeOri = edgeOriMoveTable[edgeOriCoord * 18 + i]; 
                int nextUDSlice = UDSliceMoveTable[UDSliceCoord * 18 + i];
                
                if (edgeOriUDSlicePruneTable[nextEdgeOri * 495 + nextUDSlice] == -1) {
                    edgeOriUDSlicePruneTable[nextEdgeOri * 495 + nextUDSlice] = (byte)(temp[1] + 1);
                    coordQueue.add(new int[]{nextEdgeOri * 495 + nextUDSlice, temp[1] + 1});
                }
            }
        }

        coordQueue.add(new int[]{0, 0});
        P2EdgePermP2UDPermPruneTable[0] = 0;

        while (!coordQueue.isEmpty()) {
            int[] temp = coordQueue.poll();
            int P2EdgePermCoord = temp[0] / 24;
            int P2UDPermCoord = temp[0] % 24;

            for (int i : phase2Moves) {
                int nextP2EdgePerm = P2EdgePermMoveTable[P2EdgePermCoord * 18 + i]; 
                int nextP2UDPerm = P2UDPermMoveTable[P2UDPermCoord * 18 + i];
                
                if (P2EdgePermP2UDPermPruneTable[nextP2EdgePerm * 24 + nextP2UDPerm] == -1) {
                    P2EdgePermP2UDPermPruneTable[nextP2EdgePerm * 24 + nextP2UDPerm] = (byte)(temp[1] + 1);
                    coordQueue.add(new int[]{nextP2EdgePerm * 24 + nextP2UDPerm, temp[1] + 1});
                }
            }
        }

        coordQueue.add(new int[]{0, 0});
        cornerPermP2UDPermPruneTable[0] = 0;

        while (!coordQueue.isEmpty()) {
            int[] temp = coordQueue.poll();
            int cornerPermCoord = temp[0] / 24;
            int P2UDPermCoord = temp[0] % 24;

            for (int i : phase2Moves) {
                int nextCornerPerm = cornerPermMoveTable[cornerPermCoord * 18 + i]; 
                int nextP2UDPerm = P2UDPermMoveTable[P2UDPermCoord * 18 + i];
                
                if (cornerPermP2UDPermPruneTable[nextCornerPerm * 24 + nextP2UDPerm] == -1) {
                    cornerPermP2UDPermPruneTable[nextCornerPerm * 24 + nextP2UDPerm] = (byte)(temp[1] + 1);
                    coordQueue.add(new int[]{nextCornerPerm * 24 + nextP2UDPerm, temp[1] + 1});
                }
            }
        }
    }

    public static void solveCube(CubieCube cube) {
        int[] state = new int[]{cube.getCornerOriCoord(), cube.getEdgeOriCoord(), cube.getUDSliceCoord()};
        byte bound = (byte)Math.max(cornerOriUDSlicePruneTable[state[0] * 495 + state[2]], edgeOriUDSlicePruneTable[state[1] * 495 + state[2]]);       
        solveLength = 0;
        Arrays.fill(solveMoves, 0);

        while (true) { 
            int result = P1ida(state, 0, bound, -1);
            if (result == 0) break;
            bound = (byte)result;
        }

        CubieCube p2Cube = CubieCube.copyCube(cube);

        for (int i = 0; i < solveLength; i++) {
            p2Cube.move(solveMoves[i]);
        } 

        int[] p2State = new int[]{p2Cube.getCornerPermCoord(), p2Cube.getP2EdgePermCoord(), p2Cube.getP2UDPermCoord()};
        byte p2Bound = (byte)Math.max(cornerPermP2UDPermPruneTable[p2State[0] * 24 + p2State[2]], P2EdgePermP2UDPermPruneTable[p2State[1] * 24 + p2State[2]]);  
        int lastP1Move = solveLength > 0 ? solveMoves[solveLength - 1] : -1;

        while (true) {
            int result = P2ida(p2State, 0, p2Bound, lastP1Move);
            if (result == 0) break;
            p2Bound = (byte)result;
        }

        for (int i = 0; i < solveLength; i++) {
            cube.move(solveMoves[i]);
        } 
        System.out.println(solveLength);
    }

    private static int P1ida(int[] state, int depth, int bound, int lastMove) {
        byte heuristic = (byte)Math.max(cornerOriUDSlicePruneTable[state[0] * 495 + state[2]], edgeOriUDSlicePruneTable[state[1] * 495 + state[2]]);

        if (state[0] == 0 && state[1] == 0 && state[2] == 0) {
            solveLength += depth;
            return 0;
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

            solveMoves[depth] = m;
            int result = P1ida(nextState, depth + 1, bound, m);
            if (result == 0) return 0;
            minimum = Math.min(minimum, result);
        }
        return minimum;
    }

    private static int P2ida(int[] state, int depth, int bound, int lastMove) {
        byte heuristic = (byte)Math.max(cornerPermP2UDPermPruneTable[state[0] * 24 + state[2]], P2EdgePermP2UDPermPruneTable[state[1] * 24 + state[2]]);

        if (heuristic == 0) {
            solveLength += depth;
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

            solveMoves[depth + solveLength] = m;
            int result = P2ida(nextState, depth + 1, bound, m);
            if (result == 0) return 0;
            minimum = Math.min(minimum, result);
        }
        return minimum;
    }

    public char[] as1DFaceletArray() {
        char[] returnFacelets = new char[54];

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 3; j++) {
                returnFacelets[cornerFaceletMap[i][j]] = cornerColorMap[cornerPos[i]][(j + 2*cornerOri[i]) % 3];
            }
        }

        for (int i = 0; i < 12; i++) {
            for (int j = 0; j < 2; j++) {
                returnFacelets[edgeFaceletMap[i][j]] = edgeColorMap[edgePos[i]][(j + edgeOri[i]) % 2];
            }
        }

        returnFacelets[4] = 'w';
        returnFacelets[13] = 'y';
        returnFacelets[22] = 'g';
        returnFacelets[31] = 'o';
        returnFacelets[40] = 'b';
        returnFacelets[49] = 'r';

        return returnFacelets;
    }

    private static int binomial(final int N, final int K) {
        int returnInt = 1;
        for (int k = 0; k < K; k++) {
            returnInt = returnInt * (N-k) / (k+1);
        }
    return returnInt;
    }
}
