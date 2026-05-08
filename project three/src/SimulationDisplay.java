import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class SimulationDisplay {
    private final SimulationPanel panel;
    private final JFrame frame;
    
    public SimulationDisplay(Color[][] gridColors) {
        this.panel = new SimulationPanel(gridColors);
        this.frame = new JFrame("Life Simulation");
        frame.add(panel);
        frame.pack();
        frame.setDefaultCloseOperation(
            JFrame.EXIT_ON_CLOSE
        );
        frame.setVisible(true);
    }

    public void updateWorld(ArrayList<Creature> newCreatures, ArrayList<Plant> newPlants) {
        panel.updateWorld(newCreatures, newPlants);
        panel.repaint();
    }
}


class SimulationPanel extends JPanel {
    private final Color[][] gridColors;
    private final int cellSize;
    private ArrayList<Creature> creatures;
    private ArrayList<Plant> plants;

    public SimulationPanel(Color[][] gridColors) {
        this.gridColors = gridColors;
        this.cellSize = 30;
        this.creatures = new ArrayList<>();
        this.plants = new ArrayList<>();
        
        setPreferredSize(
            new Dimension(
                gridColors[0].length * cellSize,
                gridColors.length * cellSize
            )
        );
    }

    public void updateWorld(ArrayList<Creature> newCreatures, ArrayList<Plant> newPlants) {
        this.plants = newPlants;
        this.creatures = newCreatures;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int y = 0; y < gridColors.length; y++) {
            for (int x = 0; x < gridColors[0].length; x++) {

                g.setColor(gridColors[y][x]);
                g.fillRect(x*cellSize, y*cellSize, cellSize, cellSize);
                // g.setColor(Color.BLACK);
                // g.drawRect(x*cellSize, y*cellSize, cellSize, cellSize);
            }
        }

        for (Plant p : plants) {
            g.setColor(p.getColor());
            double diameter = cellSize * p.getSizeModifier();
            double inset = cellSize / 2 - diameter / 2;
            g.fillOval((int)(p.getX() * cellSize + inset), (int)(p.getY() * cellSize + inset), (int) diameter, (int) diameter);
        }

        for (Creature c : creatures) {
            g.setColor(c.getColor());
            int diameter = cellSize / 2;
            int inset = cellSize / 2 - diameter / 2;
            g.fillRect(c.getX() * cellSize + inset, c.getY() * cellSize + inset, diameter, diameter);
        }
    }
}
