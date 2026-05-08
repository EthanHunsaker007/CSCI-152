import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.util.HashMap;
import java.util.HashSet;
import javax.swing.*;

public class Go {
    private final int boardSize;
    private int gap;
    private int sideMargins;
    private int topBottomMargins;
    private int pieceSize;
    private int turnIndicatorSize;
    private boolean blackTurn = true;
    private boolean passed = false;
    private boolean gameOver = false;
    private int blackScore = 0;
    private int whiteScore = 0;
    private boolean uiHorizontal = true;
    private final Piece[][] board;
    private final HashMap<Piece, PieceGroup> groupMap = new HashMap<>();
    private final static int[][] dirs = { { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 } };

    public Go(int size) {
        this.boardSize = size;
        this.board = new Piece[size][size];
    }

    public void showUI(int width, int height) {
        JFrame frame = new JFrame("Go");
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.add(new Component() {
            @Override
            public void paint(Graphics g) {
                // Draw Board Lines
                g.setColor(Color.BLACK);
                for (int i = 0; i < boardSize; i++) {
                    g.drawLine(sideMargins, topBottomMargins+i*gap, sideMargins+(boardSize-1)*gap, topBottomMargins+i*gap);
                    g.drawLine(sideMargins+i*gap, topBottomMargins, sideMargins+i*gap, topBottomMargins+(boardSize-1) * gap);
                }

                // Draw Stones
                for (int x = 0; x < boardSize; x++) {
                    for (int y = 0; y < boardSize; y++) {
                        if (board[x][y] != null) {
                            g.setColor(board[x][y].black() == true ? Color.BLACK : Color.WHITE);
                            g.fillOval(sideMargins + x * gap - pieceSize / 2, topBottomMargins + y * gap - pieceSize / 2, pieceSize, pieceSize);
                            g.setColor(Color.BLACK);
                            g.drawOval(sideMargins + x * gap - pieceSize / 2, topBottomMargins + y * gap - pieceSize / 2, pieceSize, pieceSize);
                        }
                    }
                }

                // Draw Turn Indicator and Scores

                g.setColor(blackTurn ? Color.BLACK : Color.WHITE);
                Graphics2D g2 = (Graphics2D) g;

                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                String white = String.valueOf(whiteScore);
                String black = String.valueOf(blackScore);
                Font font = new Font("Arial", Font.BOLD, turnIndicatorSize);
                FontRenderContext frc = g2.getFontRenderContext();
                GlyphVector whiteVector = font.createGlyphVector(frc, white);
                GlyphVector blackVector = font.createGlyphVector(frc, black);
                Shape blackShape;
                Shape whiteShape;

                if (uiHorizontal) {
                    g.fillOval(sideMargins / 2 - turnIndicatorSize / 2, topBottomMargins + (boardSize - 1) / 2 * gap - turnIndicatorSize / 2, turnIndicatorSize, turnIndicatorSize);
                    g.fillOval(sideMargins + sideMargins / 2 + (boardSize-1) * gap - turnIndicatorSize / 2, topBottomMargins + (boardSize - 1) / 2 * gap - turnIndicatorSize / 2, turnIndicatorSize, turnIndicatorSize);
                    g.setColor(Color.BLACK);
                    g.drawOval(sideMargins / 2 - turnIndicatorSize / 2, topBottomMargins + (boardSize - 1) / 2 * gap - turnIndicatorSize / 2, turnIndicatorSize, turnIndicatorSize);
                    g.drawOval(sideMargins + sideMargins / 2 + (boardSize-1) * gap - turnIndicatorSize / 2, topBottomMargins + (boardSize - 1) / 2 * gap - turnIndicatorSize / 2, turnIndicatorSize, turnIndicatorSize);

                    whiteShape = whiteVector.getOutline(sideMargins / 2 - (int)whiteVector.getVisualBounds().getWidth() / 2,  topBottomMargins + (boardSize - 1) / 2 * gap + turnIndicatorSize + turnIndicatorSize / 2);
                    blackShape = blackVector.getOutline(sideMargins + sideMargins / 2 + (boardSize-1) * gap - (int)blackVector.getVisualBounds().getWidth() / 2, topBottomMargins + (boardSize - 1) / 2 * gap + turnIndicatorSize + turnIndicatorSize / 2);
                } else {
                    g.fillOval(sideMargins + (boardSize - 1) / 2 * gap - turnIndicatorSize / 2, topBottomMargins / 2 - turnIndicatorSize / 2, turnIndicatorSize, turnIndicatorSize);
                    g.setColor(Color.BLACK);
                    g.drawOval(sideMargins + (boardSize - 1) / 2 * gap - turnIndicatorSize / 2, topBottomMargins / 2 - turnIndicatorSize / 2, turnIndicatorSize, turnIndicatorSize);

                    whiteShape = whiteVector.getOutline(sideMargins + (boardSize - 1) / 2 * gap - (int)whiteVector.getVisualBounds().getWidth() * 3, topBottomMargins / 2 + (int)whiteVector.getVisualBounds().getHeight() / 2);
                    blackShape = blackVector.getOutline(sideMargins + (boardSize - 1) / 2 * gap + (int)blackVector.getVisualBounds().getWidth() * 2, topBottomMargins / 2 + (int)blackVector.getVisualBounds().getHeight() / 2);
                }
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(3f));
                g2.draw(whiteShape);
                g2.draw(blackShape);
                g2.setColor(Color.WHITE);
                g2.fill(whiteShape);
                g2.setColor(Color.BLACK);
                g2.fill(blackShape);

                if (gameOver) {
                    Font endFont = new Font("Times New Roman", Font.BOLD, gap * 2);
                    String gameEnd = "Game Over";
                    GlyphVector endVector = endFont.createGlyphVector(frc, gameEnd);
                    Shape endShape = endVector.getOutline(sideMargins + (boardSize - 1) / 2  * gap - (int)endVector.getVisualBounds().getWidth() / 2, topBottomMargins + (boardSize - 1) / 2 * gap + (int)endVector.getVisualBounds().getHeight() / 2);
                    g2.setColor(Color.BLACK);
                    g2.setStroke(new BasicStroke(6f));
                    g2.draw(endShape);
                    g2.setColor(Color.WHITE);
                    g2.fill(endShape);
                }
            }
        });

        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Dimension frameSize = frame.getContentPane().getSize();

                int shortSide;

                if (frameSize.height > frameSize.width) {
                    uiHorizontal = false;
                    shortSide = frameSize.width;
                } else {    
                    uiHorizontal = true;
                    shortSide = frameSize.height;
                }

                int pixelBoardSize = (int) Math.round(shortSide * 0.8);
                sideMargins = (frameSize.width - pixelBoardSize) / 2;
                topBottomMargins = (frameSize.height - pixelBoardSize) / 2;
                gap = pixelBoardSize / (boardSize - 1);
                pieceSize = gap / 2;
                turnIndicatorSize = uiHorizontal ? sideMargins / 3 : topBottomMargins / 3;
            }
        });

        // Interaction logic
        frame.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (gameOver) return;

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

        JButton passButton = new JButton("Pass");
            passButton.addActionListener(e -> {
                if (gameOver) return;
                if (passed == true) {
                    gameOver = true;
                }
                blackTurn = !blackTurn;
                passed = true;
                frame.repaint();
            });

        frame.add(passButton, java.awt.BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    public boolean clicked(int x, int y) {
        if (board[y][x] == null) {
            Piece newPiece = new Piece(blackTurn, new PiecePosition(x, y));
            board[y][x] = newPiece;
            HashSet<PieceGroup> neighborGroups = pieceNeighborGroups(newPiece);
            HashSet<PiecePosition> liberties = pieceLiberties(newPiece);

            PieceGroup newGroup = new PieceGroup(newPiece, liberties);
            groupMap.put(newPiece, newGroup);

            for (PieceGroup ng : neighborGroups) {
                ng.subtractLiberty(newPiece.position());
            }

            if (captureAndSuicideCheck(newGroup, neighborGroups)) {
                clearGroup(newGroup);
                board[y][x] = null;
                return false;
            }

            collapseGroups(newGroup, neighborGroups);
            passed = false;
            return true;
        }
        return false;
    }

    private void collapseGroups(PieceGroup mainGroup, HashSet<PieceGroup> neighborGroups) {
        for (PieceGroup ng : neighborGroups) {
            if (mainGroup.black() != ng.black()) {
                continue;
            }

            mainGroup.addGroup(ng);

            for (Piece p : ng.pieces()) {
                groupMap.remove(p);
                groupMap.put(p, mainGroup);
            }
        }
    }

    private boolean captureAndSuicideCheck(PieceGroup capturingGroup, HashSet<PieceGroup> neighborGroups) {
        for (PieceGroup ng : neighborGroups) {
            if (capturingGroup != ng && ng != null && ng.liberties().isEmpty()
                    && ng.black() != capturingGroup.black()) {
                int score = clearGroup(ng);
                if (blackTurn) {
                    blackScore += score;
                } else {
                    whiteScore += score;
                }
            }
        }

        boolean suicide = capturingGroup.liberties().isEmpty();

        for (PieceGroup ng : neighborGroups) {
            if (ng.black() == capturingGroup.black() && !ng.liberties().isEmpty()) {
                suicide = false;
            }
        }

        return suicide;
    }

    private int clearGroup(PieceGroup group) {
        int groupSize = group.pieces().size();
        for (Piece p : group.pieces()) {
            for (PieceGroup ng : pieceNeighborGroups(p)) {
                if (ng.black() != p.black()) {
                    ng.addLiberty(p.position());
                }
            }
            groupMap.remove(p);
            board[p.position().y()][p.position().x()] = null;
        }
        return groupSize;
    }

    private HashSet<PiecePosition> pieceLiberties(Piece piece) {
        HashSet<PiecePosition> returnSet = new HashSet<>();
        PiecePosition pos = piece.position();

        for (int[] dir : dirs) {
            int nx = pos.x() + dir[0];
            int ny = pos.y() + dir[1];

            if (nx < 0 || ny < 0 || nx >= boardSize || ny >= boardSize)
                continue;
            if (board[ny][nx] != null)
                continue;

            returnSet.add(new PiecePosition(nx, ny));
        }
        return returnSet;
    }

    private HashSet<PieceGroup> pieceNeighborGroups(Piece piece) {
        HashSet<PieceGroup> returnGroups = new HashSet<>();
        PiecePosition pos = piece.position();

        for (int[] dir : dirs) {
            int nx = pos.x() + dir[0];
            int ny = pos.y() + dir[1];

            if (nx < 0 || ny < 0 || nx >= boardSize || ny >= boardSize)
                continue;
            if (board[ny][nx] == null)
                continue;

            returnGroups.add(groupMap.get(board[ny][nx]));
        }
        return returnGroups;
    }
}