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
                    default -> throw new AssertionError();
                }
            }
        }
    }
}
 
