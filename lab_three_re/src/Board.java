
import java.util.HashMap;
import java.util.HashSet;

record Position (int x, int y) {}
record Piece(boolean black, Position position) {}

enum Stone { BLACK, WHITE, EMPTY }

public class Board {
    private final int BOARD_SIZE;
    private final Piece[][] board;
    private final HashSet<PieceGroup> groups;
    private final HashMap<Piece, PieceGroup> groupMap;
    private final int[][] dirs = {{0,1}, {1,0}, {0,-1}, {-1,0}};

    
    public Board(int size) {
        BOARD_SIZE = size;
        board = new Piece[size][size];
        groups = new HashSet<>();
        groupMap = new HashMap<>();
    }

    public boolean clicked(int x, int y, boolean turn) {
        if (board[y][x] == null) {
            Piece newPiece = new Piece(turn, new Position(x, y));
            HashSet<Piece> neighbors = pieceNeighbors(newPiece);

            PieceGroup newPieceGroup = groupPiece(newPiece, neighbors);

            captureCheck(newPieceGroup, neighbors);

            return true;
        }

        return false;
    }

    public Stone getPieceColor(Position pos) {
        Piece piece = board[pos.y()][pos.x()];
        if (piece == null) {
            return Stone.EMPTY;
        } else if (piece.black()) {
            return Stone.BLACK;
        } else {
            return Stone.WHITE;
        }
    }

    private PieceGroup groupPiece(Piece piece, HashSet<Piece> neighbors) {
        HashSet<Position> liberties = pieceLiberties(piece); 

        PieceGroup grouped = null;
        for (Piece p : neighbors) {
            PieceGroup neighborGroup = groupMap.get(p);
            
            if (p.black() == piece.black()) {
                if (grouped == null) {
                    neighborGroup.addPiece(piece, liberties);
                    grouped = neighborGroup;
                    groupMap.put(piece, grouped);
                } else if(grouped != neighborGroup) {
                    grouped.addGroup(neighborGroup);
                    grouped.subtractLiberty(piece.position());
                    groups.remove(neighborGroup);                        
                }
            } else {
                neighborGroup.subtractLiberty(piece.position());
            }
        }

        if (grouped == null) {
            grouped = new PieceGroup(piece, liberties);
            groups.add(grouped);
            groupMap.put(piece, grouped);
        }
        
        return grouped;
    }
    
    private void captureCheck(PieceGroup capturingGroup, HashSet<Piece> neighbors) {
        for (Piece p : neighbors) {
            if (p.black() != capturingGroup.black()) {
                PieceGroup neighborGroup = groupMap.get(p);
                
                if (neighborGroup.liberties().isEmpty()) {
                    clearGroup(neighborGroup);
                }
            }
        }
        if (capturingGroup.liberties().isEmpty()) {
            clearGroup(capturingGroup);
        }
    }

    private void clearGroup(PieceGroup group) {
        for (Piece p: group.pieces()) {
            for (Piece n: pieceNeighbors(p)) {
                if (n.black() != p.black()) {
                    groupMap.get(n).addLiberty(p.position());
                    board[p.position().y()][p.position().x()] = null;
                }
            }
        }
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