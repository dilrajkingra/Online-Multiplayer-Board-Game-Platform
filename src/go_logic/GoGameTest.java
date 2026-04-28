package go_logic;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GoGameTest {

    @Test
    void placeStoneOnEmptyBoard() throws IllegalMoveException {
        GoGame game = new GoGame(9);

        game.playMove(4, 4); // Black
        assertEquals(StoneColor.WHITE, game.getCurrentPlayer());
        assertEquals(StoneColor.BLACK, game.getBoard().getStone(4, 4));
    }

    @Test
    void suicideMoveNotAllowed() throws IllegalMoveException {
        GoGame game = new GoGame(5);
        GoBoard b = game.getBoard();

        // Create a white "eye" at (1,1) completely surrounded by black.
        game.playMove(0, 1); // B
        game.playMove(4, 4); // W
        game.playMove(1, 0); // B
        game.playMove(4, 3); // W
        game.playMove(1, 2); // B
        game.playMove(3, 4); // W
        game.playMove(2, 1); // B

        IllegalMoveException ex = assertThrows(
                IllegalMoveException.class,
                () -> game.playMove(1, 1)
        );
        assertTrue(ex.getMessage().contains("Suicide"));
        assertTrue(b.isEmpty(1, 1));
    }

    @Test
    void twoPassesEndGame() throws IllegalMoveException {
        GoGame game = new GoGame(9);

        assertFalse(game.isGameOver());
        game.pass(); // B passes
        assertFalse(game.isGameOver());
        game.pass(); // W passes
        assertTrue(game.isGameOver());

        assertThrows(IllegalMoveException.class, () -> game.playMove(0, 0));
    }
}
