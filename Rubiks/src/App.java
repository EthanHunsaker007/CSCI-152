public class App {
    public static void main(String[] args) throws Exception {
        VectorCube cube = new VectorCube();
        CubieCube cubie = new CubieCube();

        // RubiksCube displayCube = new RubiksCube();
        RubiksCube cubieDisplay = new RubiksCube();

        // displayCube.showVectorCube(cube);
        cubieDisplay.showCubieCube(cubie);

        if (args.length != 0) {
            cube.moveSequence(args);
        } else {
            // cube.randomizeCube(50);
        }
    }
}
 
