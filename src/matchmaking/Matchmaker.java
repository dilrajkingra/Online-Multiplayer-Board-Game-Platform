// File: src/matchmaking/Matchmaker.java
package matchmaking;

import java.util.*;

/**
 * Matchmaker is responsible for:
 * - Queueing players for a given game type
 * - Removing players from the queue
 * - Pairing players based on rating similarity
 * - Estimating wait time for a player
 *
 * The queue for each game type is kept sorted by rating at insertion time,
 * so findMatch() never needs to re-sort — O(log n) insert, O(n) scan.
 */
public class Matchmaker {

    /** Maximum rating difference allowed when pairing players. */
    static final int MAX_RATING_DIFFERENCE = 200;

    /** Base wait time (seconds) used by estimateWait(). */
    private static final int BASE_WAIT_SECONDS = 15;

    /** Per-game sorted queues (sorted by rating ascending). */
    private final Map<GameType, List<QueuedPlayer>> queues = new EnumMap<>(GameType.class);

    public Matchmaker() {
        for (GameType type : GameType.values()) {
            queues.put(type, new ArrayList<>());
        }
    }

    // ── Internal record ───────────────────────────────────────────────────────

    static final class QueuedPlayer {
        final Player player;
        final GameType gameType;
        final int rating;
        final long enqueuedAtMillis;

        QueuedPlayer(Player player, GameType gameType) {
            this.player = player;
            this.gameType = gameType;
            this.rating = player.getRating();
            this.enqueuedAtMillis = System.currentTimeMillis();
        }
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Adds a player to the queue for the given game type.
     * Inserted in sorted order by rating so findMatch() never re-sorts.
     */
    public synchronized void enqueue(Player player, GameType gameType) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(gameType, "gameType");

        List<QueuedPlayer> queue = queues.get(gameType);

        // Reject duplicates
        for (QueuedPlayer qp : queue) {
            if (qp.player.equals(player)) return;
        }

        QueuedPlayer newQP = new QueuedPlayer(player, gameType);

        // Binary-search insertion point to maintain sorted order
        int lo = 0, hi = queue.size();
        while (lo < hi) {
            int mid = (lo + hi) >>> 1;
            if (queue.get(mid).rating <= newQP.rating) lo = mid + 1;
            else hi = mid;
        }
        queue.add(lo, newQP);
    }

    /**
     * Removes a player from all queues (e.g. player cancels search).
     */
    public synchronized void dequeue(Player player) {
        Objects.requireNonNull(player, "player");
        for (List<QueuedPlayer> queue : queues.values()) {
            queue.removeIf(qp -> qp.player.equals(player));
        }
    }

    /**
     * Attempts to find a match across all game types.
     * Returns the first Match found, or null if none is available.
     */
    public synchronized Match findMatch() {
        for (GameType type : GameType.values()) {
            Match match = findMatchForType(type);
            if (match != null) return match;
        }
        return null;
    }

    /**
     * Estimates wait time (seconds) for a given player.
     * Returns -1 if the player is not currently queued.
     */
    public synchronized int estimateWait(Player player) {
        Objects.requireNonNull(player, "player");
        for (List<QueuedPlayer> queue : queues.values()) {
            for (int i = 0; i < queue.size(); i++) {
                if (queue.get(i).player.equals(player)) {
                    int pairsAhead = i / 2;
                    return (pairsAhead + 1) * BASE_WAIT_SECONDS;
                }
            }
        }
        return -1;
    }

    /** Returns a snapshot of queued players for a given game type. */
    public synchronized List<Player> getQueuedPlayers(GameType type) {
        Objects.requireNonNull(type, "type");
        List<Player> result = new ArrayList<>();
        for (QueuedPlayer qp : queues.get(type)) {
            result.add(qp.player);
        }
        return result;
    }

    // ── Internal ─────────────────────────────────────────────────────────────

    private Match findMatchForType(GameType type) {
        List<QueuedPlayer> queue = queues.get(type);
        if (queue.size() < 2) return null;

        // Queue is already sorted by rating — scan adjacent pairs for best match
        QueuedPlayer bestA = null, bestB = null;
        int bestDiff = Integer.MAX_VALUE;

        for (int i = 0; i < queue.size() - 1; i++) {
            QueuedPlayer a = queue.get(i);
            QueuedPlayer b = queue.get(i + 1);
            int diff = b.rating - a.rating; // b >= a since sorted
            if (diff < bestDiff && diff <= MAX_RATING_DIFFERENCE) {
                bestDiff = diff;
                bestA = a;
                bestB = b;
            }
        }

        if (bestA == null) return null;

        queue.remove(bestA);
        queue.remove(bestB);
        return new Match(type, bestA.player, bestB.player);
    }
}
