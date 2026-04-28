// File: src/matchmaking/LobbyManager.java
package matchmaking;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Manages lobbies: creating them and joining them.
 */
public class LobbyManager {

    private final Map<String, Lobby> lobbies = new HashMap<>();

    /**
     * Create a new lobby hosted by the given player for the specified game type.
     * Returns the generated lobby ID (the host can share this with a friend).
     */
    public synchronized String createLobby(Player host, GameType gameType) {
        Objects.requireNonNull(host, "host");
        Objects.requireNonNull(gameType, "gameType");

        String id = generateLobbyId();
        lobbies.put(id, new Lobby(id, gameType, host));
        return id;
    }

    /**
     * Attempt to join a lobby by ID.
     * Returns the Lobby if join succeeded, or null if the lobby does not exist,
     * is full, or the player is already in it.
     */
    public synchronized Lobby joinLobby(String lobbyId, Player player) {
        Objects.requireNonNull(lobbyId, "lobbyId");
        Objects.requireNonNull(player, "player");

        Lobby lobby = lobbies.get(lobbyId);
        if (lobby == null || !lobby.canJoin(player)) return null;

        lobby.addPlayer(player);
        return lobby;
    }

    /** Get a lobby by ID. Returns null if it does not exist. */
    public synchronized Lobby getLobby(String lobbyId) {
        Objects.requireNonNull(lobbyId, "lobbyId");
        return lobbies.get(lobbyId);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private String generateLobbyId() {
        String id;
        // Loop instead of recursion — no stack-overflow risk even with many lobbies
        do {
            id = UUID.randomUUID().toString().replace("-", "")
                     .substring(0, 6).toUpperCase();
        } while (lobbies.containsKey(id));
        return id;
    }
}
