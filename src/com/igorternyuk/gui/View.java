package com.igorternyuk.gui;

import com.igorternyuk.engine.Alliance;
import com.igorternyuk.engine.Game;
import com.igorternyuk.engine.board.*;
import com.igorternyuk.engine.moves.Move;
import com.igorternyuk.engine.pieces.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

import static javax.swing.SwingUtilities.isLeftMouseButton;
import static javax.swing.SwingUtilities.isRightMouseButton;

/**
 * Created by igor on 28.02.18.
 */

public class View {
    private static final String TITLE_OF_MAIN_WINDOW = "TeChess";
    private static final int DX = 3;
    private static final int DY = 48;
    private static final int TILE_SIZE = 64;
    private static final int MAIN_WINDOW_WIDTH = TILE_SIZE * BoardUtils.BOARD_SIZE + TakenPiecesPanel.PANEL_WIDTH +
            GameHistoryPanel.PANEL_WIDTH + DX;
    private static final int MAIN_WINDOW_HEIGHT = TILE_SIZE * BoardUtils.BOARD_SIZE + DY;
    private static final Dimension BOARD_PANEL_DIMENSION = new Dimension(TILE_SIZE * BoardUtils.BOARD_SIZE,
            TILE_SIZE * BoardUtils.BOARD_SIZE);
    private static final int LEGAL_MOVE_HIGHLIGHT_CIRCLE_RADIUS = 5;
    private static final int LAST_MOVE_HIGHLIGHT_ARROW_HEIGHT = 30;
    private static final int LAST_MOVE_HIGHLIGHT_ARROW_ANGLE = 30;
    private static final int LAST_MOVE_HIGHLIGHT_ARROW_LINE_WIDTH = 5;
    final String[] PROMOTED_PIECE_OPTIONS = {"Rook", "Bishop", "Knight", "Queen"};
    private static final ResourceManager RESOURCE_MANAGER = ResourceManager.getInstance();
    private final Game game = new Game(GameType.CLASSIC_CHESS);
    private Board chessBoard;
    private Tile startTile;
    private Tile destinationTile;
    private Move lastMove;
    private Piece humanMovedPiece;
    private int humanMovedPieceX, humanMovedPieceY;
    private final JFrame mainWindow;
    private final BoardPanel boardPanel;
    private final TakenPiecesPanel takenPiecesPanel;
    private final GameHistoryPanel gameHistoryPanel;
    private BoardOrientation boardOrientation;
    private boolean highlightLegalMoves = false;
    private boolean highlightLastMove = false;
    private boolean isAutoQueenEnabled = false;

    public View(){
        this.chessBoard = this.game.getChessBoard();
        this.mainWindow = new JFrame(TITLE_OF_MAIN_WINDOW);
        this.mainWindow.setLayout(new BorderLayout());
        this.mainWindow.setSize(MAIN_WINDOW_WIDTH, MAIN_WINDOW_HEIGHT);
        this.mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.mainWindow.setLocationRelativeTo(null);
        this.mainWindow.setResizable(false);
        final JMenuBar tableMenuBar = createMenuBar();
        this.mainWindow.setJMenuBar(tableMenuBar);
        this.boardPanel = new BoardPanel();
        this.takenPiecesPanel = new TakenPiecesPanel();
        this.gameHistoryPanel = new GameHistoryPanel();
        this.boardOrientation = BoardOrientation.REGULAR;
        this.mainWindow.add(this.boardPanel, BorderLayout.CENTER);
        this.mainWindow.add(this.takenPiecesPanel, BorderLayout.WEST);
        this.mainWindow.add(this.gameHistoryPanel, BorderLayout.EAST);
        this.mainWindow.setVisible(true);
    }

    private JMenuBar createMenuBar(){
        final JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu());
        menuBar.add(createPreferencesMenu());
        return menuBar;
    }

    private JMenu createFileMenu(){
        final JMenu fileMenu = new JMenu("File");

        final JMenuItem newClassicChessGameMenuItem = new JMenuItem("New classic chess game");
        newClassicChessGameMenuItem.addActionListener(event->{
            this.prepareNewClassicChessGame();
        });

        fileMenu.add(newClassicChessGameMenuItem);

        final JMenuItem newRandomFisherChessGameMenuItem = new JMenuItem("New random Fisher chess game");
        newRandomFisherChessGameMenuItem.addActionListener(event->{
            this.prepareNewRandomFisherChessGame();
        });

        fileMenu.add(newRandomFisherChessGameMenuItem);

        final JMenuItem openPGN = new JMenuItem("Load pgn file");
        openPGN.addActionListener(event -> {
            System.out.println("Open up that pgn file!!!");
        });
        fileMenu.add(openPGN);

        final JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(event -> {
            int userAnswer = JOptionPane.showConfirmDialog(
                    null,
                    "Do you really want to exit?",
                    "Confirm exit, please",
                    JOptionPane.YES_NO_OPTION
            );
            if(userAnswer == JOptionPane.YES_OPTION){
                System.exit(0);
            }
        });

        fileMenu.add(exitMenuItem);
        return fileMenu;
    }

    private JMenu createPreferencesMenu(){
        final JMenu preferencesMenu = new JMenu("Preferences");
        final JMenuItem flipBoardMenuItem = new JMenuItem("Flip board");
        flipBoardMenuItem.addActionListener(event -> {
            this.boardOrientation = this.boardOrientation.getOpposite();
            this.boardPanel.redraw();
        });
        preferencesMenu.add(flipBoardMenuItem);

        final JCheckBoxMenuItem highlightLegalMovesMenuItem = new JCheckBoxMenuItem("Highlight legal moves",
                                                                                 false);
        highlightLegalMovesMenuItem.addActionListener(event -> {
            highlightLegalMoves = highlightLegalMovesMenuItem.isSelected();
            System.out.println("highlightLegalMoves has been set to " + highlightLegalMoves + " now.");
        });
        preferencesMenu.addSeparator();
        preferencesMenu.add(highlightLegalMovesMenuItem);

        final JCheckBoxMenuItem highlightLastMoveMenuItem = new JCheckBoxMenuItem("Highlight last move",
                false);
        highlightLastMoveMenuItem.addActionListener(event ->{
            highlightLastMove = highlightLastMoveMenuItem.isSelected();
        });
        preferencesMenu.add(highlightLastMoveMenuItem);

        final JCheckBoxMenuItem setAutoQueenMenuItem = new JCheckBoxMenuItem("Set autoqueen");
        setAutoQueenMenuItem.addActionListener(event->{
            isAutoQueenEnabled = setAutoQueenMenuItem.isSelected();
        });

        preferencesMenu.add(setAutoQueenMenuItem);
        return preferencesMenu;
    }

    private void cleanMoveTilesUp(){
        startTile = null;
        destinationTile = null;
        humanMovedPiece = null;
        humanMovedPieceX = -TILE_SIZE;
        humanMovedPieceY = -TILE_SIZE;
    }

    private void cleanAllUpForNewGame(){
        this.takenPiecesPanel.clear();
        this.takenPiecesPanel.validate();
        this.takenPiecesPanel.repaint();
        this.gameHistoryPanel.clear();
        this.lastMove = null;
        cleanMoveTilesUp();
    }

    private void prepareNewClassicChessGame(){
        cleanAllUpForNewGame();
        this.game.prepareNewGame(GameType.CLASSIC_CHESS);
        this.chessBoard = this.game.getChessBoard();
    }

    private void prepareNewRandomFisherChessGame(){
        cleanAllUpForNewGame();
        this.game.prepareNewGame(GameType.RANDOM_FISHER_CHESS);
        this.chessBoard = this.game.getChessBoard();
    }

    private Piece choosePromotedPiece() {
        final Piece newPiece;
        int userAnswer = JOptionPane.showOptionDialog(null, "Choose a piece for promotion",
                "Pawn promotion", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                PROMOTED_PIECE_OPTIONS, PROMOTED_PIECE_OPTIONS[0]);
        switch (userAnswer){
            case 0:
                newPiece = Rook.createRook(destinationTile.getTileLocation(),
                        humanMovedPiece.getAlliance(),false);
                break;
            case 1:
                newPiece = Bishop.createBishop(destinationTile.getTileLocation(),
                        humanMovedPiece.getAlliance(),false);
                break;
            case 2:
                newPiece = Knight.createKnight(destinationTile.getTileLocation(),
                        humanMovedPiece.getAlliance(),false);
                break;
            default:
                newPiece = Queen.createQueen(destinationTile.getTileLocation(),
                        humanMovedPiece.getAlliance(),false);
                break;
        }
        return newPiece;
    }


    private class BoardPanel extends JPanel implements MouseMotionListener{
        public BoardPanel(){
            this.setPreferredSize(BOARD_PANEL_DIMENSION);
            this.addMouseMotionListener(this);
            this.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseReleased(MouseEvent e) {
                    if(isRightMouseButton(e)){
                        cleanMoveTilesUp();
                        redraw();
                    } else if(isLeftMouseButton(e)) {
                        int x = e.getX() / TILE_SIZE;
                        int y = e.getY() / TILE_SIZE;
                        if(boardOrientation.isOpposite()){
                            x = calculateFlippedCoordinate(x);
                            y = calculateFlippedCoordinate(y);
                        }
                        if(startTile == null){
                            startTile = chessBoard.getTile(x,y);
                            if(startTile.isOccupied()) {
                                humanMovedPiece = startTile.getPiece();
                                //System.out.println("Selected piece = " + startTile.getPiece().getPieceType().getName());
                            } else {
                                startTile = null;
                            }
                        } else {
                            destinationTile = chessBoard.getTile(x,y);
                            if(destinationTile != null){
                                //System.out.println("Destination tile = " + destinationTile.getTileLocation());
                                final Move move = detectMove();
                                if (game.tryToMakeMove(move)) {
                                    chessBoard = game.getChessBoard();
                                    lastMove = move;
                                    gameHistoryPanel.update(chessBoard, game.getMoveLog());
                                    if(move.isCapturingMove()) {
                                        takenPiecesPanel.update(game.getMoveLog());
                                    }
                                }
                                cleanMoveTilesUp();

                               if(game.isGameOver()){
                                   showGameOverMessageAndAskUserAboutNewGame();
                               }
                                redraw();
                            } else {
                                cleanMoveTilesUp();
                            }
                        }
                    }

                    super.mouseReleased(e);
                }

                private void showGameOverMessageAndAskUserAboutNewGame() {
                    final String[] options = {"Classic chess", "Chess960", "Exit"};
                    int userAnswer = JOptionPane.showOptionDialog(
                            null,
                            game.getGameStatus().getMessage() + "\nDo you want play again?",
                            "Select option",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.PLAIN_MESSAGE,
                            null,
                            options,
                            options[0]
                    );
                    switch (userAnswer){
                        case 0:
                            prepareNewClassicChessGame();
                            break;
                        case 1:
                            prepareNewRandomFisherChessGame();
                            break;
                        default:
                            System.exit(0);
                            break;
                    }
                }
            });
        }

        private Move detectMove(){
            if(humanMovedPiece == null || destinationTile == null) return Move.NULL_MOVE;
            if (chessBoard.getGameType().isRandomFisherChess() &&
                    humanMovedPiece.getPieceType().isKing()){
                final int kingsRookStartCoordinateX = chessBoard.getKingsRookStartCoordinateX();
                final int queensRookStartCoordinateX = chessBoard.getQueensRookStartCoordinateX();
                final Alliance currentPlayerAlliance = chessBoard.getCurrentPlayer().getAlliance();
                final int backRankCoordinateY =
                        chessBoard.getCurrentPlayer().getAlliance().isWhite() ?
                                BoardUtils.FIRST_RANK : BoardUtils.EIGHTH_RANK;
                final Location kingSideRookStartLocation =
                        BoardUtils.getPosition(kingsRookStartCoordinateX, backRankCoordinateY);
                final Location queenSideRookStartLocation =
                        BoardUtils.getPosition(queensRookStartCoordinateX, backRankCoordinateY);
                final Location kingsSideKingLocation =
                        BoardUtils.getKingsSideCastlingKingTargetPosition(currentPlayerAlliance);
                final Location queensSideKingLocation =
                        BoardUtils.getQueensSideCastlingKingTargetPosition(currentPlayerAlliance);
                final Location kingsSideRookTargetLocation =
                        BoardUtils.getKingsSideCastlingRookTargetPosition(currentPlayerAlliance);
                final Location queensSideRookTargetLocation =
                        BoardUtils.getQueensSideCastlingRookTargetPosition(currentPlayerAlliance);

                if (destinationTile.getTileLocation().equals(kingSideRookStartLocation)) {
                    Tile kingsRookStartTile = chessBoard.getTile(kingSideRookStartLocation);
                    if(kingsRookStartTile.isOccupied() && kingsRookStartTile.getPiece().getPieceType().isRook()){
                        Rook castlingRook = (Rook)kingsRookStartTile.getPiece();
                        if(castlingRook.getAlliance().equals(humanMovedPiece.getAlliance())) {
                            return Move.MoveFactory.createRandomFisherChessCastling(chessBoard,
                                    startTile.getTileLocation(), kingsSideKingLocation, castlingRook,
                                    kingsSideRookTargetLocation);
                        }
                    }

                } else if (destinationTile.getTileLocation().equals(queenSideRookStartLocation)) {
                    Tile queensRookStartTile = chessBoard.getTile(queenSideRookStartLocation);
                    if(queensRookStartTile.isOccupied() && queensRookStartTile.getPiece().getPieceType().isRook()){
                        Rook castlingRook = (Rook)queensRookStartTile.getPiece();
                        if(castlingRook.getAlliance().equals(humanMovedPiece.getAlliance())) {
                            return Move.MoveFactory.createRandomFisherChessCastling(chessBoard,
                                    startTile.getTileLocation(), queensSideKingLocation, castlingRook,
                                    queensSideRookTargetLocation);
                        }
                    }
                }
            }

            if(humanMovedPiece.getPieceType().isPawn() &&
                    chessBoard.getCurrentPlayer().getAlliance().isPawnPromotionSquare(destinationTile
                            .getTileLocation())) {
                final Piece promotedPiece = isAutoQueenEnabled ?
                        Queen.createQueen(destinationTile.getTileLocation(),
                                humanMovedPiece.getAlliance(),false) :
                        choosePromotedPiece();
                return Move.MoveFactory.createPawnPromotionMove(chessBoard,
                        startTile.getTileLocation(),
                        destinationTile.getTileLocation(),
                       promotedPiece);

            }
            return Move.MoveFactory.createMove(chessBoard, startTile.getTileLocation(),
                    destinationTile.getTileLocation());
        }

        @Override
        public void mouseDragged(final MouseEvent e) {
            e.consume();
        }

        @Override
        public void mouseMoved(final MouseEvent e) {
            if(humanMovedPiece != null){
                humanMovedPieceX = e.getX() - TILE_SIZE / 2;
                humanMovedPieceY = e.getY() - TILE_SIZE / 2;
                redraw();
            }
            e.consume();
        }

        int calculateFlippedCoordinate(final int coordinate){
            return BoardUtils.BOARD_SIZE - 1 - coordinate;
        }

        int toBoardCoordinate(final int coordinate){
            return coordinate * TILE_SIZE;
        }

        private void redraw(){
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    boardPanel.repaint();
                }
            });
        }

        private void drawChessField(Graphics2D g2D){
            for(int y = 0; y < BoardUtils.BOARD_SIZE; ++y){
                for(int x = 0; x < BoardUtils.BOARD_SIZE; ++x){
                    g2D.setColor((x + y) % 2 == 0 ? Color.WHITE : new Color(0, 202, 255));
                    g2D.fillRect(x * TILE_SIZE,y * TILE_SIZE,TILE_SIZE, TILE_SIZE);
                    final Tile currentTile = chessBoard.getTile(x, y);
                    //Highlights king which is under check
                    if((chessBoard.getCurrentPlayer().isUnderCheck() && currentTile.isOccupied() &&
                            currentTile.getPiece().getPieceType().isKing() &&
                            currentTile.getPiece().getAlliance().equals(chessBoard.getCurrentPlayer().getAlliance()))) {
                        g2D.setColor(Color.RED);
                        int checkedKingX = x;
                        int checkedKingY = y;
                        if(boardOrientation.isOpposite()){
                            checkedKingX = calculateFlippedCoordinate(checkedKingX);
                            checkedKingY = calculateFlippedCoordinate(checkedKingY);
                        }
                        g2D.fillRect(checkedKingX * TILE_SIZE, checkedKingY * TILE_SIZE,TILE_SIZE, TILE_SIZE);
                    }
                }
            }
        }

        private void drawAllActivePieces(Graphics2D g2D){
            chessBoard.getAllActivePieces().stream().filter(piece -> !piece.equals(humanMovedPiece))
                    .forEach((Piece piece) -> {
                        final BufferedImage bufferedImage = RESOURCE_MANAGER.getPieceImage(piece);
                        final int pieceX = piece.getLocation().getX();
                        final int pieceY = piece.getLocation().getY();
                        if(boardOrientation.isOpposite()){
                            final int flippedX = calculateFlippedCoordinate(pieceX);
                            final int flippedY = calculateFlippedCoordinate(pieceY);
                            g2D.drawImage(bufferedImage, flippedX * TILE_SIZE, flippedY * TILE_SIZE,
                                    TILE_SIZE, TILE_SIZE, this);
                        } else {
                            g2D.drawImage(bufferedImage, pieceX * TILE_SIZE, pieceY * TILE_SIZE, TILE_SIZE,
                                    TILE_SIZE, this);
                        }
                    });
        }

        private void drawLegalMovesHints(Graphics2D g2D){
            //Draw current player's legal moves
            if(highlightLegalMoves){
                chessBoard.getCurrentPlayer().getLegalMoves().stream()
                        .filter(move -> move.getMovedPiece().equals(humanMovedPiece)).forEach(move -> {
                    g2D.setColor(Color.GREEN.brighter());
                    int x = move.getDestination().getX();
                    int y = move.getDestination().getY();
                    if(boardOrientation.isOpposite()) {
                        x = calculateFlippedCoordinate(x);
                        y = calculateFlippedCoordinate(y);
                    }
                    x = x * TILE_SIZE + TILE_SIZE / 2;
                    y = y * TILE_SIZE + TILE_SIZE / 2;
                    g2D.fillOval(x - LEGAL_MOVE_HIGHLIGHT_CIRCLE_RADIUS,
                            y - LEGAL_MOVE_HIGHLIGHT_CIRCLE_RADIUS,
                            2 * LEGAL_MOVE_HIGHLIGHT_CIRCLE_RADIUS,
                            2 * LEGAL_MOVE_HIGHLIGHT_CIRCLE_RADIUS);

                });
            }
            //Draws selected piece which is following mouse cursor
            g2D.drawImage(RESOURCE_MANAGER.getPieceImage(humanMovedPiece), humanMovedPieceX, humanMovedPieceY,
                    TILE_SIZE, TILE_SIZE, this);
        }

        private void drawLastMoveHighlight(Graphics2D g2D){
            //This part of code draw arrow which highlights last move
            g2D.setColor(Color.RED);
            g2D.setStroke(new BasicStroke(LAST_MOVE_HIGHLIGHT_ARROW_LINE_WIDTH));
            int arrowStartPointX = lastMove.getMovedPiece().getLocation().getX();
            int arrowStartPointY = lastMove.getMovedPiece().getLocation().getY();
            int arrowEndPointX = lastMove.getDestination().getX();
            int arrowEndPointY = lastMove.getDestination().getY();

            if(boardOrientation.isOpposite()) {
                arrowStartPointX = calculateFlippedCoordinate(arrowStartPointX);
                arrowStartPointY = calculateFlippedCoordinate(arrowStartPointY);
                arrowEndPointX = calculateFlippedCoordinate(arrowEndPointX);
                arrowEndPointY = calculateFlippedCoordinate(arrowEndPointY);
            }

            arrowStartPointX = arrowStartPointX * TILE_SIZE + TILE_SIZE / 2;
            arrowStartPointY = arrowStartPointY * TILE_SIZE + TILE_SIZE / 2;
            arrowEndPointX = arrowEndPointX * TILE_SIZE + TILE_SIZE / 2;
            arrowEndPointY = arrowEndPointY * TILE_SIZE + TILE_SIZE / 2;

            final int dx = arrowEndPointX - arrowStartPointX;
            final int dy = arrowEndPointY - arrowStartPointY;
            int arrowHeadLeftPointX = 0,arrowHeadLeftPointY = 0,arrowHeadRightPointX = 0,arrowHeadRightPointY = 0;

            final double arrowHalfAngle = Math.toRadians(LAST_MOVE_HIGHLIGHT_ARROW_ANGLE / 2);
            //Horizontal move
            if(dy == 0){
                arrowHeadLeftPointX = arrowHeadRightPointX = arrowEndPointX +
                        ((dx > 0) ? -LAST_MOVE_HIGHLIGHT_ARROW_HEIGHT : LAST_MOVE_HIGHLIGHT_ARROW_HEIGHT);
                arrowHeadLeftPointY = (int)(arrowEndPointY - LAST_MOVE_HIGHLIGHT_ARROW_HEIGHT *
                        Math.tan(arrowHalfAngle));
                arrowHeadRightPointY = (int)(arrowEndPointY + LAST_MOVE_HIGHLIGHT_ARROW_HEIGHT *
                        Math.tan(arrowHalfAngle));
            } else if(dx == 0){
                //Vertical move
                arrowHeadLeftPointY = arrowHeadRightPointY = arrowEndPointY +
                        ((dy > 0) ? -LAST_MOVE_HIGHLIGHT_ARROW_HEIGHT : LAST_MOVE_HIGHLIGHT_ARROW_HEIGHT);
                arrowHeadLeftPointX = (int)(arrowEndPointX - LAST_MOVE_HIGHLIGHT_ARROW_HEIGHT *
                        Math.tan(arrowHalfAngle));
                arrowHeadRightPointX = (int)(arrowEndPointX + LAST_MOVE_HIGHLIGHT_ARROW_HEIGHT *
                        Math.tan(arrowHalfAngle));
            } else {
                final double arrowTilt = Math.atan2(dy, dx);

                arrowHeadLeftPointX = (int)(arrowEndPointX - LAST_MOVE_HIGHLIGHT_ARROW_HEIGHT *
                        Math.cos(arrowTilt + arrowHalfAngle));
                arrowHeadLeftPointY = (int)(arrowEndPointY - LAST_MOVE_HIGHLIGHT_ARROW_HEIGHT *
                        Math.sin(arrowTilt + arrowHalfAngle));
                arrowHeadRightPointX = (int)(arrowEndPointX - LAST_MOVE_HIGHLIGHT_ARROW_HEIGHT *
                        Math.cos(arrowTilt - arrowHalfAngle));
                arrowHeadRightPointY = (int)(arrowEndPointY - LAST_MOVE_HIGHLIGHT_ARROW_HEIGHT *
                        Math.sin(arrowTilt - arrowHalfAngle));
            }

            g2D.drawLine(arrowStartPointX, arrowStartPointY, arrowEndPointX, arrowEndPointY);
            g2D.drawLine(arrowHeadLeftPointX, arrowHeadLeftPointY, arrowEndPointX, arrowEndPointY);
            g2D.drawLine(arrowHeadRightPointX, arrowHeadRightPointY, arrowEndPointX, arrowEndPointY);
        }

        @Override
        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            Graphics2D g2D = (Graphics2D)g;
            g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            //Draws chess field
            drawChessField(g2D);
            //Draws all active pieces
            drawAllActivePieces(g2D);
            //Highlights
            if(humanMovedPiece != null) {
                drawLegalMovesHints(g2D);
            } else if(highlightLastMove && lastMove != null){
                drawLastMoveHighlight(g2D);
            }
        }
    }
}
