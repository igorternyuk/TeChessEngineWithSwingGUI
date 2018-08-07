package com.igorternyuk.engine.pieces;

import com.google.common.collect.ImmutableMap;
import com.igorternyuk.engine.Alliance;
import com.igorternyuk.engine.board.Board;
import com.igorternyuk.engine.board.BoardUtils;
import com.igorternyuk.engine.board.Location;
import com.igorternyuk.engine.moves.Move;

import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by igor on 01.12.17.
 */

public class Queen extends Piece {

    private static final Map<Location, Queen> WHITE_ALREADY_MOVED_QUEENS = createAllPossibleWhiteQueens(false);
    private static final Map<Location, Queen> WHITE_NOT_MOVED_QUEENS = createAllPossibleWhiteQueens(true);
    private static final Map<Location, Queen> BLACK_ALREADY_MOVED_QUEENS = createAllPossibleBlackQueens(false);
    private static final Map<Location, Queen> BLACK_NOT_MOVED_QUEENS = createAllPossibleBlackQueens(true);

    public static Queen createQueen(final Location location, final Alliance alliance, final boolean isFirstMove) {
        if(isFirstMove) {
            return alliance.equals(Alliance.WHITE) ? WHITE_NOT_MOVED_QUEENS.get(location) :
                    BLACK_NOT_MOVED_QUEENS.get(location);
        } else {
            return alliance.equals(Alliance.WHITE) ? WHITE_ALREADY_MOVED_QUEENS.get(location) :
                    BLACK_ALREADY_MOVED_QUEENS.get(location);
        }
    }

    public static Queen createQueen(final int x, final int y, final Alliance alliance, final boolean isFirstMove){
        return createQueen(BoardUtils.getPosition(x, y), alliance, isFirstMove);
    }

    public static Queen createQueen(final char file, final int rank, final Alliance alliance,
                                  final boolean isFirstMove){
        return createQueen(BoardUtils.getPosition(file,rank), alliance, isFirstMove);
    }

    public static Queen createQueen(final String algebraicNotationForPosition, final Alliance alliance,
                                  final boolean isFirstMove){
        return createQueen(BoardUtils.getPosition(algebraicNotationForPosition), alliance, isFirstMove);
    }

    private Queen(final int x, final int y, final Alliance alliance, final boolean isFirstMove)
    {
        this(BoardUtils.getPosition(x,y), alliance, isFirstMove);
    }

    private Queen(final Location pieceLocation, final Alliance alliance, final boolean isFirstMove) {
        super(PieceType.QUEEN, pieceLocation, alliance, isFirstMove);
    }

    @Override
    public void setPossibleOffsets() {
        this.moveOffsets.add(new Point(-1, -1));
        this.moveOffsets.add(new Point(-1, 1));
        this.moveOffsets.add(new Point(1, -1));
        this.moveOffsets.add(new Point(1, 1));
        this.moveOffsets.add(new Point(-1, 0));
        this.moveOffsets.add(new Point(1, 0));
        this.moveOffsets.add(new Point(0, -1));
        this.moveOffsets.add(new Point(0, 1));
    }

    @Override
    public Collection<Move> getLegalMoves(final Board board) {
        return getLinearlyMovingPiecesLegalMoves(board);
    }

    @Override
    public Queen move(final Move move) {
        if(move.getMovedPiece().getAlliance().equals(Alliance.WHITE)) {
            return WHITE_ALREADY_MOVED_QUEENS.get(move.getDestination());
        } else {
            return BLACK_ALREADY_MOVED_QUEENS.get(move.getDestination());
        }
    }

    private static final Map<Location, Queen> createAllPossibleWhiteQueens(final boolean isFirstMove) {
        return createAllPossibleQueens(Alliance.WHITE, isFirstMove);
    }

    private static final Map<Location, Queen> createAllPossibleBlackQueens(final boolean isFirstMove) {
        return createAllPossibleQueens(Alliance.BLACK, isFirstMove);
    }

    private static final Map<Location, Queen> createAllPossibleQueens(final Alliance alliance,
                                                                      final boolean isFirstMove){
        Map<Location, Queen> queens = new HashMap<>();
        if(isFirstMove){
            final int backRank = alliance.isWhite() ? BoardUtils.FIRST_RANK : BoardUtils.EIGHTH_RANK;
            for(int x = 0; x < BoardUtils.BOARD_SIZE; ++x){
                final Location currentLocation = BoardUtils.getPosition(x, backRank);
                queens.put(currentLocation, new Queen(currentLocation, alliance, true));
            }
        } else {
            for (int y = 0; y < BoardUtils.BOARD_SIZE; ++y) {
                for (int x = 0; x < BoardUtils.BOARD_SIZE; ++x) {
                    final Location currentLocation = BoardUtils.getPosition(x, y);
                    queens.put(currentLocation, new Queen(currentLocation, alliance, false));
                }
            }
        }
        return ImmutableMap.copyOf(queens);
    }

   /* @Override
    public String toString() {
        return PieceType.QUEEN.getName().toUpperCase() + Board.getChessNotationTileName(this.getLocation);
    }*/
}
