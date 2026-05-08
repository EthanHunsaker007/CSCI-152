import java.util.HashSet;

public class PieceGroup {
    private final boolean black;
    private final HashSet<Piece> pieces;
    private final HashSet<PiecePosition> liberties;



    public PieceGroup(Piece piece, HashSet<PiecePosition> libs) {
        black = piece.black();
        pieces = new HashSet<>();
        pieces.add(piece);
        liberties = new HashSet<>();
        liberties.addAll(libs);
    }

    public PieceGroup(boolean is_black, HashSet<Piece> _pieces, HashSet<PiecePosition> libs) {
        black = is_black;
        pieces = new HashSet<>();
        pieces.addAll(_pieces);
        liberties = new HashSet<>();
        liberties.addAll(libs);
    }

    public void addPiece(Piece piece, HashSet<PiecePosition> libs) {
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

    public void addLiberty(PiecePosition liberty) {
        liberties.add(liberty);
    }

    public void subtractLiberty(PiecePosition liberty) {
        liberties.remove(liberty);
    }

    public boolean black() {
        return black;
    }

    public HashSet<Piece> pieces() {
        return pieces;
    }

    public HashSet<PiecePosition> liberties() {
        return liberties;
    }

    public PieceGroup copy() {
        return new PieceGroup(black, pieces, liberties);
    }
}