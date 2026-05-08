public class App {
    public static void main(String[] args) throws Exception {
        World world = new World( 50, 30);

        for (int i = 0; i < 10; i++) {
            world.spawnRandomPlant("Grass");
            world.spawnCreature();
        }

        while (true) { 
            world.tick();
            stepsPerSecond(10);
        }
    }

    private static void stepsPerSecond(int steps) throws InterruptedException {
        Thread.sleep(1000 / steps);
    }
}
