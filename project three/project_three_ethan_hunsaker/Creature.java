
import java.awt.Color;

public class Creature {
    private final String name;
    private final String species;
    private int age;
    private final int maxAge;
    private double satiety;
    private final double maxSatiety;
    private int health;
    private final int maxHealth;
    private final int damage;
    private final double healChance;
    private final double deathChance;
    private final double reproChance;
    private int x;
    private int y;
    private boolean alive;

    public Creature
    (
        String name, String species, int maxAge, 
        int maxHealth, double maxSatiety, 
        int damage, double healChance, 
        double deathChance, double reproChance, 
        int x, int y
    ) 
    {
        this.name = name;
        this.species = species;
        this.age = 0;
        this.maxAge = maxAge;
        this.maxSatiety = maxSatiety;
        this.satiety = maxSatiety;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.damage = damage;
        this.healChance = healChance;
        this.deathChance = deathChance;
        this.reproChance = reproChance;
        this.x = x;
        this.y = y;
        this.alive = true;
    }

    public void die() {
        health = 0;
        alive = false;
    }

    public boolean shouldDie() {
        return age >= maxAge || Math.random() <= deathChance || health <= 0;
    }

    public boolean isDead() {
        return !alive;
    }
    public Creature reproduce() {
        if (Math.random() <= reproChance && alive) {
            return new Creature(name + " Jr.", species, maxAge, maxHealth, maxSatiety, damage, healChance, deathChance, reproChance, x, y);
        }

        return null;
    }

    public void randomMove(int worldWidth, int worldHeight) {
        x += (int)(Math.random() * 3) - 1;
        y += (int)(Math.random() * 3) - 1;

        x = Math.max(0, Math.min(x, worldWidth - 1));
        y = Math.max(0, Math.min(y, worldHeight - 1));
    }

    public void tick() {
        if (!alive) {
            return;
        }

        age++;
        satiety -= 1;

        if (satiety <= 0) {
            health--;
        }

        if (Math.random() <= healChance) {
            health++;
            health = Math.min(health, maxHealth);
        }

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
            case "Fox" -> {
                return new Color(195, 88, 23);
            }
            case "Chicken" -> {
                return new Color(240, 240, 240);
            }
            default -> {
                return new Color(255, 0, 255);
            }
        }
    }
}
