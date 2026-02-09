import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.HashSet;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

public class App {
    private static final int WINDOW_HEIGHT = 700;
    private static final int WINDOW_WIDTH = 700;
    private static final int BOARD_SIZE = 13;
    private static final Color BOARD_COLOR = new Color(207, 185, 151);
    private static final int LINE_WEIGHT = 2;
    public static int pieceSize;
    private final static JButton[] buttons = new JButton[BOARD_SIZE * BOARD_SIZE];
    static boolean turn = true;
    private final static HashSet<Position> modifiedPositions = new HashSet<>();

    static void board() {
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
        pieceSize = cellSize / 2;
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


        Board newBoard = new Board(BOARD_SIZE);

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
                    modifiedPositions.clear();
                    turn = newBoard.clicked(x, y, turn) ? !turn : turn;
                    updateButtons(newBoard, buttons);
                }); 
                buttons[i * BOARD_SIZE + j] = button;                   
            }
        }

        layers.add(buttonPanel, JLayeredPane.PALETTE_LAYER);

        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Dimension frameSize = frame.getContentPane().getSize();

                int shortSide = (frameSize.height > frameSize.width) ? frameSize.width : frameSize.height;

                int boardSize = (int) Math.round(shortSide * 0.8);
                int cellSize = boardSize / (BOARD_SIZE - 1);
                pieceSize = cellSize / 2;
                int panelSize = boardSize + cellSize;
                
                displayPanel.setBounds(frameSize.width / 2 - boardSize / 2, frameSize.height / 2 - boardSize / 2, boardSize, boardSize);
                buttonPanel.setBounds(frameSize.width / 2 - panelSize / 2, frameSize.height / 2 - panelSize / 2, panelSize, panelSize);
                updateButtons(newBoard, buttons);
            }
        });

        displayPanel.setBounds(frameSize.width / 2 - boardSize / 2, frameSize.height / 2 - boardSize / 2, boardSize, boardSize);
        buttonPanel.setBounds(frameSize.width / 2 - panelSize / 2, frameSize.height / 2 - panelSize / 2, panelSize, panelSize);
        frame.setVisible(true);
    }

    private static void updateButtons(Board board, JButton[] buttons) {  
        try {
            Image blackPieceImg = ImageIO.read(App.class.getResource("circleBlack.png")).getScaledInstance(pieceSize, pieceSize, Image.SCALE_DEFAULT);
            Image whitePieceImg = ImageIO.read(App.class.getResource("circleWhite.png")).getScaledInstance(pieceSize, pieceSize, Image.SCALE_DEFAULT);

            ImageIcon blackPiece = new ImageIcon(blackPieceImg);
            ImageIcon whitePiece = new ImageIcon(whitePieceImg);

            for(int i = 0; i < BOARD_SIZE; i++) {
                for(int j = 0; j < BOARD_SIZE; j++) {
                    Stone color = board.getPieceColor(new Position(j, i));

                    ImageIcon setIcon;

                    setIcon = switch (color) {
                        case EMPTY -> null;
                        case BLACK -> blackPiece;
                        case WHITE -> whitePiece;
                        default -> null;
                    };
                    buttons[i * BOARD_SIZE + j].setIcon(setIcon);
                }
            }

        } catch (IOException ex) {
            System.out.println(ex);
        }
    }

    public static void main(String[] args) throws Exception {
        board();
    }
}