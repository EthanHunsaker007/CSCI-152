import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    public static CubieCube copyCube(CubieCube cube) {
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

    public int getCornerPermCoord() {
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

    public int getP2EdgePermCoord() {
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

    public int getP2UDPermCoord() {
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

    public static CubieCube fromCornerOriCoord(int coord) {
        CubieCube cube = new CubieCube();
        int sum = 0;

        for (int i = 6; i >= 0; i--) {
            cube.cornerOri[i] = (byte)(coord % 3);
            sum += cube.cornerOri[i];
            coord /= 3;
        }

        cube.cornerOri[7] = (byte)((3 - sum % 3) % 3);
        return cube;
    }

    public static CubieCube fromEdgeOriCoord(int coord) {
        CubieCube cube = new CubieCube();
        int sum = 0;

        for (int i = 10; i >= 0; i--) {
            cube.edgeOri[i] = (byte)(coord % 2);
            sum += cube.edgeOri[i];
            coord /= 2;
        }

        cube.edgeOri[11] = (byte)((2 - sum % 2) % 2);
        return cube;
    }

    public static CubieCube fromUDSliceCoord(int coord) {
        CubieCube cube = new CubieCube();
        int k = 3;
        int nextEdge = 7;

        for (int i = 11; i >= 0; i--){
            int binomial = binomial(i, k);
            if (coord >= binomial) {
                cube.edgePos[i] = (byte)(nextEdge);
                nextEdge--;
                coord -= binomial;
            } else {
                cube.edgePos[i] = (byte)(8 + k);
                k--;
            }
        }

        return cube;
    }

    public static CubieCube fromCornerPermCoord(int coord) {
        CubieCube cube = new CubieCube();
        int[] perms = new int[8];
    
        for (int i = 1; i <= 7; i++) {
            perms[i] = coord % (i + 1);
            coord /= (i + 1);
        }

        List<Integer> corners = new ArrayList<>(Arrays.asList(0,1,2,3,4,5,6,7));

        for (int i = 7; i >= 0; i--) {
            int rank = corners.size() - 1 - perms[i];
            cube.cornerPos[i] = corners.get(rank).byteValue();
            corners.remove(rank);
        }

        return cube;
    }

    public static CubieCube fromP2EdgePermCoord(int coord) {
        CubieCube cube = new CubieCube();
        int[] perms = new int[8];
    
        for (int i = 1; i <= 7; i++) {
            perms[i] = coord % (i + 1);
            coord /= (i + 1);
        }

        List<Integer> edges = new ArrayList<>(Arrays.asList(0,1,2,3,4,5,6,7));

        for (int i = 7; i >= 0; i--) {
            int rank = edges.size() - 1 - perms[i];
            cube.edgePos[i] = edges.get(rank).byteValue();
            edges.remove(rank);
        }
        
        return cube;
    }

    public static CubieCube fromP2UDPermCoord(int coord) {
        CubieCube cube = new CubieCube();
        int[] perms = new int[4];

        for (int i = 1; i <= 3; i++) {
            perms[i] = coord % (i + 1);
            coord /= (i + 1);
        }

        List<Integer> edges = new ArrayList<>(Arrays.asList(8, 9, 10, 11));

        for (int i = 11; i >= 8; i--) {
            int rank = edges.size() - 1 - perms[i - 8];
            cube.edgePos[i] = edges.get(rank).byteValue();
            edges.remove(rank);
        }

        return cube;
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
    
    public void displayCubeTerminal() {
        StringBuilder displayString = new StringBuilder();
        char[] cube = as1DFaceletArray();

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
    
    private static int binomial(final int N, final int K) {
        int returnInt = 1;
        for (int k = 0; k < K; k++) {
            returnInt = returnInt * (N-k) / (k+1);
        }
    return returnInt;
    }
}
