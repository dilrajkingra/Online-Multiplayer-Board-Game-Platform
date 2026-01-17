package auth_logic;
import java.util.ArrayList;
import java.util.List;

public class PlayerData {
    public static List<Player> players = new ArrayList<>();
    
    // Initialize dummy data for the leaderboard to display
    static {
        Player p1 = new Player("Alice");
        p1.getPlayerStats().setRankChess(1500);
        p1.getPlayerStats().setRankGo(1100);
        
        Player p2 = new Player("Bob");
        p2.getPlayerStats().setRankChess(1200);
        p2.getPlayerStats().setRankGo(1350);
        
        Player p3 = new Player("Charlie");
        p3.getPlayerStats().setRankChess(900);
        p3.getPlayerStats().setRankGo(900);
        
        players.add(p1);
        players.add(p2);
        players.add(p3);
    }
}