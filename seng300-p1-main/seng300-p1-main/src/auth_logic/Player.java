package auth_logic;
import java.util.ArrayList;
import java.util.List;

public class Player {
    private String username;
    private PlayerStats stats;
    public List<Player> friendsList = new ArrayList<>(); // Stub for friends

    public Player(String username) {
        this.username = username;
        this.stats = new PlayerStats();
    }

    public String getUsername() { return username; }
    public PlayerStats getPlayerStats() { return stats; }
    public String getStatus() { return "Online"; } // Stub
}