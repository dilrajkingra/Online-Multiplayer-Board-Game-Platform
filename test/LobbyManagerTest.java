import matchmaking.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LobbyManager — creation, joining, full lobbies, and ID uniqueness.
 */
class LobbyManagerTest {

    private LobbyManager manager;
    private Player host;
    private Player guest;

    @BeforeEach
    void setUp() {
        manager = new LobbyManager();
        host    = new Player("h", "Host",  1200);
        guest   = new Player("g", "Guest", 1200);
    }

    @Test
    void createLobby_returnsNonNullId() {
        String id = manager.createLobby(host, GameType.CHESS);
        assertNotNull(id);
        assertFalse(id.isBlank());
    }

    @Test
    void createLobby_idIs6Chars() {
        String id = manager.createLobby(host, GameType.GO);
        assertEquals(6, id.length(), "Lobby ID should be 6 characters");
    }

    @Test
    void createLobby_multipleIds_areUnique() {
        String id1 = manager.createLobby(host,  GameType.CHESS);
        Player host2 = new Player("h2", "Host2", 1200);
        String id2 = manager.createLobby(host2, GameType.CHESS);
        assertNotEquals(id1, id2, "Two lobby IDs should not collide");
    }

    @Test
    void getLobby_existingId_returnsLobby() {
        String id = manager.createLobby(host, GameType.CHESS);
        assertNotNull(manager.getLobby(id));
    }

    @Test
    void getLobby_unknownId_returnsNull() {
        assertNull(manager.getLobby("ZZZZZZ"));
    }

    @Test
    void joinLobby_validGuest_returnsLobby() {
        String id = manager.createLobby(host, GameType.GO);
        Lobby lobby = manager.joinLobby(id, guest);
        assertNotNull(lobby, "Guest should be able to join an open lobby");
    }

    @Test
    void joinLobby_unknownId_returnsNull() {
        assertNull(manager.joinLobby("BADID1", guest));
    }

    @Test
    void joinLobby_hostRejoinsSelf_returnsNull() {
        String id = manager.createLobby(host, GameType.CHESS);
        assertNull(manager.joinLobby(id, host), "Host cannot join their own lobby twice");
    }

    @Test
    void joinLobby_fullLobby_returnsNull() {
        String id = manager.createLobby(host, GameType.CHESS);
        manager.joinLobby(id, guest);

        Player third = new Player("t", "Third", 1200);
        assertNull(manager.joinLobby(id, third), "Full lobby should reject a third player");
    }

    @Test
    void createLobby_nullHost_throwsException() {
        assertThrows(NullPointerException.class,
                () -> manager.createLobby(null, GameType.CHESS));
    }

    @Test
    void joinLobby_nullPlayer_throwsException() {
        String id = manager.createLobby(host, GameType.CHESS);
        assertThrows(NullPointerException.class,
                () -> manager.joinLobby(id, null));
    }
}
