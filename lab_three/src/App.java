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
import java.util.ArrayList;
import java.util.Arrays;
import java.awt.Image;

class Board {
    private int BOARD_SIZE;    
    private Piece[][] board;
    private Piece[][] nextBoard;
    private ArrayList<PieceGroup> groups;

    public Board(int boardSize) {
        BOARD_SIZE = boardSize;
        board = new Piece[BOARD_SIZE][BOARD_SIZE];
        nextBoard = new Piece[BOARD_SIZE][BOARD_SIZE];
        groups = new ArrayList<PieceGroup>();
    }

    public void placePiece(int x, int y, boolean blackTurn) {
        if (board[y][x] == null) {
            // System.arraycopy(board, 0, nextBoard, 0, BOARD_SIZE);
            int liberties = calcLiberties(x, y);
            System.out.println("libs = " + liberties);
            Piece potentialPiece = new Piece(liberties, blackTurn);
            board[y][x] = potentialPiece;
            PieceGroup group = findGroup(x, y, potentialPiece);

            for (PieceGroup g : groups) {
                if (g == group) return;
            }

            groups.add(group);
            System.out.println(groups);
        }
    }

    public int calcLiberties(int x, int y) {
        int liberties = 0;

        //Is this legal syntax? It's pretty funky
        if (y+1 < BOARD_SIZE) if (board[y+1][x] == null) liberties++;
        if (x+1 < BOARD_SIZE) if (board[y][x+1] == null) liberties++;
        if (y-1 >= 0) if (board[y-1][x] == null) liberties++;
        if (x-1 >= 0) if (board[y][x-1] == null) liberties++;

        return liberties;
    }

    public PieceGroup findGroup(int x, int y, Piece ungroupedPiece) {
        Piece piece;
        PieceGroup returnGroup = null;
        PieceGroup removalGroup = null;

        if (y+1 < BOARD_SIZE) if (board[y+1][x] != null) {
            piece = board[y+1][x];
            board[y+1][x] = piece.subtractLiberty();

            if (ungroupedPiece.isBlack() == piece.isBlack()) {
                for (PieceGroup group : groups) {
                    if (group.isPieceInGroup(piece)) {
                        removalGroup = group;
                        returnGroup = group.addPiece(ungroupedPiece);
                    }
                }
                groups.remove(removalGroup);
            }

        }

        if (x+1 < BOARD_SIZE) if (board[y][x+1] != null) {
            piece = board[y][x+1];
            board[y][x+1] = piece.subtractLiberty();

            if (ungroupedPiece.isBlack() == piece.isBlack()) {
                for (PieceGroup group : groups) {
                    if (group.isPieceInGroup(piece)) {
                        removalGroup = group;
                        if (returnGroup == null) {
                            returnGroup = group.addPiece(ungroupedPiece);                        
                        } else {
                            if (group.isPieceInGroup(ungroupedPiece)) {
                                returnGroup = group.subtractLiberty();
                            } else {
                                returnGroup = returnGroup.addGroup(group);
                            }
                        }
                    }
                }
                groups.remove(removalGroup);  
            } 
        }
        

        if (y-1 >= 0) if (board[y-1][x] != null) {
            piece = board[y-1][x];
            board[y-1][x] = piece.subtractLiberty();

            if (ungroupedPiece.isBlack() == piece.isBlack()) {
                for (PieceGroup group : groups) {
                    if (group.isPieceInGroup(piece)) {
                        removalGroup = group;
                        if (returnGroup == null) {
                            returnGroup = group.addPiece(ungroupedPiece);                      
                        } else {
                            if (group.isPieceInGroup(ungroupedPiece)) {
                                returnGroup = group.subtractLiberty();
                            } else {
                                returnGroup = returnGroup.addGroup(group);
                            }
                        }
                    }
                }  
                groups.remove(removalGroup); 
            }
        }

        if (x-1 >= 0) if (board[y][x-1] != null) {
            piece = board[y][x-1];
            board[y][x-1] = piece.subtractLiberty();

            if (ungroupedPiece.isBlack() == piece.isBlack()) {
                for (PieceGroup group : groups) {
                    if (group.isPieceInGroup(piece)) {
                        removalGroup = group;
                        if (returnGroup == null) {
                            returnGroup = group.addPiece(ungroupedPiece);                
                        } else {
                            if (group.isPieceInGroup(ungroupedPiece)) {
                                returnGroup = group.subtractLiberty();
                            } else {
                                returnGroup = returnGroup.addGroup(group);
                            }
                        }
                    }
                }   
                groups.remove(removalGroup);
            }
        }

        if (returnGroup == null) {
            returnGroup = new PieceGroup(new ArrayList<Piece>(Arrays.asList(ungroupedPiece)), ungroupedPiece.returnLiberties());
        }

        return returnGroup;
    }

    @Override
    public String toString() {
        String boardString = new String();

        for(int i = 0; i < BOARD_SIZE; i++) {
            for(int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == null) {
                    boardString += "0";
                } else {
                    boardString += board[i][j].returnLiberties();
                }
            }
            boardString += "\n";
        }

        return boardString;
    }
}

class Piece {
    private int liberties;
    private boolean black;

    public Piece(int libs, boolean isBlack) {
        liberties = libs;
        black = isBlack;
    }

    public int returnLiberties() {
        return liberties;
    }
    public boolean isBlack() {
        return black;
    }
    
    public Piece subtractLiberty() {
        return new Piece(liberties - 1, black);
    }

}

class PieceGroup {
    private ArrayList<Piece> pieces;
    private int liberties;

    public PieceGroup(ArrayList<Piece> piecesList, int libs) {
        pieces = piecesList;
        liberties = libs;
    }

    public boolean isPieceInGroup(Piece piece) {
        for (Piece element : pieces) {
            if (element == piece) return true;
        }
        return false;
    }

    public ArrayList<Piece> returnPieces() {
        return pieces;
    }

    public int returnLiberties() {
        return liberties;
    }

    public PieceGroup subtractLiberty() {
        return new PieceGroup(pieces, liberties - 1);
    }

    public PieceGroup addPiece(Piece piece) {
        pieces.add(piece);
        liberties += piece.returnLiberties() - 1;
        return new PieceGroup(pieces, liberties);
    }

    public PieceGroup addGroup(PieceGroup group) {
        ArrayList<Piece> newPieces = pieces;
        newPieces.addAll(group.returnPieces());
        return new PieceGroup(newPieces, liberties + group.returnLiberties() - 1);

    }

    @Override
    public String toString() {
        String printGroups = new String();

        for (Piece piece : pieces) {
            printGroups += (" " +  piece.returnLiberties());
        }
        printGroups += "\n" + liberties + "\n";
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
                            System.out.println(newBoard);
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

