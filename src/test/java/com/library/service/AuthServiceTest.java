package com.library.service;

import com.library.domain.Admin;
import com.library.domain.FileStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link AuthService} class.
 *
 * <p>This test suite verifies authentication logic for admins,
 * librarians, and users. All authentication scenarios are validated,
 * including:</p>
 *
 * <ul>
 *     <li>Successful admin login.</li>
 *     <li>Failed login due to incorrect password.</li>
 *     <li>Unknown email handling.</li>
 *     <li>Correct logout behavior.</li>
 *     <li>Librarian login and role switching.</li>
 *     <li>User login and ensuring only one role is active at a time.</li>
 * </ul>
 *
 * <p>Each test uses a {@code @TempDir} temporary folder to isolate
 * file storage and ensure no real application data is touched.</p>
 *
 * @author Maram
 * @version 1.0
 */
class AuthServiceTest {

    /**
     * Temporary directory automatically created by JUnit
     * to hold the dynamically generated authentication files.
     */
    @TempDir
    Path tempDir;

    /**
     * The service under test. Created fresh before each test.
     */
    private AuthService authService;

    /**
     * Prepares the test environment by creating mock data files for:
     * <ul>
     *     <li>Admins</li>
     *     <li>Librarians</li>
     *     <li>Users</li>
     * </ul>
     *
     * <p>The method writes realistic authentication lines to the files,
     * then initializes a new {@link AuthService} instance using the
     * temporary storage directory.</p>
     *
     * @throws IOException if file writing fails
     */
    @BeforeEach
    void setUp() throws IOException {

        Path adminsFile = tempDir.resolve("admins.txt");
        List<String> adminsLines = List.of(
                "1;Admin One;admin@example.com;1234",
                "2;Another Admin;admin2@example.com;abcd"
        );
        Files.write(adminsFile, adminsLines);

        Path librariansFile = tempDir.resolve("librarians.txt");
        List<String> librarianLines = List.of(
                "L1;Lib One;librarian@example.com;libpwd"
        );
        Files.write(librariansFile, librarianLines);

        Path usersFile = tempDir.resolve("users.txt");
        List<String> userLines = List.of(
                "U1;User One;user@example.com;userpwd"
        );
        Files.write(usersFile, userLines);

        FileStorage storage = new FileStorage(tempDir.toString());
        authService = new AuthService(storage);
    }

    /**
     * Verifies that a valid admin email and password
     * successfully authenticate an administrator and mark them as logged in.
     */
    @Test
    void login_withValidCredentials_returnsAdminAndMarksLoggedIn() {
        Admin admin = authService.login("admin@example.com", "1234");

        assertNotNull(admin, "Login should return an Admin for valid credentials");
        assertEquals("Admin One", admin.getName());
        assertTrue(authService.isAdminLoggedIn(), "Admin should be marked as logged in");
    }

    /**
     * Ensures incorrect passwords result in failed login
     * and no admin session is created.
     */
    @Test
    void login_withInvalidPassword_returnsNullAndNotLoggedIn() {
        Admin admin = authService.login("admin@example.com", "wrong");

        assertNull(admin, "Login with wrong password should return null");
        assertFalse(authService.isAdminLoggedIn(), "No admin should be logged in");
    }

    /**
     * Ensures that unknown emails do not match any admin record.
     */
    @Test
    void login_withUnknownEmail_returnsNull() {
        Admin admin = authService.login("unknown@example.com", "1234");

        assertNull(admin, "Login with unknown email should return null");
        assertFalse(authService.isAdminLoggedIn());
    }

    /**
     * Verifies that logging out clears the current logged-in admin.
     */
    @Test
    void logout_clearsCurrentAdmin() {
        authService.login("admin@example.com", "1234");
        assertTrue(authService.isAdminLoggedIn());

        authService.logout();

        assertFalse(authService.isAdminLoggedIn(), "After logout no admin should be logged in");
    }

    /**
     * Tests librarian login and ensures that logging in as a librarian:
     * <ul>
     *     <li>Authenticates correctly</li>
     *     <li>Sets librarian as current role</li>
     *     <li>Clears admin and user login states</li>
     * </ul>
     */
    @Test
    void librarianLogin_setsCurrentLibrarianAndClearsOthers() {
        assertNull(authService.getCurrentLibrarian());

        var librarian = authService.loginLibrarian("librarian@example.com", "libpwd");

        assertNotNull(librarian);
        assertEquals("Lib One", librarian.getName());
        assertTrue(authService.isLibrarianLoggedIn());
        assertFalse(authService.isAdminLoggedIn());
        assertFalse(authService.isUserLoggedIn());
    }

    /**
     * Ensures correct login for a normal system user,
     * and verifies that logging in as a user resets
     * admin and librarian sessions.
     */
    @Test
    void userLogin_setsCurrentUserAndResetsOtherRoles() {
        var user = authService.loginUser("user@example.com", "userpwd");

        assertNotNull(user);
        assertEquals("User One", user.getName());

        authService.login("admin@example.com", "1234");
        assertTrue(authService.isAdminLoggedIn());

        authService.loginUser("user@example.com", "userpwd");
        assertTrue(authService.isUserLoggedIn());
        assertFalse(authService.isAdminLoggedIn());
        assertFalse(authService.isLibrarianLoggedIn());
    }
    @Test
    void isAdminLoggedIn_whenNoAdminLoggedIn_returnsFalse() {
        assertFalse(authService.isAdminLoggedIn());
    }
    @Test
    void loginLibrarian_withWrongPassword_returnsNull() {
        var lib = authService.loginLibrarian("librarian@example.com", "wrong");
        assertNull(lib);
        assertFalse(authService.isLibrarianLoggedIn());
    }
    @Test
    void loginLibrarian_withUnknownEmail_returnsNull() {
        var lib = authService.loginLibrarian("unknown@example.com", "libpwd");
        assertNull(lib);
        assertFalse(authService.isLibrarianLoggedIn());
    }
    @Test
    void isLibrarianLoggedIn_whenNoneLoggedIn_returnsFalse() {
        assertFalse(authService.isLibrarianLoggedIn());
    }
    @Test
    void loginUser_withWrongPassword_returnsNull() {
        var user = authService.loginUser("user@example.com", "wrong");
        assertNull(user);
        assertFalse(authService.isUserLoggedIn());
    }
    @Test
    void loginUser_withUnknownEmail_returnsNull() {
        var user = authService.loginUser("unknown@example.com", "userpwd");
        assertNull(user);
        assertFalse(authService.isUserLoggedIn());
    }
    @Test
    void isUserLoggedIn_whenNoUserLoggedIn_returnsFalse() {
        assertFalse(authService.isUserLoggedIn());
    }

}
