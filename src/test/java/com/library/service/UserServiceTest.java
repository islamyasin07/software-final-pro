package com.library.service;

import com.library.domain.FileStorage;
import com.library.domain.Fine;
import com.library.domain.Loan;
import com.library.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link UserService}.
 *
 * <p>This test class verifies all core functionalities of user management, including:
 * registration, login, and the unregistering workflow that checks active loans
 * and outstanding fines before allowing removal.</p>
 *
 * <p>All tests run using a temporary file-based database to ensure isolation
 * and consistent behavior.</p>
 *
 * @author Asil
 * @version 1.0
 */
class UserServiceTest {

    /** Temporary directory that stores isolated test data for each test execution. */
    @TempDir
    Path tempDir;

    private FileStorage storage;
    private UserService userService;
    private LoanService loanService;
    private FineService fineService;

    // Strong password that satisfies the validation rules in UserService.register
    private static final String VALID_PASSWORD = "Abcd1234!";

    /**
     * Initializes a clean test environment before each test.
     *
     * <p>All required storage files are created empty in a temporary directory.
     * Service instances are initialized using this isolated storage.</p>
     *
     * @throws IOException if writing to the temporary directory fails
     */
    @BeforeEach
    void setUp() throws IOException {
        Files.write(tempDir.resolve("admins.txt"), Collections.emptyList());
        Files.write(tempDir.resolve("librarians.txt"), Collections.emptyList());
        Files.write(tempDir.resolve("books.txt"), Collections.emptyList());
        Files.write(tempDir.resolve("users.txt"), Collections.emptyList());
        Files.write(tempDir.resolve("loans.txt"), Collections.emptyList());
        Files.write(tempDir.resolve("fines.txt"), Collections.emptyList());

        storage = new FileStorage(tempDir.toString());
        loanService = new LoanService(storage);
        fineService = new FineService(storage);
        userService = new UserService(storage);
    }

    /**
     * Tests that the {@link UserService#register(String, String, String)} method
     * correctly creates a new user and persists it to file storage.
     */
    @Test
    void register_createsNewUserAndPersistsToFile() {
        User u = userService.register("Aseel", "aseel@example.com", VALID_PASSWORD);

        assertNotNull(u);
        assertEquals("U1", u.getId());
        assertEquals("aseel@example.com", u.getEmail());

        List<User> fromFile = storage.loadUsers();
        assertEquals(1, fromFile.size());
        assertEquals("aseel@example.com", fromFile.get(0).getEmail());
    }

    /**
     * Ensures that registering a user with an email that already exists
     * results in an {@link IllegalArgumentException}.
     */
    @Test
    void register_withDuplicateEmail_throwsException() {
        storage.saveUsers(List.of(
                new User("U1", "Old User", "aseel@example.com", "oldpwd")
        ));

        assertThrows(IllegalArgumentException.class,
                () -> userService.register("Aseel", "aseel@example.com", VALID_PASSWORD));
    }

    /**
     * Tests both successful and failed login attempts.
     *
     * <p>Verifies correct handling of:</p>
     * <ul>
     *   <li>Valid credentials</li>
     *   <li>Incorrect password</li>
     *   <li>Unknown email</li>
     * </ul>
     */
    @Test
    void login_returnsUserOnCorrectCredentials_elseNull() {
        storage.saveUsers(List.of(
                new User("U1", "Dana", "dana@example.com", "1234")
        ));

        User ok = userService.login("dana@example.com", "1234");
        assertNotNull(ok);
        assertEquals("U1", ok.getId());

        User wrongPass = userService.login("dana@example.com", "xxxx");
        assertNull(wrongPass);

        User unknown = userService.login("nope@example.com", "1234");
        assertNull(unknown);
    }

    /**
     * Tests that a user with no active loans and no unpaid fines
     * is successfully removed from storage.
     */
    @Test
    void unregisterUser_whenNoLoansAndNoFines_removesUserFromFile() {
        storage.saveUsers(List.of(
                new User("U1", "User1", "u1@example.com", "pwd")
        ));
        storage.saveLoans(Collections.emptyList());
        storage.saveFines(Collections.emptyList());

        userService.unregisterUser("U1", loanService, fineService);

        List<User> remaining = storage.loadUsers();
        assertTrue(remaining.isEmpty(), "User should be removed from users.txt");
    }

    /**
     * Ensures that unregistering a user with an active loan throws an exception
     * and does not remove the user.
     */
    @Test
    void unregisterUser_whenUserHasActiveLoans_throwsIllegalState() {
        storage.saveUsers(List.of(
                new User("U1", "User1", "u1@example.com", "pwd")
        ));

        LocalDate today = LocalDate.now();
        storage.saveLoans(List.of(
                new Loan("L1", "U1", "B1",
                        today.minusDays(1),
                        today.plusDays(7),
                        null)
        ));

        storage.saveFines(Collections.emptyList());

        assertThrows(IllegalStateException.class,
                () -> userService.unregisterUser("U1", loanService, fineService));

        assertEquals(1, storage.loadUsers().size());
    }

    /**
     * Ensures that unregistering a user with unpaid fines is not allowed
     * and results in an exception.
     */
    @Test
    void unregisterUser_whenUserHasUnpaidFines_throwsIllegalState() {
        storage.saveUsers(List.of(
                new User("U1", "User1", "u1@example.com", "pwd")
        ));
        storage.saveLoans(Collections.emptyList());

        storage.saveFines(List.of(
                new Fine("F1", "U1", 20.0, false)
        ));

        assertThrows(IllegalStateException.class,
                () -> userService.unregisterUser("U1", loanService, fineService));

        assertEquals(1, storage.loadUsers().size());
    }

    /**
     * Tests that attempting to unregister a non-existent user
     * results in an {@link IllegalArgumentException}.
     */
    @Test
    void unregisterUser_whenUserDoesNotExist_throwsIllegalArgument() {
        storage.saveUsers(Collections.emptyList());
        storage.saveLoans(Collections.emptyList());
        storage.saveFines(Collections.emptyList());

        assertThrows(IllegalArgumentException.class,
                () -> userService.unregisterUser("UNKNOWN", loanService, fineService));
    }

    @Test
    void constructor_withEmailService_initializesStorageCorrectly() {
        EmailService dummyEmail = new EmailService("a", "b");
        UserService us = new UserService(storage, dummyEmail);

        assertNotNull(us);
    }

    @Test
    void isUserLoggedIn_whenNoUserLoggedIn_returnsFalse() {
        assertFalse(userService.isUserLoggedIn());
    }

    @Test
    void logout_clearsCurrentUser() {
        userService.register("Lana", "lana@mail.com", VALID_PASSWORD);
        userService.login("lana@mail.com", VALID_PASSWORD);

        userService.logout();

        assertFalse(userService.isUserLoggedIn());
    }

    @Test
    void findById_whenUserNotFound_returnsNull() {
        storage.saveUsers(List.of());
        User found = userService.findById("U999");

        assertNull(found);
    }

    @Test
    void unregisterUser_whenRemovingLoggedInUser_logsOut() {
        User u = userService.register("Lana", "lana@mail.com", VALID_PASSWORD);
        userService.login("lana@mail.com", VALID_PASSWORD);

        storage.saveLoans(List.of());
        storage.saveFines(List.of());

        userService.unregisterUser("U1", loanService, fineService);

        assertFalse(userService.isUserLoggedIn());
    }

    @Test
    void unregisterUser_whenServicesAreNull_stillRemovesUser() {
        storage.saveUsers(List.of(
                new User("U1", "User1", "u1@mail.com", "p")
        ));

        userService.unregisterUser("U1", null, null);

        assertTrue(storage.loadUsers().isEmpty());
    }

}
