import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;


public class App {
    private static final int WINDOW_HEIGHT = 700;
    private static final int WINDOW_WIDTH = 700;
    private static final int BOARD_SIZE = 9;
    private static final int LINE_WEIGHT = 2;

    static void board() {
        int[][] board = new int[BOARD_SIZE][BOARD_SIZE];
        JFrame frame = new JFrame("Go Board");
        frame.pack();
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        JLayeredPane layers = new JLayeredPane();
        layers.setLayout(null);
        frame.setContentPane(layers);

        Dimension frameSize = frame.getContentPane().getSize();

        int shortSide = (frameSize.height > frameSize.width) ? frameSize.width : frameSize.height;

        int boardSize = (int) Math.round(shortSide * 0.8);
        
        JPanel boardPanel = new JPanel(new GridLayout(1, 1));

        JPanel displayPanel = new JPanel(new GridLayout(BOARD_SIZE - 1, BOARD_SIZE - 1, LINE_WEIGHT, LINE_WEIGHT));
        displayPanel.setBackground(Color.BLACK);

        for(int i = 0; i < BOARD_SIZE - 1; i++) {
            for(int j = 0; j < BOARD_SIZE - 1; j++) {
                displayPanel.add(new JPanel());
            }
        }

        boardPanel.add((displayPanel));

        layers.add(boardPanel, JLayeredPane.DEFAULT_LAYER);

        JPanel interactionPanel = new JPanel(new GridLayout(1, 1));
        interactionPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        JPanel buttonPanel = new JPanel(new GridLayout(BOARD_SIZE, BOARD_SIZE));
        for(int i = 0; i < BOARD_SIZE; i++) {
            for(int j = 0; j < BOARD_SIZE; j++) {
                JButton button = new JButton();
                switch (board[j][i]) {
                    case 0:
                        
                        break;
                    
                    case 1:

                        break;

                    case -1: 

                        break;

                    default:
                        break;
                }
                buttonPanel.add(new JButton("hi"));
            }
        }

        interactionPanel.add(buttonPanel);

        layers.add(interactionPanel, JLayeredPane.PALETTE_LAYER);

        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Dimension frameSize = frame.getContentPane().getSize();

                int shortSide = (frameSize.height > frameSize.width) ? frameSize.width : frameSize.height;

                int boardSize = (int) Math.round(shortSide * 0.8);
                
                boardPanel.setBounds(frameSize.width / 2 - boardSize / 2, frameSize.height / 2 - boardSize / 2, boardSize, boardSize);
            }
        });

        boardPanel.setBounds(0, 0, boardSize, boardSize);
        interactionPanel.setBounds(0, 0, 10, 10);
        frame.setVisible(true);
    }

    public static void main(String[] args) throws Exception {
        board();
    }
}

