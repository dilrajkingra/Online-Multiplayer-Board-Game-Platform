import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Handles user registration and authentication using SHA-256 + random salt.
 * Credentials are persisted to users.properties on disk.
 * Format per entry: username(lowercase) -> salt:hash:email
 */
public class AuthService {

    private static final Logger LOG = Logger.getLogger(AuthService.class.getName());
    private static final String USER_FILE = "users.properties";
    private static final Properties store = new Properties();

    static {
        loadStore();
    }

    // ── Public API ──────────────────────────────────────────────────────────

    /**
     * Registers a new user. Returns false if the username is already taken or
     * any argument is blank.
     */
    public static boolean register(String username, String email, String password) {
        if (isBlank(username) || isBlank(email) || isBlank(password)) return false;
        String key = username.toLowerCase(Locale.ROOT);
        synchronized (store) {
            if (store.containsKey(key)) return false;
            String salt = generateSalt();
            String hash = hash(password, salt);
            store.setProperty(key, salt + ":" + hash + ":" + email);
            saveStore();
        }
        // Add the player to the in-memory/file player list
        auth_logic.Player p = new auth_logic.Player(username);
        auth_logic.PlayerData.players.add(p);
        auth_logic.PlayerData.saveToFile();
        return true;
    }

    /**
     * Returns true if the given username + password match a stored credential.
     */
    public static boolean authenticate(String username, String password) {
        if (isBlank(username) || isBlank(password)) return false;
        String key = username.toLowerCase(Locale.ROOT);
        synchronized (store) {
            String record = store.getProperty(key);
            if (record == null) return false;
            String[] parts = record.split(":", 3);
            if (parts.length < 2) return false;
            String salt = parts[0];
            String storedHash = parts[1];
            return storedHash.equals(hash(password, salt));
        }
    }

    /** Returns true if a user with that username already exists. */
    public static boolean userExists(String username) {
        if (isBlank(username)) return false;
        synchronized (store) {
            return store.containsKey(username.toLowerCase(Locale.ROOT));
        }
    }

    // ── Internal helpers ────────────────────────────────────────────────────

    private static void loadStore() {
        Path path = Paths.get(USER_FILE);
        if (!Files.exists(path)) return;
        try (InputStream in = Files.newInputStream(path)) {
            store.load(in);
        } catch (IOException e) {
            LOG.warning("Could not load user database: " + e.getMessage());
        }
    }

    private static void saveStore() {
        try (OutputStream out = Files.newOutputStream(Paths.get(USER_FILE))) {
            store.store(out, "OMG User Credentials – do not edit manually");
        } catch (IOException e) {
            LOG.severe("Could not save user database: " + e.getMessage());
        }
    }

    private static String generateSalt() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static String hash(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update((salt + password).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 unavailable", e);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
