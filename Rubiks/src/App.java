// Ethan Hunsaker
// 2/24/2026
// Rubiks Cube Solver using Herbert Kociemba's Two Phase Algorithm
// Average moves per solve: 23.2

public class App {
    public static void main(String[] args) throws Exception {
        CubieCube cube = new CubieCube();
        KociembaSolver.buildTables();
        RubiksCube cubeDisplay = new RubiksCube();
        cubeDisplay.showCube(cube);

        if (args.length > 0) {
            for (String m : args) {
                switch (m) {
                    case "u" -> cube.move(0);
                    case "d" -> cube.move(1);
                    case "l" -> cube.move(2);
                    case "r" -> cube.move(3);
                    case "f" -> cube.move(4);
                    case "b" -> cube.move(5);
                    case "u'" -> cube.move(12);
                    case "d'" -> cube.move(13);
                    case "l'" -> cube.move(14);
                    case "r'" -> cube.move(15);
                    case "f'" -> cube.move(16);
                    case "b'" -> cube.move(17);
                    default -> throw new AssertionError();
                }
            }
            cubeDisplay.setCubeColors(cube.as1DFaceletArray());
            cube.displayCubeTerminal();
            int[] solveMoves = KociembaSolver.solveCube(cube);
            for (int m : solveMoves) {
                System.out.println(m + " ");
                cube.move(m);
            }
            cube.displayCubeTerminal();
        }
    }
}
 
