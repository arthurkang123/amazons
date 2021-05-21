package amazons;

import static amazons.Piece.*;
import java.util.Iterator;

/** A Player that automatically generates moves.
 *  @author Ho Jong Kang
 */
class AI extends Player {

    /** A position magnitude indicating a win (for white if positive, black
     *  if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 1;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new AI with no piece or controller (intended to produce
     *  a template). */
    AI() {
        this(null, null);
    }

    /** A new AI playing PIECE under control of CONTROLLER. */
    AI(Piece piece, Controller controller) {
        super(piece, controller);
    }

    @Override
    Player create(Piece piece, Controller controller) {
        return new AI(piece, controller);
    }

    @Override
    String myMove() {
        Move move = findMove();
        _controller.reportMove(move);
        return move.toString();
    }

    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        Board b = new Board(board());
        if (_myPiece == WHITE) {
            findMove(b, maxDepth(b), true, 1, -INFTY, INFTY);
        } else {
            findMove(b, maxDepth(b), true, -1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /** The move found by the last call to one of the ...FindMove methods
     *  below. */
    private Move _lastFoundMove;

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _lastFoundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _lastMoveFound. */
    private int findMove(Board board, int depth, boolean saveMove, int sense,
                         int alpha, int beta) {
        if (depth == 0 || board.winner() != null) {
            return staticScore(board);
        }
        int bestVal;
        Iterator<Move> possiblemove = board.legalMoves();
        if (sense > 0) {
            bestVal = Integer.MIN_VALUE;
            while (possiblemove.hasNext()) {
                Move node = possiblemove.next();
                Board copied = new Board(board);
                copied.makeMove(node);
                int current = Math.max(bestVal, findMove(copied, depth - 1,
                        false, sense * -1, alpha, beta));
                if (current > bestVal) {
                    if (saveMove) {
                        _lastFoundMove = node;
                    }
                    bestVal = current;
                }
                if (bestVal > beta) {
                    return bestVal;
                }
                alpha = Math.max(alpha, bestVal);
            }
        } else {
            bestVal = Integer.MAX_VALUE;
            while (possiblemove.hasNext()) {
                Move node = possiblemove.next();
                Board copied = new Board(board);
                copied.makeMove(node);
                int current = Math.min(bestVal, findMove(copied, depth - 1,
                        false, sense * -1, alpha, beta));
                if (current < bestVal) {
                    if (saveMove) {
                        _lastFoundMove = node;
                    }
                    bestVal = current;
                }
                if (bestVal < alpha) {
                    return bestVal;
                }
                beta = Math.min(beta, bestVal);
            }
        }
        return bestVal;
    }

    /** Upper bound used in maxDepth. */
    static final int UPPERBOUND = 70;

    /** Lower bound used in maxDepth. */
    static final int LOWERBOUND = 40;

    /** Return a heuristically determined maximum search depth
     *  based on characteristics of BOARD. */
    private int maxDepth(Board board) {
        int N = board.numMoves();
        if (N > UPPERBOUND) {
            return 5;
        } else if (N < LOWERBOUND) {
            return 1;
        } else {
            return 2;
        }
    }


    /** Return a heuristic value for BOARD. */
    private int staticScore(Board board) {
        Piece winner = board.winner();
        if (winner == BLACK) {
            return -WINNING_VALUE;
        } else if (winner == WHITE) {
            return WINNING_VALUE;
        }
        Piece currturn = WHITE;
        int myCounter = 0;
        int yourCounter = 0;
        int steps = 1;

        Iterator<Square> boarditr = Square.iterator();
        while (boarditr.hasNext()) {
            Square curr = boarditr.next();
            if (board.get(curr).equals(currturn)) {
                for (int i = 0; i < 8; i += 1) {
                    try {
                        for (int step = 1; step <= steps; step += 1) {
                            if (board.get(curr.queenMove(i, step)).
                                    equals(EMPTY)) {
                                myCounter += 1;
                            }
                        }
                    } catch (NullPointerException e) {
                        myCounter = myCounter;
                    }
                }
            } else if (board.get(curr).equals(currturn.opponent())) {
                for (int i = 0; i < 8; i += 1) {
                    try {
                        for (int step = 1; step <= steps; step += 1) {
                            if (board.get(curr.queenMove(i, step)).
                                    equals(EMPTY)) {
                                yourCounter += 1;
                            }
                        }
                    } catch (NullPointerException e) {
                        yourCounter = yourCounter;
                    }
                }
            }
        }
        return myCounter - yourCounter;
    }


}
