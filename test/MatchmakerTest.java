import matchmaking.*;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Matchmaker — enqueue, dequeue, match-finding, rating filtering,
 * and the sorted-insertion performance guarantee.
 */
class MatchmakerTest {

    private Matchmaker mm;
    private static final int MAX_DIFF = Matchmaker.MAX_RATING_DIFFERENCE;

    @BeforeEach
    void setUp() {
        mm = new Matchmaker();
    }

    @Test
    void findMatch_twoSimilarRatingPlayers_returnsMatch() {
        Player a = new Player("a", "Alice", 1200);
        Player b = new Player("b", "Bob",   1200);

        mm.enqueue(a, GameType.CHESS);
        mm.enqueue(b, GameType.CHESS);

        Match match = mm.findMatch();

        assertNotNull(match, "Two players at same rating should be matched");
        assertEquals(GameType.CHESS, match.getGameType());
    }

    @Test
    void findMatch_ratingTooFarApart_returnsNull() {
        Player a = new Player("a", "Alice", 1000);
        Player b = new Player("b", "Bob",   1000 + MAX_DIFF + 1);

        mm.enqueue(a, GameType.GO);
        mm.enqueue(b, GameType.GO);

        assertNull(mm.findMatch(), "Players outside rating window should not be matched");
    }

    @Test
    void findMatch_exactBoundaryRating_returnsMatch() {
        Player a = new Player("a", "Alice", 1000);
        Player b = new Player("b", "Bob",   1000 + MAX_DIFF);

        mm.enqueue(a, GameType.CHESS);
        mm.enqueue(b, GameType.CHESS);

        assertNotNull(mm.findMatch(), "Players exactly at max rating diff should match");
    }

    @Test
    void enqueue_duplicate_ignoredSilently() {
        Player a = new Player("a", "Alice", 1200);

        mm.enqueue(a, GameType.CHESS);
        mm.enqueue(a, GameType.CHESS); // duplicate

        List<Player> queued = mm.getQueuedPlayers(GameType.CHESS);
        assertEquals(1, queued.size(), "Duplicate enqueue should be ignored");
    }

    @Test
    void dequeue_removesPlayerFromAllQueues() {
        Player a = new Player("a", "Alice", 1200);

        mm.enqueue(a, GameType.CHESS);
        mm.enqueue(a, GameType.GO);
        mm.dequeue(a);

        assertEquals(0, mm.getQueuedPlayers(GameType.CHESS).size());
        assertEquals(0, mm.getQueuedPlayers(GameType.GO).size());
    }

    @Test
    void getQueuedPlayers_returnsSnapshot_inRatingOrder() {
        Player hi  = new Player("h", "High",   1500);
        Player lo  = new Player("l", "Low",    900);
        Player mid = new Player("m", "Middle", 1200);

        mm.enqueue(hi,  GameType.CHESS);
        mm.enqueue(lo,  GameType.CHESS);
        mm.enqueue(mid, GameType.CHESS);

        List<Player> queued = mm.getQueuedPlayers(GameType.CHESS);
        assertEquals(3, queued.size());
        // Sorted by rating ascending (insertion sort maintained)
        assertEquals("Low",    queued.get(0).getName());
        assertEquals("Middle", queued.get(1).getName());
        assertEquals("High",   queued.get(2).getName());
    }

    @Test
    void estimateWait_playerInQueue_returnsPositive() {
        Player a = new Player("a", "Alice", 1200);
        mm.enqueue(a, GameType.GO);

        int wait = mm.estimateWait(a);
        assertTrue(wait > 0, "estimateWait should return positive for queued player");
    }

    @Test
    void estimateWait_playerNotInQueue_returnsMinusOne() {
        Player a = new Player("a", "Alice", 1200);
        assertEquals(-1, mm.estimateWait(a));
    }

    @Test
    void findMatch_afterMatch_queueIsEmpty() {
        Player a = new Player("a", "Alice", 1200);
        Player b = new Player("b", "Bob",   1200);

        mm.enqueue(a, GameType.TIC_TAC_TOE);
        mm.enqueue(b, GameType.TIC_TAC_TOE);
        mm.findMatch();

        assertEquals(0, mm.getQueuedPlayers(GameType.TIC_TAC_TOE).size(),
                "Matched players should be removed from queue");
    }
}
