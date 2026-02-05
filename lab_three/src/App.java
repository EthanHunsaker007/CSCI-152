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
import java.util.HashMap;
import java.util.HashSet;
import java.awt.Image;

record Position(int x, int y) {}
enum Stone { BLACK, WHITE, EMPTY }

class Board {
    private int BOARD_SIZE;    
    private Piece[][] board;
    private HashSet<PieceGroup> groups;
    private HashSet<Position> modifiedPieces;
    private HashMap<Position, PieceGroup> groupedPieces;
    private final int[][] dirs = {{0,1},{1,0},{0,-1},{-1,0}};

    public Board(int boardSize) {
        BOARD_SIZE = boardSize;
        board = new Piece[BOARD_SIZE][BOARD_SIZE];
        groups = new HashSet<PieceGroup>();
        modifiedPieces = new HashSet<>();
        groupedPieces = new HashMap<>();
    }

    public Stone getPieceColor(Position pos) {
        Piece piece = board[pos.y()][pos.x()];
        if (piece == null) {
            return Stone.EMPTY;
        } else if (piece.isBlack()) {
            return Stone.BLACK;
        } else {
            return Stone.WHITE;
        }
    }

    private void captureGroup(PieceGroup group) {
        for (Piece p : group.returnPieces()) {
            Position pos = p.returnPosition();
            removePiece(pos);
            Position[] neighbors = findNeighbors(p);
            for (Position n : neighbors) {
                if (n == null) break;
                PieceGroup neigborGroup = groupedPieces.get(n);
                neigborGroup.addLiberty(pos);
            }
        }
    }

    public HashSet<Position> drainModifiedPieces() {
        HashSet<Position> drain = new HashSet<>(modifiedPieces);
        modifiedPieces.clear();
        return drain;
    }

    private void removePiece(Position pos) {
        board[pos.y()][pos.x()] = null;
        modifiedPieces.add(pos);
    }

    public boolean placePiece(int x, int y, boolean blackTurn) {
        if (board[y][x] == null) {         
            Piece potentialPiece = new Piece(new Position(x, y), blackTurn);
            PieceGroup group = findGroup(potentialPiece);
            board[y][x] = potentialPiece;
            modifiedPieces.add(potentialPiece.returnPosition());

            HashSet<PieceGroup> capturedGroups = new HashSet<>();

            for (PieceGroup g: groups) {
                if (g.returnLiberties().size() == 0 && g != group) {
                    capturedGroups.add(g);
                }
            }

            for (PieceGroup g: capturedGroups) {
                captureGroup(g);
                groups.remove(g);
            }

            if (group.returnLiberties().size() == 0) {
                captureGroup(group);
                groups.remove(group);
            }

            System.out.println(groups);
            return true;
        } else {
            return false;
        }
    } 

    private Position[] findNeighbors(Piece p) {
        Position[] neighbors = new Position[4];
        int neighbor = 0;
        Position pos = p.returnPosition();

        for (int[] dir : dirs) {
            int nx = pos.x() + dir[0];
            int ny = pos.y() + dir[1];

            if (nx < 0 || ny < 0 || nx >= BOARD_SIZE || ny >= BOARD_SIZE) continue;
            if (board[ny][nx] == null) continue;

            Position piecePos = new Position(nx, ny);
            boolean isBlack = groupedPieces.get(piecePos).isBlack();

            if (p.isBlack() != isBlack) {
                neighbors[neighbor] = piecePos;
                neighbor++;
            }
        }    
        return neighbors;   
    }

    private HashSet<Position> findLiberties(int x, int y) {
        HashSet<Position> libs = new HashSet<>();

        for (int[] dir : dirs) {
            int nx = x + dir[0];
            int ny = y + dir[1];

            if (nx < 0 || ny < 0 || nx >= BOARD_SIZE || ny >= BOARD_SIZE) continue;
            if (board[ny][nx] != null) continue;

            libs.add(new Position(nx, ny));
        }

        return libs;
    }

    private PieceGroup findGroup(Piece ungroupedPiece) {
        Position pos = ungroupedPiece.returnPosition();
        HashSet<Position> liberties = findLiberties(pos.x(), pos.y());
        Piece currentPiece;
        PieceGroup currentGroup = null;
        boolean grouped = false;

        for (int[] dir : dirs) {
            int nx = pos.x() + dir[0];
            int ny = pos.y() + dir[1];

            if (nx < 0 || ny < 0 || nx >= BOARD_SIZE || ny >= BOARD_SIZE) continue;
            if (board[ny][nx] == null) continue;

            currentPiece = board[ny][nx];
            PieceGroup group = groupedPieces.get(currentPiece.returnPosition());

            if (ungroupedPiece.isBlack() == currentPiece.isBlack()) {
                PieceGroup removalGroup = null;

                if (!grouped) {
                    group.addPiece(ungroupedPiece, liberties);
                    group.subtractLiberty(ungroupedPiece.returnPosition());
                    currentGroup = group;
                    grouped = true;                            
                } else {
                    if (group != currentGroup) {
                        currentGroup.addGroup(group);
                        removalGroup = group;
                    }
                    currentGroup.subtractLiberty(ungroupedPiece.returnPosition());
                }
                if (removalGroup != null) groups.remove(removalGroup);
            } else {
                group.subtractLiberty(ungroupedPiece.returnPosition());
            }
        }                

        if (!grouped) {
            PieceGroup returnGroup = new PieceGroup(ungroupedPiece, liberties);
            groups.add(returnGroup);
            groupedPieces.put(ungroupedPiece.returnPosition(), returnGroup);
            return returnGroup;
        }
        groupedPieces.put(ungroupedPiece.returnPosition(), currentGroup);
        return currentGroup;
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
    private boolean black;
    private HashSet<Piece> pieces;
    private HashSet<Position> liberties;

    public PieceGroup(Piece piece, HashSet<Position> libs) {
        black = piece.isBlack();
        pieces = new HashSet<>();
        pieces.add(piece);
        liberties = new HashSet<>();
        liberties.addAll(libs);
    }

    public boolean isPieceInGroup(Piece piece) {
        return pieces.contains(piece);
    }

    public HashSet<Piece> returnPieces() {
        return pieces;
    }

    public HashSet<Position> returnLiberties() {
        return liberties;
    }

    public boolean isBlack() {
        return black;
    }

    public void subtractLiberty(Position liberty) {
        liberties.remove(liberty);
    }

    public void addLiberty(Position liberty) {
        liberties.add(liberty);
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
        printGroups += "Piece Group (" + pieces.size() + " Pieces, " + liberties.size() + " Liberties, " + (black ? "Black" : "White") + ")\n";
        printGroups += "Liberties:\n";
        for (Position l : liberties) {
            printGroups += l + "\n";
        }
        return printGroups;
    }
}

public class App {
    private static final int WINDOW_HEIGHT = 700;
    private static final int WINDOW_WIDTH = 700;
    private static final int BOARD_SIZE = 13;
    private static final Color BOARD_COLOR = new Color(207, 185, 151);
    private static final int LINE_WEIGHT = 2;
    public static int pieceSize;
    private static JButton[] buttons = new JButton[BOARD_SIZE * BOARD_SIZE];
    static boolean turn = true;
    private static HashSet<Position> modifiedPositions = new HashSet<>();

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
                    turn = newBoard.placePiece(x, y, turn) ? !turn : turn;
                    modifiedPositions = newBoard.drainModifiedPieces();
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

                    switch (color) {
                        case EMPTY:
                            setIcon = null;
                            break;
                        
                        case BLACK:
                            setIcon = blackPiece;
                            break;

                        case WHITE:
                            setIcon = whitePiece;
                            break;

                        default:
                            setIcon = null;
                            break;
                    }
                    buttons[i * BOARD_SIZE + j].setIcon(setIcon);
                }
            }

        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    public static void main(String[] args) throws Exception {
        board();
    }
}