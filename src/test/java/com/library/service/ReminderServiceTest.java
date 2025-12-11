package com.library.service;

import com.library.domain.FileStorage;
import com.library.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link ReminderService}.
 *
 * <p>This test class verifies the behavior of the reminder system responsible
 * for sending overdue loan notifications to users. It uses temporary file storage
 * along with a mock email service implementation to ensure deterministic and isolated testing.</p>
 *
 * @author Maram
 * @version 1.0
 */
class ReminderServiceTest {

    /** Temporary directory used to isolate file-based storage for each test. */
    @TempDir
    Path tempDir;

    private FileStorage storage;
    private LoanService loanService;
    private CapturingEmailService emailService;
    private ReminderService reminderService;

    /**
     * A mock implementation of {@link EmailService} that captures outgoing
     * emails instead of sending them.
     *
     * <p>This allows the tests to verify the content and number of outgoing
     * reminder messages without relying on an SMTP server.</p>
     */
    static class CapturingEmailService extends EmailService {

        /** List of captured recipient email addresses. */
        List<String> toList = new ArrayList<>();

        /** List of captured email subjects. */
        List<String> subjectList = new ArrayList<>();

        /** List of captured email message bodies. */
        List<String> bodyList = new ArrayList<>();

        /**
         * Constructs a mock email service using a dummy sender account.
         */
        CapturingEmailService() {
            super("test@example.com", "dummy-password");
        }

        /**
         * Overrides the default send method to capture outgoing email data.
         *
         * @param to      recipient email address
         * @param subject email subject
         * @param body    email message body
         */
        @Override
        public void sendEmail(String to, String subject, String body) {
            toList.add(to);
            subjectList.add(subject);
            bodyList.add(body);
        }
    }

    /**
     * Sets up a clean test environment before each test case.
     *
     * <p>All database files are rewritten in a temporary directory.
     * Custom mock services are initialized for isolation and control.</p>
     *
     * @throws IOException if file initialization fails
     */
    @BeforeEach
    void setUp() throws IOException {
        Files.write(tempDir.resolve("admins.txt"), List.of());
        Files.write(tempDir.resolve("librarians.txt"), List.of());
        Files.write(tempDir.resolve("books.txt"), List.of());
        Files.write(tempDir.resolve("fines.txt.txt"), List.of());
        Files.write(tempDir.resolve("loans.txt"), List.of());

        storage = new FileStorage(tempDir.toString());
        loanService = new LoanService(storage);
        emailService = new CapturingEmailService();

        UserService fakeUsers = new FakeUserService();

        reminderService = new ReminderService(
                loanService,
                fakeUsers,
                emailService
        );
    }

    /**
     * Tests that overdue reminders are sent correctly when overdue loans exist.
     *
     * @throws IOException if loan file writing fails
     */
    @Test
    void sendOverdueReminders_sendsOneEmailPerOverdueLoan() throws IOException {
        LocalDate today = LocalDate.now();

        List<String> loansLines = List.of(
                "L1;user1@example.com;B1;" +
                        today.minusDays(40) + ";" +
                        today.minusDays(5) + ";" +
                        "",
                "L2;user2@example.com;B2;" +
                        today.minusDays(5) + ";" +
                        today.plusDays(10) + ";" +
                        ""
        );
        Files.write(tempDir.resolve("loans.txt"), loansLines);

        int count = reminderService.sendOverdueReminders();

        assertEquals(1, count);
        assertEquals(1, emailService.toList.size());
        assertEquals("user1@example.com@example.com", emailService.toList.get(0));
    }

    /**
     * Tests that no reminders are sent if no overdue loans exist.
     *
     * @throws IOException if file writing fails
     */
    @Test
    void sendOverdueReminders_whenNoOverdueLoans_sendsNoEmails() throws IOException {

        LocalDate today = LocalDate.now();
        List<String> loansLines = List.of(
                "L1;user1@example.com;B1;" + today.minusDays(5) + ";" + today.plusDays(10) + ";"
        );

        Files.write(tempDir.resolve("loans.txt"), loansLines);

        int count = reminderService.sendOverdueReminders();

        assertEquals(0, count);
        assertEquals(0, emailService.toList.size());
    }

    /**
     * A fake user service used to supply user details during tests
     * without accessing real storage.
     */
    static class FakeUserService extends UserService {
        /**
         * Creates a fake user service with no storage backend.
         */
        public FakeUserService() {
            super(null);
        }

        /**
         * Returns a fabricated user instance based on the requested user ID.
         *
         * @param userId the ID of the user being queried
         * @return a dummy {@link User} instance
         */
        @Override
        public User findById(String userId) {
            return new User(userId, "TestUser", userId + "@example.com", "pass");
        }
    }
    @Test
    void sendOverdueReminders_skipsLoansWhenUserNotFound() throws IOException {
        LocalDate today = LocalDate.now();


        List<String> loansLines = List.of(
                "L1;U99;B1;" +
                        today.minusDays(40) + ";" +
                        today.minusDays(5) + ";" +
                        ""
        );
        Files.write(tempDir.resolve("loans.txt"), loansLines);


        UserService userService = new UserService(null) {
            @Override
            public User findById(String userId) {
                return null;   // simulate NOT FOUND
            }
        };

        reminderService = new ReminderService(loanService, userService, emailService);

        int count = reminderService.sendOverdueReminders();

        assertEquals(0, count, "No reminders should be sent");
        assertEquals(0, emailService.toList.size(), "Email list must remain empty");
    }

}
