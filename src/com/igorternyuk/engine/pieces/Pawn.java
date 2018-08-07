package com.igorternyuk.engine.pieces;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.igorternyuk.engine.Alliance;
import com.igorternyuk.engine.board.Board;
import com.igorternyuk.engine.board.BoardUtils;
import com.igorternyuk.engine.board.Location;
import com.igorternyuk.engine.board.Tile;
import com.igorternyuk.engine.moves.Move;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Created by igor on 01.12.17.
 */

public class Pawn extends Piece {
    public static final int[] DX = { -1, 0, 1};
    private static final Map<Location, Pawn> WHITE_ALREADY_MOVED_PAWNS = createAllPossibleWhitePawns(false);
    private static final Map<Location, Pawn> WHITE_NOT_MOVED_PAWNS = createAllPossibleWhitePawns(true);
    private static final Map<Location, Pawn> BLACK_ALREADY_MOVED_PAWNS = createAllPossibleBlackPawns(false);
    private static final Map<Location, Pawn> BLACK_NOT_MOVED_PAWNS = createAllPossibleBlackPawns(true);

    public static Pawn createPawn(final Location location, final Alliance alliance, final boolean isFirstMove) {
        if(isFirstMove) {
            return alliance.equals(Alliance.WHITE) ? WHITE_NOT_MOVED_PAWNS.get(location) :
                    BLACK_NOT_MOVED_PAWNS.get(location);
        } else {
            return alliance.equals(Alliance.WHITE) ? WHITE_ALREADY_MOVED_PAWNS.get(location) :
                    BLACK_ALREADY_MOVED_PAWNS.get(location);
        }
    }

    public static Pawn createPawn(final int x, final int y, final Alliance alliance, final boolean isFirstMove){
        return createPawn(BoardUtils.getPosition(x,y), alliance, isFirstMove);
    }

    public static Pawn createPawn(final char file, final int rank, final Alliance alliance,
                                      final boolean isFirstMove){
        return createPawn(BoardUtils.getPosition(file,rank), alliance, isFirstMove);
    }

    public static Pawn createPawn(final String algebraicNotationForPosition, final Alliance alliance,
                                      final boolean isFirstMove){
        return createPawn(BoardUtils.getPosition(algebraicNotationForPosition), alliance, isFirstMove);
    }

    private Pawn(final int x, final int y, final Alliance alliance, final boolean isFirstMove) {
        this(BoardUtils.getPosition(x,y), alliance, isFirstMove);
    }

    private Pawn(final Location pieceLocation, final Alliance pieceAlliance, final boolean isFirstMove) {
        super(PieceType.PAWN, pieceLocation, pieceAlliance, isFirstMove);
    }

    @Override
    public void setPossibleOffsets() {
        this.moveOffsets.add(new Point(-1, this.getAlliance().getDirectionY()));
        this.moveOffsets.add(new Point(0, this.getAlliance().getDirectionY()));
        this.moveOffsets.add(new Point(1, this.getAlliance().getDirectionY()));
    }

    @Override
    public Collection<Move> getLegalMoves(final Board board) {
        List<Move> legalMoves = new ArrayList<>();
        for (int i = 0; i < DX.length; ++i){
            final int destX = this.location.getX() + DX[i];
            final int destY = this.location.getY() + this.getAlliance().getDirectionY();

            if(!BoardUtils.isValidPosition(destX, destY)){
                continue;
            }

            final Location candidateDestination = BoardUtils.getPosition(destX, destY);
            if (this.moveOffsets.get(i).x == 0) {
                if(!board.getTile(candidateDestination).isOccupied()) {
                    //Regular move
                    if(this.getAlliance().isPawnPromotionSquare(candidateDestination)){
                        addAllPossiblePawnPromotions(board, candidateDestination, legalMoves);
                    } else {
                        legalMoves.add(new Move.PawnMove(board, this, candidateDestination));
                    }

                    if(this.isFirstMove){
                        //Pawn jump
                        final int jumpDestY = candidateDestination.getY() + this.getAlliance().getDirectionY();
                        if(!board.getTile(destX, jumpDestY).isOccupied()){
                            legalMoves.add(new Move.PawnJump(board, this, BoardUtils.getPosition(destX, jumpDestY)));
                        }
                    }
                }
            } else {
                // Diagonal capture
                final Tile destinationTile = board.getTile(candidateDestination);
                if(destinationTile.isOccupied()){
                    final Piece capturedPiece = destinationTile.getPiece();
                    if(capturedPiece.getAlliance() != this.alliance){
                        // Pawn promotion by capturing
                        if(this.getAlliance().isPawnPromotionSquare(candidateDestination)){
                            addAllPossiblePawnPromotions(board, candidateDestination, legalMoves);
                        } else {
                            legalMoves.add(new Move.PawnCapturingMove(board, this, candidateDestination,
                                    capturedPiece));
                        }
                    }
                } else {
                    //En passant capture
                    final Pawn enPassantPawn = board.getEnPassantPawn();
                    if(enPassantPawn != null && !enPassantPawn.getAlliance().equals(this.alliance) &&
                       enPassantPawn.getAlliance().getDirectionY() == this.alliance.getOppositeDirectionY() &&
                            enPassantPawn.getLocation().getY() == this.location.getY()) {
                        if (enPassantPawn.getLocation().getX() - this.location.getX() == this.moveOffsets.get(i).x) {
                            legalMoves.add(new Move.PawnEnPassantCapture(board, this, candidateDestination,
                                    enPassantPawn));
                        }
                    }
                }
            }
        }
        return ImmutableList.copyOf(legalMoves);
    }

    private void addAllPossiblePawnPromotions(final Board board, final Location candidateDestination,
                                              final List<Move> legalMoves){
        legalMoves.add(new Move.PawnPromotion(new Move.PawnMove(board,
                this, candidateDestination), Queen.createQueen(candidateDestination,
                this.alliance, false)));
        legalMoves.add(new Move.PawnPromotion(new Move.PawnMove(board,
                this, candidateDestination), Rook.createRook(candidateDestination,
                this.alliance, false)));
        legalMoves.add(new Move.PawnPromotion(new Move.PawnMove(board,
                this, candidateDestination), Knight.createKnight(candidateDestination,
                this.alliance, false)));
        legalMoves.add(new Move.PawnPromotion(new Move.PawnMove(board,
                this, candidateDestination), Bishop.createBishop(candidateDestination,
                this.alliance, false)));
    }

    @Override
    public Pawn move(final Move move) {
        if(move.getMovedPiece().getAlliance().equals(Alliance.WHITE)) {
            return WHITE_ALREADY_MOVED_PAWNS.get(move.getDestination());
        } else {
            return BLACK_ALREADY_MOVED_PAWNS.get(move.getDestination());
        }
    }

    private static final Map<Location, Pawn> createAllPossibleWhitePawns(final boolean isFirstMove) {
        return createAllPossiblePawns(Alliance.WHITE, isFirstMove);
    }

    private static final Map<Location, Pawn> createAllPossibleBlackPawns(final boolean isFirstMove) {
        return createAllPossiblePawns(Alliance.BLACK, isFirstMove);
    }

    private static final Map<Location, Pawn> createAllPossiblePawns(final Alliance alliance,
                                                                    final boolean isFirstMove){
        Map<Location, Pawn> pawns = new HashMap<>();
        if(isFirstMove){
            final int startRank = alliance.equals(Alliance.WHITE) ? BoardUtils.SECOND_RANK : BoardUtils.SEVENTH_RANK;
            for(int x = 0; x < BoardUtils.BOARD_SIZE; ++x){
                final Location currentLocation = BoardUtils.getPosition(x, startRank);
                pawns.put(currentLocation, new Pawn(currentLocation, alliance, true));
            }
        } else {
            final int from = alliance.equals(Alliance.WHITE) ? BoardUtils.EIGHTH_RANK : BoardUtils.SIXTH_RANK;
            final int to = alliance.equals(Alliance.WHITE) ? BoardUtils.THIRD_RANK : BoardUtils.FIRST_RANK;
            for (int y = from; y <= to; ++y) {
                for (int x = 0; x < BoardUtils.BOARD_SIZE; ++x) {
                    final Location currentLocation = BoardUtils.getPosition(x, y);
                    pawns.put(currentLocation, new Pawn(currentLocation, alliance, false));
                }
            }
        }
        return ImmutableMap.copyOf(pawns);
    }

    @Override
    public String toString() {
        return String.valueOf(BoardUtils.getAlgebraicNotationForCoordinateX(this.location.getX()));
    }
}
