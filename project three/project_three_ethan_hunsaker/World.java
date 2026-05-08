import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;

public class World {
    private final Tile[][] map;
    private final NameGenerator nameGenerator;
    private final ArrayList<Creature> creatures;
    private final ArrayList<Plant> plants;
    private final SimulationDisplay display;

    public World(int mapWidth, int mapHeight) throws IOException {
        this.map = generateMap(mapWidth, mapHeight);
        this.nameGenerator = new NameGenerator();
        this.creatures = new ArrayList<>();
        this.plants = new ArrayList<>();
        this.display = new SimulationDisplay(getDisplayColors());
    }

    private Color[][] getDisplayColors() {
        Color[][] displayColors = new Color[map.length][map[0].length];

        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[0].length; x++) {
                displayColors[y][x] = map[y][x].genColor();
            }            
        }

        return displayColors;
    }

    private static Tile[][] generateMap(int mapWidth, int mapHeight) {
        Tile[][] map = new Tile[mapHeight][mapWidth];

        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < mapWidth; x++) {
                map[y][x] = new Tile();
            }            
        }
        return map;
    }

    public void spawnCreature() {
        creatures.add(new Creature(nameGenerator.getRandomName(), "Fox", 100, 10, 100.0, 1, 0.1, 0.01, 0.015, (int)(Math.random() * map[0].length), (int)(Math.random() * map.length)));
    }

    public void spawnPlant(String species, int x, int y) {
        Plant plant = new Plant(species, 0.01, 3, 0.1, 0.2, 0.1, x, y);
        if (plantCanSpreadToTile(plant)) plants.add(plant);
    }

    private boolean plantCanSpreadToTile(Plant plant) {
        return map[plant.getY()][plant.getX()].plantSpeciesCanSpread(plant.getSpecies());
    }

    private void removePlantFromTile(Plant plant) {
        map[plant.getY()][plant.getX()].removeSpecies(plant.getSpecies());        
    }

    public void spawnRandomPlant(String species) {
        int x = (int)(Math.random() * map[0].length);
        int y = (int)(Math.random() * map.length);
        spawnPlant(species, x, y);
    }

    public void tick() {
        ArrayList<Plant> deadPlants = new ArrayList<>();
        ArrayList<Plant> newPlants = new ArrayList<>();

        for (Plant p : plants) {
            p.tick();

            if (p.isDead()) {
                deadPlants.add(p);
                removePlantFromTile(p);
                continue;
            }

            Plant spreadPlant = p.spread(map[0].length, map.length);
            if (spreadPlant != null && plantCanSpreadToTile(spreadPlant)) {
                newPlants.add(spreadPlant);
            }
        }

        plants.removeAll(deadPlants);
        plants.addAll(newPlants);

        ArrayList<Creature> deadCreatures = new ArrayList<>();
        ArrayList<Creature> newCreatures = new ArrayList<>();

        for (Creature c : creatures) {
            c.tick();

            if (c.isDead()) {
                deadCreatures.add(c);
                continue;
            }

            c.randomMove(map[0].length, map.length);

            Creature bornCreature = c.reproduce();
            if (bornCreature != null) {
                newCreatures.add(bornCreature);
            }
        }

        creatures.removeAll(deadCreatures);
        creatures.addAll(newCreatures);

        display.updateWorld(new ArrayList<>(creatures), new ArrayList<>(plants));
    }
}