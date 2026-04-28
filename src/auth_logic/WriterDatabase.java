package auth_logic;

import java.io.File;
import java.util.logging.Logger;

/** Persistence facade — delegates to PlayerData's file I/O. */
public class WriterDatabase {

    private static final Logger LOG = Logger.getLogger(WriterDatabase.class.getName());

    public static void save(File f) {
        PlayerData.saveToFile();
        LOG.info("Player data saved.");
    }
}
