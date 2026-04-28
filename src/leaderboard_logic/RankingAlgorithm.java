package leaderboard_logic;

import java.io.File;
import auth_logic.Player;
import auth_logic.PlayerStats;
import auth_logic.WriterDatabase;

public class RankingAlgorithm {

    private static final int K_FACTOR_CHESS = 32;
    private static final int K_FACTOR_GO = 24;
    private static final int K_FACTOR_TTT = 16;

    public static int getPlayerRating(Player player, String game) {
        if (game == null || game.isBlank()) throw new IllegalArgumentException("Invalid game type.");

        PlayerStats stats = player.getPlayerStats();
        // Updated to handle mixed case inputs nicely
        if (game.toLowerCase().startsWith("chess")) return stats.getRankChess();
        if (game.toLowerCase().startsWith("go")) return stats.getRankGo();
        if (game.toLowerCase().contains("tic")) return stats.getRankTTT();
        
        throw new IllegalArgumentException("Invalid game type: " + game);
    }

    // (Note: You can keep the rest of the original logic methods if you need them, 
    // but this getter is the only one required for the View Use Case)
}