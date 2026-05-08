import java.awt.Color;
import java.util.ArrayList;

public class Tile {
    private final String biome;
    // private double temp;
    private final Color color;
    private final ArrayList<String> plantSpecies;

    public Tile() {
        this.biome = "Forest";
        // this.temp = 20.0;
        this.color = setColor();
        this.plantSpecies = new ArrayList<>();
    }

    private Color setColor() {
        int rJitter = (int) (Math.random() * 11) - 5;
        int gJitter = (int) (Math.random() * 11) - 5;
        int bJitter = (int) (Math.random() * 11) - 5;

        switch (biome) {
            case "Forest" -> {
                return new Color(63 + rJitter, 155 + gJitter, 11 + bJitter);
            }
            default -> {
                return new Color(255, 0, 255);
            }
        }
    }

    public Color genColor() {
        return color;
    }    

    public boolean plantSpeciesCanSpread(String species) {
        if (!plantSpecies.contains(species)) {
            plantSpecies.add(species);
            return true;
        } 
        return false;
    }

    public void removeSpecies(String species) {
        plantSpecies.remove(species);
    }
}
