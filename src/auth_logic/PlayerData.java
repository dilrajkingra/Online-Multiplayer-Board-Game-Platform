package auth_logic;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;

public class PlayerData {

    private static final Logger LOG = Logger.getLogger(PlayerData.class.getName());
    private static final String DATA_FILE = "playerdata.properties";

    public static List<Player> players = new ArrayList<>();

    static {
        if (!loadFromFile()) {
            // Seed with default demo players when no data file exists yet
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
            saveToFile();
        }
    }

    /** Persists the current player list to disk. */
    public static synchronized void saveToFile() {
        Properties props = new Properties();
        props.setProperty("count", String.valueOf(players.size()));
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            PlayerStats s = p.getPlayerStats();
            String pfx = "player." + i + ".";
            props.setProperty(pfx + "username",   p.getUsername());
            props.setProperty(pfx + "chess",      String.valueOf(s.getRankChess()));
            props.setProperty(pfx + "go",         String.valueOf(s.getRankGo()));
            props.setProperty(pfx + "ttt",        String.valueOf(s.getRankTTT()));
            props.setProperty(pfx + "winsChess",  String.valueOf(s.getWinsChess()));
            props.setProperty(pfx + "lossChess",  String.valueOf(s.getLossChess()));
            props.setProperty(pfx + "tieChess",   String.valueOf(s.getTieChess()));
            props.setProperty(pfx + "winsGo",     String.valueOf(s.getWinsGo()));
            props.setProperty(pfx + "lossGo",     String.valueOf(s.getLossGo()));
            props.setProperty(pfx + "tieGo",      String.valueOf(s.getTieGo()));
            props.setProperty(pfx + "winsTTT",    String.valueOf(s.getWinsTTT()));
            props.setProperty(pfx + "lossTTT",    String.valueOf(s.getLossTTT()));
            props.setProperty(pfx + "tieTTT",     String.valueOf(s.getTieTTT()));
        }
        try (OutputStream out = Files.newOutputStream(Paths.get(DATA_FILE))) {
            props.store(out, "OMG Player Data");
        } catch (IOException e) {
            LOG.warning("Could not save player data: " + e.getMessage());
        }
    }

    /** Loads player list from disk. Returns true if at least one player was loaded. */
    private static boolean loadFromFile() {
        Path path = Paths.get(DATA_FILE);
        if (!Files.exists(path)) return false;

        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            props.load(in);
        } catch (IOException e) {
            LOG.warning("Could not load player data: " + e.getMessage());
            return false;
        }

        int count = Integer.parseInt(props.getProperty("count", "0"));
        for (int i = 0; i < count; i++) {
            String pfx = "player." + i + ".";
            String username = props.getProperty(pfx + "username");
            if (username == null || username.isBlank()) continue;

            Player p = new Player(username);
            PlayerStats s = p.getPlayerStats();
            s.setRankChess(intProp(props, pfx + "chess",     1200));
            s.setRankGo(   intProp(props, pfx + "go",        1200));
            s.setRankTTT(  intProp(props, pfx + "ttt",       1200));
            s.setWinsChess(intProp(props, pfx + "winsChess", 0));
            s.setLossChess(intProp(props, pfx + "lossChess", 0));
            s.setTieChess( intProp(props, pfx + "tieChess",  0));
            s.setWinsGo(   intProp(props, pfx + "winsGo",    0));
            s.setLossGo(   intProp(props, pfx + "lossGo",    0));
            s.setTieGo(    intProp(props, pfx + "tieGo",     0));
            s.setWinsTTT(  intProp(props, pfx + "winsTTT",   0));
            s.setLossTTT(  intProp(props, pfx + "lossTTT",   0));
            s.setTieTTT(   intProp(props, pfx + "tieTTT",    0));
            players.add(p);
        }
        return !players.isEmpty();
    }

    private static int intProp(Properties p, String key, int defaultVal) {
        try {
            String v = p.getProperty(key);
            return (v != null) ? Integer.parseInt(v) : defaultVal;
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }
}
