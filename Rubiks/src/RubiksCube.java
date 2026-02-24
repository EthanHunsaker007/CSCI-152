import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.*;

public class RubiksCube extends JPanel implements ActionListener {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private double angleX = -0.5, angleY = 0.5;
    @SuppressWarnings("FieldMayBeFinal")
    private double angleZ = 0;
    private Point lastMousePos;
    private final List<Cubie> cubies = new ArrayList<>();
    private boolean isAutoRotating = true;

    public RubiksCube() {
        setBackground(new Color(20, 20, 20));
        
        // Initialize 27 cubies (3x3x3)
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    cubies.add(new Cubie(x, y, z));
                }
            }
        }

        // Mouse listeners for rotation logic
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastMousePos = e.getPoint();
                isAutoRotating = false; // Stop auto-rotation on click
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastMousePos != null) {
                    Point currentPos = e.getPoint();
                    // Calculate distance moved since last frame
                    double dx = currentPos.x - lastMousePos.x;
                    double dy = currentPos.y - lastMousePos.y;

                    // Update rotation angles (sensitivity of 0.01)
                    angleY += dx * 0.01;
                    angleX += dy * 0.01;

                    lastMousePos = currentPos;
                    repaint();
                }
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);

        Timer timer = new Timer(16, this);
        timer.start();
    }

    public void setCubeColors(char[] faceData) {
        for (Cubie cubie : cubies) {
            // Convert screen coordinates back to grid indices (-1, 0, 1)
            int gx = (int) Math.round(cubie.ox / (cubie.size + cubie.gap));
            int gy = (int) Math.round(cubie.oy / (cubie.size + cubie.gap));
            int gz = (int) Math.round(cubie.oz / (cubie.size + cubie.gap));

            // Front Face (Z = 1)
            if (gz == 1) cubie.faceColors[0] = getColor(faceData[18 + (1-gy)*3 + (gx+1)]);
            // Back Face (Z = -1)
            if (gz == -1) cubie.faceColors[1] = getColor(faceData[36 + (1-gy)*3 + (1-gx)]);
            // Right Face (X = 1)
            if (gx == 1) cubie.faceColors[2] = getColor(faceData[45 + (1-gy)*3 + (1-gz)]);
            // Left Face (X = -1)
            if (gx == -1) cubie.faceColors[3] = getColor(faceData[27 + (1-gy)*3 + (gz+1)]);
            // Top Face (Y = -1)
            if (gy == 1) cubie.faceColors[4] = getColor(faceData[(gz+1)*3 + (gx+1)]);
            // Bottom Face (Y = 1)
            if (gy == -1) cubie.faceColors[5] = getColor(faceData[9 + (1-gz)*3 + (gx+1)]);
        }
        repaint();
    }

    private Color getColor(char code) {
        return switch (code) {
            case 'w' -> Color.WHITE;
            case 'y' -> Color.YELLOW;
            case 'r' -> new Color(183, 18, 52);
            case 'o' -> new Color(255, 88, 0);
            case 'b' -> new Color(0, 70, 173);
            case 'g' -> new Color(0, 155, 72);
            default -> Color.BLACK;
        };
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isAutoRotating) {
            angleX += 0.005;
            angleY += 0.01;
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        // 1. Project all faces
        List<Face> allFaces = new ArrayList<>();
        for (Cubie cubie : cubies) {
            allFaces.addAll(cubie.getProjectedFaces(angleX, angleY, angleZ, centerX, centerY));
        }

        // 2. Sort by Z-depth (Painters Algorithm)
        Collections.sort(allFaces, (f1, f2) -> Double.compare(f2.avgZ, f1.avgZ));

        // 3. Render
        for (Face face : allFaces) {
            g2d.setColor(face.color);
            g2d.fill(face.path);
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(1.5f));
            g2d.draw(face.path);
        }
        
        // Instructions overlay
        g2d.setColor(Color.GRAY);
        g2d.drawString("Drag mouse to rotate", 20, 30);
    }

    private static class Cubie {
        double ox, oy, oz; 
        double size = 50; 
        double gap = 4;
        // Order: Front, Back, Right, Left, Top, Bottom
        Color[] faceColors = new Color[6]; 

        Cubie(double x, double y, double z) {
            this.ox = x * (size + gap);
            this.oy = y * (size + gap);
            this.oz = z * (size + gap);
            
            // Initialize with default black/hidden interior
            for(int i=0; i<6; i++) faceColors[i] = Color.BLACK;
        }

        List<Face> getProjectedFaces(double ax, double ay, double az, int cx, int cy) {
            List<Face> faces = new ArrayList<>();
            double s = size / 2.0;

            // Use the faceColors array instead of hardcoded values
            faces.add(createFace(new double[][]{{s,s,s}, {-s,s,s}, {-s,-s,s}, {s,-s,s}}, faceColors[0], ax, ay, az, cx, cy)); // Front
            faces.add(createFace(new double[][]{{s,s,-s}, {-s,s,-s}, {-s,-s,-s}, {s,-s,-s}}, faceColors[1], ax, ay, az, cx, cy)); // Back
            faces.add(createFace(new double[][]{{s,s,s}, {s,-s,s}, {s,-s,-s}, {s,s,-s}}, faceColors[2], ax, ay, az, cx, cy));   // Right
            faces.add(createFace(new double[][]{{-s,s,s}, {-s,-s,s}, {-s,-s,-s}, {-s,s,-s}}, faceColors[3], ax, ay, az, cx, cy)); // Left
            faces.add(createFace(new double[][]{{s,s,s}, {s,s,-s}, {-s,s,-s}, {-s,s,s}}, faceColors[4], ax, ay, az, cx, cy));  // Top
            faces.add(createFace(new double[][]{{s,-s,s}, {s,-s,-s}, {-s,-s,-s}, {-s,-s,s}}, faceColors[5], ax, ay, az, cx, cy)); // Bottom

            return faces;
        }

        

        private Face createFace(double[][] localVertices, Color color, double ax, double ay, @SuppressWarnings("unused") double az, int cx, int cy) {
            Polygon poly = new Polygon();
            double totalZ = 0;

            for (double[] lv : localVertices) {
                double x = lv[0] + ox;
                double y = lv[1] + oy;
                double z = lv[2] + oz;

                // Rotation X
                double dy = y * Math.cos(ax) - z * Math.sin(ax);
                double dz = y * Math.sin(ax) + z * Math.cos(ax);
                y = dy; z = dz;

                // Rotation Y
                double dx = x * Math.cos(ay) + z * Math.sin(ay);
                dz = -x * Math.sin(ay) + z * Math.cos(ay);
                x = dx; z = dz;

                // Perspective Projection
                double fov = 800;
                double viewDistance = 1000;
                double scale = fov / (viewDistance + z);
                
                poly.addPoint((int) (x * scale) + cx, (int) (y * scale) + cy);
                totalZ += z;
            }

            return new Face(poly, color, totalZ / 4.0);
        }
    }

    private static class Face {
        Polygon path;
        Color color;
        double avgZ;

        Face(Polygon path, Color color, double avgZ) {
            this.path = path;
            this.color = color;
            this.avgZ = avgZ;
        }
    }

    public void showVectorCube(VectorCube cube) {
        char[] faceData = cube.cube;
        JFrame frame = new JFrame("Interactive 3D Rubik's Cube");
        RubiksCube Cube = new RubiksCube();
        Cube.setCubeColors(faceData);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(40, 40, 40));

        JButton UBtn = new JButton("U");
        UBtn.addActionListener(e -> {
            cube.u();
            Cube.setCubeColors(cube.cube);
            cube.displayCubeTerminal();
        });

        JButton DBtn = new JButton("D");
        DBtn.addActionListener(e -> {
            cube.d();
            Cube.setCubeColors(cube.cube);
            cube.displayCubeTerminal();
        });

        JButton RBtn = new JButton("R");
        RBtn.addActionListener(e -> {
            cube.r();
            Cube.setCubeColors(cube.cube);
            cube.displayCubeTerminal();
        });

        JButton LBtn = new JButton("L");
        LBtn.addActionListener(e -> {
            cube.l();
            Cube.setCubeColors(cube.cube);
            cube.displayCubeTerminal();
        });

        JButton FBtn = new JButton("F");
        FBtn.addActionListener(e -> {
            cube.f();
            Cube.setCubeColors(cube.cube);
            cube.displayCubeTerminal();
        });

        JButton BBtn = new JButton("B");
        BBtn.addActionListener(e -> {
            cube.b();
            Cube.setCubeColors(cube.cube);
            cube.displayCubeTerminal();
        });
        JButton uBtn = new JButton("U'");
        uBtn.addActionListener(e -> {
            cube.U();
            Cube.setCubeColors(cube.cube);
            cube.displayCubeTerminal();
        });

        JButton dBtn = new JButton("D'");
        dBtn.addActionListener(e -> {
            cube.D();
            Cube.setCubeColors(cube.cube);
            cube.displayCubeTerminal();
        });

        JButton rBtn = new JButton("R'");
        rBtn.addActionListener(e -> {
            cube.R();
            Cube.setCubeColors(cube.cube);
            cube.displayCubeTerminal();
        });

        JButton lBtn = new JButton("L'");
        lBtn.addActionListener(e -> {
            cube.L();
            Cube.setCubeColors(cube.cube);
            cube.displayCubeTerminal();
        });

        JButton fBtn = new JButton("F'");
        fBtn.addActionListener(e -> {
            cube.F();
            Cube.setCubeColors(cube.cube);
            cube.displayCubeTerminal();
        });

        JButton bBtn = new JButton("B'");
        bBtn.addActionListener(e -> {
            cube.B();
            Cube.setCubeColors(cube.cube);
            cube.displayCubeTerminal();
        });
        JButton scrambleBtn = new JButton("Scramble");
        scrambleBtn.addActionListener(e -> {
            cube.randomizeCube(100);
            Cube.setCubeColors(cube.cube);
            cube.displayCubeTerminal();
        });

        buttonPanel.add(UBtn);
        buttonPanel.add(uBtn);
        buttonPanel.add(DBtn);
        buttonPanel.add(dBtn);
        buttonPanel.add(RBtn);
        buttonPanel.add(rBtn);
        buttonPanel.add(LBtn);
        buttonPanel.add(lBtn);
        buttonPanel.add(FBtn);
        buttonPanel.add(fBtn);
        buttonPanel.add(BBtn);
        buttonPanel.add(bBtn);
        buttonPanel.add(scrambleBtn);

        // Layout management
        frame.setLayout(new BorderLayout());
        frame.add(Cube, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.setSize(800, 800);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void showCubieCube(CubieCube cube) {
        char[] faceData = cube.as1DFaceletArray();
        JFrame frame = new JFrame("Interactive 3D Rubik's Cube");
        RubiksCube Cube = new RubiksCube();
        Cube.setCubeColors(faceData);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(40, 40, 40));

        JButton UBtn = new JButton("U");
        UBtn.addActionListener(e -> {
            cube.move(0);
            Cube.setCubeColors(cube.as1DFaceletArray());
        });

        JButton DBtn = new JButton("D");
        DBtn.addActionListener(e -> {
            cube.move(1);
            Cube.setCubeColors(cube.as1DFaceletArray());
        });

        JButton RBtn = new JButton("R");
        RBtn.addActionListener(e -> {
            cube.move(3);
            Cube.setCubeColors(cube.as1DFaceletArray());
        });

        JButton LBtn = new JButton("L");
        LBtn.addActionListener(e -> {
            cube.move(2);
            Cube.setCubeColors(cube.as1DFaceletArray());
        });

        JButton FBtn = new JButton("F");
        FBtn.addActionListener(e -> {
            cube.move(4);
            Cube.setCubeColors(cube.as1DFaceletArray());
        });

        JButton BBtn = new JButton("B");
        BBtn.addActionListener(e -> {
            cube.move(5);
            Cube.setCubeColors(cube.as1DFaceletArray());
        });

        JButton scrambleBtn = new JButton("Scramble");
        scrambleBtn.addActionListener(e -> {
            cube.randomizeCube(100);
            Cube.setCubeColors(cube.as1DFaceletArray());
        });

        JButton solveBtn = new JButton("Solve");
        solveBtn.addActionListener(e -> {
            int[] solveMoves = CubieCube.solveCube(cube);
            AtomicInteger m = new AtomicInteger(0);
            Runnable moveIncrement = () -> {
                if (m.get() > solveMoves.length) {
                    scheduler.shutdown();
                    return;
                }
                cube.move(solveMoves[m.getAndIncrement()]);
                Cube.setCubeColors(cube.as1DFaceletArray());
            };
            scheduler.scheduleAtFixedRate(moveIncrement, 0, 500, MILLISECONDS);
        });

        buttonPanel.add(UBtn);
        buttonPanel.add(DBtn);
        buttonPanel.add(LBtn);
        buttonPanel.add(RBtn);
        buttonPanel.add(FBtn);
        buttonPanel.add(BBtn);
        buttonPanel.add(scrambleBtn);
        buttonPanel.add(solveBtn);

        // Layout management
        frame.setLayout(new BorderLayout());
        frame.add(Cube, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.setSize(800, 800);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}