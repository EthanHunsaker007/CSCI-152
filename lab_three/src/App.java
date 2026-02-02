import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.Image;


public class App {
    private static final int WINDOW_HEIGHT = 700;
    private static final int WINDOW_WIDTH = 700;
    private static final int BOARD_SIZE = 9;
    private static final Color BOARD_COLOR = new Color(207, 185, 151);
    private static final int LINE_WEIGHT = 2;
    static boolean turn = true;

    static void board() {
        int[][] board = new int[BOARD_SIZE][BOARD_SIZE];

        JFrame frame = new JFrame("Go Board");
        frame.pack();
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setBackground(BOARD_COLOR);
        JLayeredPane layers = new JLayeredPane();
        layers.setLayout(null);
        frame.setContentPane(layers);

        Dimension frameSize = frame.getContentPane().getSize();

        int shortSide = (frameSize.height > frameSize.width) ? frameSize.width : frameSize.height;

        int boardSize = (int) Math.round(shortSide * 0.8);
        int cellSize = boardSize / (BOARD_SIZE - 1);
        int pieceSize = cellSize / 2;
        int panelSize = boardSize + cellSize;

        JPanel displayPanel = new JPanel(new GridLayout(BOARD_SIZE - 1, BOARD_SIZE - 1, LINE_WEIGHT, LINE_WEIGHT));
        displayPanel.setBackground(Color.BLACK);

        for(int i = 0; i < BOARD_SIZE - 1; i++) {
            for(int j = 0; j < BOARD_SIZE - 1; j++) {
                JPanel panel = new JPanel();
                panel.setBackground(BOARD_COLOR);
                displayPanel.add(panel);
            }
        }


        layers.add(displayPanel, JLayeredPane.DEFAULT_LAYER);
            
        JPanel buttonPanel = new JPanel(new GridLayout(BOARD_SIZE, BOARD_SIZE));
        buttonPanel.setOpaque(false);

        try {
            Image blackPieceImg = ImageIO.read(App.class.getResource("circleBlack.png")).getScaledInstance(pieceSize, pieceSize, Image.SCALE_DEFAULT);
            Image whitePieceImg = ImageIO.read(App.class.getResource("circleWhite.png")).getScaledInstance(pieceSize, pieceSize, Image.SCALE_DEFAULT);

            ImageIcon blackPiece = new ImageIcon(blackPieceImg);
            ImageIcon whitePiece = new ImageIcon(whitePieceImg);

            for(int i = 0; i < BOARD_SIZE; i++) {
                for(int j = 0; j < BOARD_SIZE; j++) {
                    JButton button = new JButton();
                    button.setBorderPainted(false);
                    button.setContentAreaFilled(false);
                    button.setFocusPainted(false);
                    button.setOpaque(false); 
                    buttonPanel.add(button); 

                    final int y = i;
                    final int x = j;

                    button.addActionListener(e -> {
                        if (board[y][x] == 0) {
                            if (turn) {
                                button.setIcon(blackPiece);
                                board[y][x] = 1;
                            } else {
                                button.setIcon(whitePiece);
                                board[y][x] = 2;
                            }
                            turn = !turn;
                        }
                        for (int[] row : board) {
                            for (int element : row) {
                                System.out.print(element);
                            }
                            System.out.println();
                        }
                    });                    
                }
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }


        layers.add(buttonPanel, JLayeredPane.PALETTE_LAYER);

        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Dimension frameSize = frame.getContentPane().getSize();

                int shortSide = (frameSize.height > frameSize.width) ? frameSize.width : frameSize.height;

                int boardSize = (int) Math.round(shortSide * 0.8);
                int cellSize = boardSize / (BOARD_SIZE - 1);
                int panelSize = boardSize + cellSize;
                
                displayPanel.setBounds(frameSize.width / 2 - boardSize / 2, frameSize.height / 2 - boardSize / 2, boardSize, boardSize);
                buttonPanel.setBounds(frameSize.width / 2 - panelSize / 2, frameSize.height / 2 - panelSize / 2, panelSize, panelSize);
            }
        });

        displayPanel.setBounds(frameSize.width / 2 - boardSize / 2, frameSize.height / 2 - boardSize / 2, boardSize, boardSize);
        buttonPanel.setBounds(frameSize.width / 2 - panelSize / 2, frameSize.height / 2 - panelSize / 2, panelSize, panelSize);
        frame.setVisible(true);
    }

    public static void main(String[] args) throws Exception {
        board();
    }
}

