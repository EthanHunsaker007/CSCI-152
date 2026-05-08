
import java.awt.Color;

public class Plant {
    private final String species;
    private final double deathChance;
    private double foodValue;
    private final double maxFoodValue;
    private final double growthRate;
    private final double growthChance;
    private final double spreadChance;
    private boolean alive;
    private final int x;
    private final int y;

    public Plant(String species, double deathChance, double maxFoodValue, double growthRate, double growthChance, double spreadChance, int x, int y) {
        this.species = species;
        this.deathChance = deathChance;
        this.foodValue = 0;
        this.maxFoodValue = maxFoodValue;
        this.growthRate = growthRate;
        this.growthChance = growthChance;
        this.spreadChance = spreadChance;
        this.alive = true;
        this.x = x;
        this.y = y;
    }

    public Plant spread(int gridWidth, int gridHeight) {
        if (Math.random() > spreadChance) {
            return null;
        }
        
        int newX = x;
        int newY = y;
        int spreadStep = (int)(Math.random() * 3) - 1;

        if (Math.random() < 0.5) {
            newX += spreadStep;
        } else {
            newY += spreadStep;
        }

        if (newX < 0 || newX >= gridWidth || newY < 0 || newY >= gridHeight) {
            return null;
        }

        return new Plant(species, deathChance, maxFoodValue, growthRate, growthChance, spreadChance, newX, newY);
    }

    public void grow() {
        if (Math.random() <= growthChance) {
            foodValue += growthRate;
            foodValue = Math.min(foodValue, maxFoodValue);
        }
    }

    public boolean shouldDie() {
        return Math.random() <= deathChance;
    }

    public void die() {
        foodValue = 0;
        alive = false;
    }

    public boolean isDead() {
        return !alive;
    }

    public void tick() {
        if (!alive) {
            return;
        }

        grow();

        if (shouldDie()) {
            die();
        }
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Color getColor() {
        switch (species) {
            case "Grass" -> {
                return new Color(50, 205,50);
            }
            default -> {
                return new Color(255, 0, 255);
            }
        }
    }

    public double getSizeModifier() {
        return foodValue / maxFoodValue;
    }

    public String getSpecies() {
        return species;
    }
}
