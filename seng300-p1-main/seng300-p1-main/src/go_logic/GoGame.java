package go_logic;
import java.util.EnumMap;
import java.util.Map;

/**
 * High-level Go game: tracks whose turn it is, captures, and simple end-of-game.
 */
public class GoGame {

    private final GoBoard board;
    private StoneColor currentPlayer = StoneColor.BLACK;
    private int consecutivePasses = 0;
    private final Map<StoneColor, Integer> capturedByPlayer = new EnumMap<>(StoneColor.class);

    public GoGame(int size) {
        this.board = new GoBoard(size);
        capturedByPlayer.put(StoneColor.BLACK, 0);
        capturedByPlayer.put(StoneColor.WHITE, 0);
    }

    public GoBoard getBoard() {
        return board;
    }

    public StoneColor getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean isGameOver() {
        return consecutivePasses >= 2;
    }

    public int getCapturedStones(StoneColor player) {
        return capturedByPlayer.get(player);
    }

    public void playMove(int row, int col) throws IllegalMoveException {
        if (isGameOver()) {
            throw new IllegalMoveException("Game already over");
        }

        MoveResult result = board.playStone(row, col, currentPlayer);
        int prev = capturedByPlayer.get(currentPlayer);
        capturedByPlayer.put(currentPlayer, prev + result.getCaptureCount());

        consecutivePasses = 0;
        currentPlayer = currentPlayer.opposite();
    }

    public void pass() {
        if (isGameOver()) return;
        consecutivePasses++;
        currentPlayer = currentPlayer.opposite();
    }

    public void resign() {
        consecutivePasses = 2; // force game over
    }
}
