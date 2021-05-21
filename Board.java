package amazons;

import java.util.Collections;
import java.util.Iterator;
import java.util.Stack;
import java.util.Map;
import java.util.HashMap;

import static amazons.Piece.*;
import static amazons.Move.mv;


/** The state of an Amazons Game.
 *  @author Ho Jong Kang
 */
class Board {

    /** The number of squares on a side of the board. */
    static final int SIZE = 10;

    /** A hashmap containing all pieces and their coordinates. */
    private Map<String, Piece> store = new HashMap<>();

    /** Initializes a game board with SIZE squares on a side in the
     *  initial position. */
    Board() {
        init();
    }

    /** Initializes a copy of MODEL. */
    Board(Board model) {
        copy(model);
    }

    /** Copies MODEL into me. */
    void copy(Board model) {
        this._turn = model._turn;
        this._winner = model._winner;
        this.store.clear();
        this._movements.clear();
        this._movements.addAll(model._movements);
        this.store.putAll(model.store);
    }

    /** Clears the board to the initial position. */
    void init() {
        _turn = WHITE;
        _winner = EMPTY;
        _movements.clear();
        store.clear();
        store.put("3,0", WHITE);
        store.put("6,0", WHITE);
        store.put("0,3", WHITE);
        store.put("9,3", WHITE);
        store.put("0,6", BLACK);
        store.put("3,9", BLACK);
        store.put("6,9", BLACK);
        store.put("9,6", BLACK);
    }

    /** Return the Piece whose move it is (WHITE or BLACK). */
    Piece turn() {
        return _turn;
    }

    /** Return the number of moves (that have not been undone) for this
     *  board. */
    int numMoves() {
        return _movements.size();
    }

    /** Return the winner in the current position, or null if the game is
     *  not yet finished. */
    Piece winner() {
        if (!_winner.equals(EMPTY)) {
            return _winner;
        } else {
            return null;
        }

    }

    /** Return the contents the square at S. */
    final Piece get(Square s) {
        return get(s.col(), s.row());
    }

    /** Return the contents of the square at (COL, ROW), where
     *  0 <= COL, ROW <= 9. */
    final Piece get(int col, int row) {
        Piece result = store.get(Integer.toString(col)
                + "," + Integer.toString(row));
        if (result == null) {
            put(EMPTY, col, row);
            return store.get(Integer.toString(col)
                    + "," + Integer.toString(row));
        } else {
            return result;
        }
    }

    /** Return the contents of the square at COL ROW. */
    final Piece get(char col, char row) {
        return get(col - 'a', row - '1');
    }

    /** Set square S to P. */
    final void put(Piece p, Square s) {
        store.put(Integer.toString(s.col())
                + "," + Integer.toString(s.row()), p);
    }

    /** Set square (COL, ROW) to P. */
    final void put(Piece p, int col, int row) {
        store.put(Integer.toString(col) + "," + Integer.toString(row), p);
    }

    /** Set square COL ROW to P. */
    final void put(Piece p, char col, char row) {
        put(p, col - 'a', row - '1');
    }

    /** Return true iff FROM - TO is an unblocked queen move on the current
     *  board, ignoring the contents of ASEMPTY, if it is encountered.
     *  For this to be true, FROM-TO must be a queen move and the
     *  squares along it, other than FROM and ASEMPTY, must be
     *  empty. ASEMPTY may be null, in which case it has no effect. */
    boolean isUnblockedMove(Square from, Square to, Square asEmpty) {
        if (!from.isQueenMove(to)) {
            return false;
        } else {
            int direction = from.direction(to);
            int coldiff = to.col() - from.col();
            int rowdiff = to.row() - from.row();

            int maxdiff = Math.max(Math.abs(coldiff), Math.abs(rowdiff));

            for (int i = 1; i <= maxdiff; i += 1) {
                Square temp = from.queenMove(direction, i);
                if (!(this.get(temp).equals(EMPTY)
                        || temp.equals(asEmpty) || temp.equals(from))) {
                    return false;
                }
            }
            return true;
        }
    }

    /** Return true iff FROM is a valid starting square for a move. */
    boolean isLegal(Square from) {
        return this.get(from).equals(_turn);
    }

    /** Return true iff FROM-TO is a valid first part of move, ignoring
     *  spear throwing. */
    boolean isLegal(Square from, Square to) {
        return isLegal(from) && isUnblockedMove(from, to, null);
    }

    /** Return true iff FROM-TO(SPEAR) is a legal move in the current
     *  position. */
    boolean isLegal(Square from, Square to, Square spear) {
        return isLegal(from) && isUnblockedMove(from, to, null)
                && isUnblockedMove(to, spear, from);

    }

    /** Return true iff MOVE is a legal move in the current
     *  position. */
    boolean isLegal(Move move) {
        return isLegal(move.from(), move.to(), move.spear());
    }

    /** Move FROM-TO(SPEAR), assuming this is a legal move. */
    void makeMove(Square from, Square to, Square spear) {
        _movements.push(mv(from, to, spear));
        Piece temp = store.get(Integer.toString(from.col())
                + "," + Integer.toString(from.row()));
        store.put(Integer.toString(to.col())
                + "," + Integer.toString(to.row()), temp);
        store.put(Integer.toString(from.col())
                + "," + Integer.toString(from.row()), EMPTY);
        store.put(Integer.toString(spear.col())
                + "," + Integer.toString(spear.row()), SPEAR);
        _turn = _turn.opponent();

        Iterator<Move> checkremain = this.legalMoves();
        if (!checkremain.hasNext()) {
            _winner = _turn.opponent();
        }

    }

    /** Move according to MOVE, assuming it is a legal move. */
    void makeMove(Move move) {
        makeMove(move.from(), move.to(), move.spear());
    }

    /** Undo one move.  Has no effect on the initial board. */
    void undo() {
        for (int i = 0; i < 2; i += 1) {
            if (!_movements.empty()) {
                Move temp = _movements.pop();
                Piece lastmove = store.get(Integer.toString(temp.to().col())
                        + "," + Integer.toString(temp.to().row()));
                store.put(Integer.toString(temp.from().col())
                        + "," + Integer.toString(temp.from().row()), lastmove);
                store.put(Integer.toString(temp.to().col())
                        + "," + Integer.toString(temp.to().row()), EMPTY);
                store.put(Integer.toString(temp.spear().col())
                        + "," + Integer.toString(temp.spear().row()), EMPTY);
                _turn = _turn.opponent();
            }
        }
    }

    /** A stack storing all movements made. */
    private Stack<Move> _movements = new Stack<>();

    /** Return an Iterator over the Squares that are reachable by an
     *  unblocked queen move from FROM. Does not pay attention to what
     *  piece (if any) is on FROM, nor to whether the game is finished.
     *  Treats square ASEMPTY (if non-null) as if it were EMPTY.  (This
     *  feature is useful when looking for Moves, because after moving a
     *  piece, one wants to treat the Square it came from as empty for
     *  purposes of spear throwing.) */
    Iterator<Square> reachableFrom(Square from, Square asEmpty) {
        return new ReachableFromIterator(from, asEmpty);
    }

    /** Return an Iterator over all legal moves on the current board. */
    Iterator<Move> legalMoves() {
        return new LegalMoveIterator(_turn);
    }

    /** Return an Iterator over all legal moves on the current board for
     *  SIDE (regardless of whose turn it is). */
    Iterator<Move> legalMoves(Piece side) {
        return new LegalMoveIterator(side);
    }

    /** An iterator used by reachableFrom. */
    private class ReachableFromIterator implements Iterator<Square> {

        /** Iterator of all squares reachable by queen move from FROM,
         *  treating ASEMPTY as empty. */
        ReachableFromIterator(Square from, Square asEmpty) {
            _from = from;
            _dir = 0;
            _steps = 0;
            _asEmpty = asEmpty;
            toNext();

        }

        @Override
        public boolean hasNext() {
            return _dir < 8;
        }

        @Override
        public Square next() {
            Square result = _from.queenMove(_dir, _steps);
            toNext();
            return result;
        }

        /** Advance _dir and _steps, so that the next valid Square is
         *  _steps steps in direction _dir from _from. */
        private void toNext() {
            if (isUnblockedMove(_from,
                    _from.queenMove(_dir, _steps + 1), _asEmpty)) {
                _steps += 1;
            } else {
                _steps = 0;
                _dir += 1;
                if (_dir >= 8) {
                    return;
                }
                toNext();
            }
        }

        /** Starting square. */
        private Square _from;
        /** Current direction. */
        private int _dir;
        /** Current distance. */
        private int _steps;
        /** Square treated as empty. */
        private Square _asEmpty;
    }

    /** An iterator used by legalMoves. */
    private class LegalMoveIterator implements Iterator<Move> {

        /** All legal moves for SIDE (WHITE or BLACK). */
        LegalMoveIterator(Piece side) {
            _startingSquares = Square.iterator();
            _spearThrows = NO_SQUARES;
            _pieceMoves = NO_SQUARES;
            _fromPiece = side;
            stop = false;
            toNext();
        }

        @Override
        public boolean hasNext() {
            return !stop;
        }

        @Override
        public Move next() {
            Move result = mv(_start, _nextSquare, _nextSpear);
            toNext();
            return result;
        }

        /** Advance so that the next valid Move is
         *  _start-_nextSquare(sp), where sp is the next value of
         *  _spearThrows. */
        private void toNext() {
            try {
                if (_spearThrows.hasNext()) {
                    _nextSpear = _spearThrows.next();
                    return;
                } else if (_pieceMoves.hasNext()) {
                    _nextSquare = _pieceMoves.next();
                    _spearThrows = reachableFrom(_nextSquare, _start);
                    _nextSpear = _spearThrows.next();
                    return;
                } else if (_startingSquares.hasNext()) {
                    _start = _startingSquares.next();
                    while (_startingSquares.hasNext()) {
                        if (!isLegal(_start)) {
                            _start = _startingSquares.next();
                        } else {
                            _pieceMoves = reachableFrom(_start, null);
                            _nextSquare = _pieceMoves.next();
                            _spearThrows = reachableFrom(_nextSquare, _start);
                            _nextSpear = _spearThrows.next();
                            return;
                        }
                    }
                    if (!_startingSquares.hasNext()) {
                        stop = true;
                        return;
                    }
                } else {
                    stop = true;
                    return;
                }
            } catch (NullPointerException e) {
                toNext();
            }
        }

        /** Stores next spear. */
        private Square _nextSpear;

        /** Boolean value to tell whether to stop. */
        private boolean stop;


        /** Color of side whose moves we are iterating. */
        private Piece _fromPiece;
        /** Current starting square. */
        private Square _start;
        /** Remaining starting squares to consider. */
        private Iterator<Square> _startingSquares;
        /** Current piece's new position. */
        private Square _nextSquare;
        /** Remaining moves from _start to consider. */
        private Iterator<Square> _pieceMoves;
        /** Remaining spear throws from _piece to consider. */
        private Iterator<Square> _spearThrows;
    }

    @Override
    public String toString() {
        String result = "";
        for (int i = SIZE - 1; i >= 0; i -= 1) {
            result += "   ";
            for (int j = 0; j < SIZE; j += 1) {
                try {
                    result += store.get(Integer.toString(j)
                            + "," + Integer.toString(i)).toString() + " ";
                } catch (NullPointerException e) {
                    result += "- ";
                }
            }
            result = result.substring(0, result.length() - 1);
            result += "\n";
        }
        return result;
    }

    /** An empty iterator for initialization. */
    private static final Iterator<Square> NO_SQUARES =
        Collections.emptyIterator();

    /** Piece whose turn it is (BLACK or WHITE). */
    private Piece _turn;
    /** Cached value of winner on this board, or EMPTY if it has not been
     *  computed. */
    private Piece _winner;
}
