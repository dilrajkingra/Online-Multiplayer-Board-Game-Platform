import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AuthService — registration, authentication, duplicate detection.
 * Runs in a temp directory so it never touches real credential files.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthServiceTest {

    private static final String TEST_USER  = "testuser_junit5";
    private static final String TEST_EMAIL = "junit5@example.com";
    private static final String TEST_PASS  = "SecurePass1!";

    /** Each test gets a fresh working directory so AuthService file is isolated. */
    private static Path origDir;

    @BeforeAll
    static void redirectToTemp() throws IOException {
        origDir = Paths.get(System.getProperty("user.dir"));
        Path tmpDir = Files.createTempDirectory("omg-auth-test");
        System.setProperty("user.dir", tmpDir.toString());
        // Delete any leftover credential file from the new dir (none should exist)
        Files.deleteIfExists(tmpDir.resolve("users.properties"));
        Files.deleteIfExists(tmpDir.resolve("playerdata.properties"));
    }

    @AfterAll
    static void restoreDir() {
        System.setProperty("user.dir", origDir.toString());
    }

    @Test
    @Order(1)
    void register_newUser_returnsTrue() {
        assertTrue(AuthService.register(TEST_USER, TEST_EMAIL, TEST_PASS),
                "First registration should succeed");
    }

    @Test
    @Order(2)
    void register_duplicateUsername_returnsFalse() {
        // Attempt to register the same username again
        assertFalse(AuthService.register(TEST_USER, "other@example.com", "AnotherPass1"),
                "Duplicate registration should be rejected");
    }

    @Test
    @Order(3)
    void authenticate_correctCredentials_returnsTrue() {
        assertTrue(AuthService.authenticate(TEST_USER, TEST_PASS),
                "Correct credentials should authenticate");
    }

    @Test
    @Order(4)
    void authenticate_wrongPassword_returnsFalse() {
        assertFalse(AuthService.authenticate(TEST_USER, "WrongPassword"),
                "Wrong password should fail authentication");
    }

    @Test
    @Order(5)
    void authenticate_unknownUser_returnsFalse() {
        assertFalse(AuthService.authenticate("nobody", "anything"),
                "Unknown user should fail authentication");
    }

    @Test
    @Order(6)
    void authenticate_caseInsensitiveUsername() {
        assertTrue(AuthService.authenticate(TEST_USER.toUpperCase(), TEST_PASS),
                "Username lookup should be case-insensitive");
    }

    @Test
    @Order(7)
    void userExists_knownUser_returnsTrue() {
        assertTrue(AuthService.userExists(TEST_USER));
    }

    @Test
    @Order(8)
    void userExists_unknownUser_returnsFalse() {
        assertFalse(AuthService.userExists("ghost_user_xyz"));
    }

    @Test
    @Order(9)
    void register_blankFields_returnsFalse() {
        assertFalse(AuthService.register("", TEST_EMAIL, TEST_PASS));
        assertFalse(AuthService.register("someuser", "", TEST_PASS));
        assertFalse(AuthService.register("someuser", TEST_EMAIL, ""));
    }
}
