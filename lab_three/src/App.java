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
import java.util.HashSet;
import java.awt.Image;

record Position(int x, int y) {}

class Board {
    private int BOARD_SIZE;    
    private Piece[][] board;
    private Piece[][] nextBoard;
    private HashSet<PieceGroup> groups;

    public Board(int boardSize) {
        BOARD_SIZE = boardSize;
        board = new Piece[BOARD_SIZE][BOARD_SIZE];
        nextBoard = new Piece[BOARD_SIZE][BOARD_SIZE];
        groups = new HashSet<PieceGroup>();
    }

    public void placePiece(int x, int y, boolean blackTurn) {
        if (board[y][x] == null) {
            // System.arraycopy(board, 0, nextBoard, 0, BOARD_SIZE);
            Piece potentialPiece = new Piece(new Position(x, y), blackTurn);
            PieceGroup group = findGroup(potentialPiece);
            board[y][x] = potentialPiece;

            if (group != null) groups.add(group);
            System.out.println(groups);
        }
    }

    public HashSet<Position> findLiberties(int x, int y) {
        HashSet<Position> liberties = new HashSet<>();

        if (y+1 < BOARD_SIZE && board[y+1][x] == null) liberties.add(new Position(x, y+1));
        if (x+1 < BOARD_SIZE && board[y][x+1] == null) liberties.add(new Position(x+1, y));
        if (y-1 >= 0 && board[y-1][x] == null) liberties.add(new Position(x, y-1));
        if (x-1 >= 0 && board[y][x-1] == null) liberties.add(new Position(x-1, y));

        return liberties;
    }

    public PieceGroup findGroup(Piece ungroupedPiece) {
        int x = ungroupedPiece.returnPosition().x();
        int y = ungroupedPiece.returnPosition().y();
        HashSet<Position> liberties = findLiberties(x, y);
        Piece currentPiece;
        PieceGroup currentGroup = null;
        boolean grouped = false;

        int[][] dirs = {{0,1},{1,0},{0,-1},{-1,0}};

        for (int[] d : dirs) {
            int nx = x + d[0];
            int ny = y + d[1];

            if (nx < 0 || ny < 0 || nx >= BOARD_SIZE || ny >= BOARD_SIZE) continue;
            if (board[ny][nx] == null) continue;

            currentPiece = board[ny][nx];

            if (ungroupedPiece.isBlack() == currentPiece.isBlack()) {
                PieceGroup removalGroup = null;
                for (PieceGroup group : groups) {
                    if (group.isPieceInGroup(currentPiece)) {
                        if (!grouped) {
                            group.addPiece(ungroupedPiece, liberties);
                            group.subtractLiberty(ungroupedPiece.returnPosition());
                            currentGroup = group;
                            grouped = true;                            
                        } else {
                            if (group != currentGroup) {
                                currentGroup.addGroup(group);
                                removalGroup = group;
                            } else {
                                currentGroup.subtractLiberty(ungroupedPiece.returnPosition());
                            }
                        }
                    }
                }
                if (removalGroup != null) groups.remove(removalGroup);
            } else {
                for (PieceGroup group : groups) {
                    if (group.isPieceInGroup(currentPiece)) {
                        group.subtractLiberty(ungroupedPiece.returnPosition());
                    }
                }                
            }
        }

        if (!grouped) {
            return new PieceGroup(ungroupedPiece, liberties);
        }
        return null;
    }

    @Override
    public String toString() {
        String boardString = new String();

        for(int i = 0; i < BOARD_SIZE; i++) {
            for(int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == null) {
                    boardString += "0";
                } else {
                    if (board[i][j].isBlack()) {
                        boardString += "X";                        
                    } else {
                        boardString += "O";
                    }
                }
            }
            boardString += "\n";
        }

        return boardString;
    }
}

class Piece {
    private boolean black;
    private Position position;

    public Piece(Position pos, boolean isBlack) {
        position = pos;
        black = isBlack;
    }

    public Position returnPosition() {
        return position;
    }
    public boolean isBlack() {
        return black;
    }
}

class PieceGroup {
    private HashSet<Piece> pieces;
    private HashSet<Position> liberties;

    public PieceGroup(Piece piece, HashSet<Position> libs) {
        pieces = new HashSet<>();
        pieces.add(piece);
        liberties = new HashSet<>();
        liberties.addAll(libs);
    }

    public boolean isPieceInGroup(Piece piece) {
        return pieces.contains(piece);
    }

    public HashSet<Piece> returnPieces() {
        return new HashSet<Piece>(pieces);
    }

    public HashSet<Position> returnLiberties() {
        return new HashSet<Position>(liberties);
    }

    public void subtractLiberty(Position liberty) {
        Position toBeRemoved = null;
        for (Position lib : liberties) {
            if (lib.x() == liberty.x() && lib.y() == liberty.y()) {
                toBeRemoved = lib;
                System.out.println(lib);
            }
        }
        if (toBeRemoved != null) liberties.remove(toBeRemoved);
    }

    public void addPiece(Piece piece, HashSet<Position> libs) {
        pieces.add(piece);
        liberties.addAll(libs);
    }

    public void addGroup(PieceGroup group) {
        pieces.addAll(group.returnPieces());
        liberties.addAll(group.returnLiberties());

    }

    @Override
    public String toString() {
        String printGroups = new String();
        printGroups += "Piece Group (" + pieces.size() + " Pieces, " + liberties.size() + " Liberties)\n";
        return printGroups;
    }
}

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





        

        Board newBoard = new Board(9);

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
                                newBoard.placePiece(x, y, turn);
                            } else {
                                button.setIcon(whitePiece);
                                board[y][x] = 2;
                                newBoard.placePiece(x, y, turn);
                            }
                            turn = !turn;
                            // System.out.println(newBoard);
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