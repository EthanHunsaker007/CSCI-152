import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;
import javax.swing.*;

record Position (int x, int y) {}
record Piece(boolean black, Position position) {}

public class Board {
    private final int BOARD_SIZE;
    private int gap = 30;
    private int sideMargins = 10;
    private int topBottomMargins = 10;
    private int pieceSize = 15;
    private boolean blackTurn = true;
    private final Piece[][] board;
    private final HashSet<PieceGroup> groups = new HashSet<>();
    private final HashMap<Piece, PieceGroup> groupMap = new HashMap<>();
    private final Queue<PieceGroup> suicideGroups = new ArrayDeque<>();
    private final static int[][] dirs = {{0,1}, {1,0}, {0,-1}, {-1,0}};

    
    public Board(int size) {
        BOARD_SIZE = size;
        board = new Piece[size][size];
    }

    public void showUI(int width, int height) {
        JFrame frame = new JFrame("Go");
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //OPTIONAL: frame.getContentPane().setBackground(<You can Change color here>);

        frame.add(new Component() {
            @Override
            public void paint(Graphics g) {
                // Draw Board Lines
                g.setColor(Color.BLACK);
                for (int i = 0; i < BOARD_SIZE; i++) {
                    g.drawLine(sideMargins, topBottomMargins+i*gap, sideMargins+(BOARD_SIZE-1)*gap, topBottomMargins+i*gap);
                    g.drawLine(sideMargins+i*gap, topBottomMargins, sideMargins+i*gap, topBottomMargins+(BOARD_SIZE-1) * gap);
                }

                // Draw Stones
                for (int x = 0; x < BOARD_SIZE; x++) {
                    for (int y = 0; y < BOARD_SIZE; y++) {
                        if (board[x][y] != null) {
                            g.setColor(board[x][y].black() == true ? Color.BLACK : Color.WHITE);
                            g.fillOval(sideMargins + x * gap - pieceSize / 2, topBottomMargins + y * gap - pieceSize / 2, pieceSize, pieceSize);
                            g.setColor(Color.BLACK);
                            g.drawOval(sideMargins + x * gap - pieceSize / 2, topBottomMargins + y * gap - pieceSize / 2, pieceSize, pieceSize);
                        }
                    }
                }
            }
        });

        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Dimension frameSize = frame.getContentPane().getSize();

                int shortSide = (frameSize.height > frameSize.width) ? frameSize.width : frameSize.height;

                int boardSize = (int) Math.round(shortSide * 0.8);
                sideMargins = (frameSize.width - boardSize) / 2;
                topBottomMargins = (frameSize.height - boardSize) / 2;
                gap = boardSize / (BOARD_SIZE - 1);
                pieceSize = gap / 2;
            }
        });

        // Interaction logic
        frame.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Adjust for window header which are called insets
                int y = Math.round((float) (e.getX() - sideMargins) / gap);
                int x = Math.round((float) (e.getY() - topBottomMargins - frame.getInsets().top) / gap);
                if (x >= 0 && x < board.length && y >= 0 && y < board.length) {
                    if (clicked(x, y)) {
                        blackTurn = !blackTurn;
                        frame.repaint();
                    }
                }
            }
        });
        
        frame.setVisible(true);
    }
    

    public boolean clicked(int x, int y) {
        if (board[y][x] == null) {
            Piece newPiece = new Piece(blackTurn, new Position(x, y));
            HashSet<Piece> neighbors = pieceNeighbors(newPiece);

            PieceGroup newPieceGroup = groupPiece(newPiece, neighbors);
            
            if (captureCheck(newPieceGroup, neighbors) == true) {
                suicide(newPiece, neighbors);
                System.out.println("Suicide: " + groups.size() + " groups");
                return false;
            }

            board[y][x] = newPiece;
            System.out.println("Placement: " + groups.size() + " groups");

            return true;
        }
        System.out.println("Existing Piece: " + groups.size() + " groups");
        return false;
    }

    private void suicide(Piece piece, HashSet<Piece> neighbors) {
        System.out.println(suicideGroups.size());
        PieceGroup firstGroup = suicideGroups.poll();

        for (Piece n : neighbors) {
            if (n.black() == piece.black()) continue;
            PieceGroup neighborGroup = groupMap.get(n);
            neighborGroup.addLiberty(piece.position());
        }

        groupMap.remove(piece);

        if (firstGroup.pieces().size() == 1) {
            groups.remove(firstGroup);
        } else {
            firstGroup.addLiberty(piece.position());
        }

        while (true) { 
            PieceGroup nextGroup = suicideGroups.poll();
            if (nextGroup == null) return;

            for (Piece p : nextGroup.pieces()) {
                firstGroup.removePiece(p);
                groupMap.remove(p);
                groupMap.put(p, nextGroup);
            }

            groups.add(nextGroup);
        }
    }

    private PieceGroup groupPiece(Piece piece, HashSet<Piece> neighbors) {
        HashSet<Position> liberties = pieceLiberties(piece); 
        suicideGroups.clear();

        PieceGroup grouped = null;
        for (Piece n : neighbors) {
            PieceGroup neighborGroup = groupMap.get(n);
            
            if (n.black() != piece.black()) {
                neighborGroup.subtractLiberty(piece.position());
                continue;
            }

            if (grouped == null) {
                neighborGroup.addPiece(piece, liberties);
                grouped = neighborGroup;
                groupMap.put(piece, grouped);
                suicideGroups.add(neighborGroup);
            } else if(grouped != neighborGroup) {
                grouped.addGroup(neighborGroup);

                for (Piece p : neighborGroup.pieces()) {
                    groupMap.remove(p);
                    groupMap.put(p, grouped);
                }

                grouped.subtractLiberty(piece.position());
                groups.remove(neighborGroup);
                suicideGroups.add(neighborGroup);
            }
        }

        if (grouped == null) {
            grouped = new PieceGroup(piece, liberties);
            groups.add(grouped);
            groupMap.put(piece, grouped);
            suicideGroups.add(grouped);
        }
        
        return grouped;
    }
    
    private boolean captureCheck(PieceGroup capturingGroup, HashSet<Piece> neighbors) {
        boolean suicide = false;
        for (Piece n : neighbors) {
            PieceGroup neighborGroup = groupMap.get(n);
            if (capturingGroup != neighborGroup && neighborGroup != null && neighborGroup.liberties().isEmpty()) {
                clearGroup(neighborGroup);
            }
        }

        System.out.println(capturingGroup.liberties().size());
        if (capturingGroup.liberties().isEmpty()) suicide = true;
        return suicide;
    }

    private void clearGroup(PieceGroup group) {
        for (Piece p: group.pieces()) {
            for (Piece n: pieceNeighbors(p)) {
                if (n.black() != p.black()) {
                    groupMap.get(n).addLiberty(p.position());
                }
            }
            groupMap.remove(p);
            board[p.position().y()][p.position().x()] = null;
        }
        groups.remove(group);
    }

    private HashSet<Position> pieceLiberties(Piece piece) {
        HashSet<Position> returnSet = new HashSet<>();
        Position pos = piece.position();
        
        for (int[] dir : dirs) {
            int nx = pos.x() + dir[0];
            int ny = pos.y() + dir[1];

            if (nx < 0 || ny < 0 || nx >= BOARD_SIZE || ny >= BOARD_SIZE) continue;
            if (board[ny][nx] != null) continue;

            returnSet.add(new Position(nx, ny));
        }
        return returnSet;        
    }

    private HashSet<Piece> pieceNeighbors(Piece piece) {
        HashSet<Piece> returnSet = new HashSet<>();
        Position pos = piece.position();
        
        for (int[] dir : dirs) {
            int nx = pos.x() + dir[0];
            int ny = pos.y() + dir[1];

            if (nx < 0 || ny < 0 || nx >= BOARD_SIZE || ny >= BOARD_SIZE) continue;
            if (board[ny][nx] == null) continue;

            returnSet.add(board[ny][nx]);
        }
        return returnSet;
    }
}

class PieceGroup {
    private final boolean black;
    private final HashSet<Piece> pieces;
    private final HashSet<Position> liberties;

    public PieceGroup(Piece piece, HashSet<Position> libs) {
        black = piece.black();
        pieces = new HashSet<>();
        pieces.add(piece);
        liberties = new HashSet<>();
        liberties.addAll(libs);
    }

    public void addPiece(Piece piece, HashSet<Position> libs) {
        pieces.add(piece);
        liberties.addAll(libs);
        liberties.remove(piece.position());        
    }

    public void removePiece(Piece piece) {
        pieces.remove(piece);
    }

    public void addGroup(PieceGroup group) {
        pieces.addAll(group.pieces);
        liberties.addAll(group.liberties);
    }

    public void addLiberty(Position liberty) {
        liberties.add(liberty);
    }

    public void subtractLiberty(Position liberty) {
        liberties.remove(liberty);
    }

    public boolean black() {
        return black;
    }

    public HashSet<Piece> pieces() {
        return pieces;
    }

    public HashSet<Position> liberties() {
        return liberties;
    }
}