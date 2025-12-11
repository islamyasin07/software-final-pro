package com.library.service;

import com.library.domain.FileStorage;
import com.library.domain.Loan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.library.domain.MediaType;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link BorrowingService} class.
 *
 * <p>This test suite verifies borrowing logic, ensuring that the system
 * correctly enforces rules regarding overdue loans, unpaid fines, and
 * allowed media types. All tests operate in an isolated temporary
 * directory using file-based storage through {@link FileStorage}.</p>
 *
 * <p>Each test initializes a clean environment to guarantee deterministic
 * results without affecting actual system data.</p>
 *
 * @author Maram
 * @version 1.0
 */
class BorrowingServiceTest {

    /** Temporary directory for storing mock DB files during tests. */
    @TempDir
    Path tempDir;

    private FileStorage storage;
    private LoanService loanService;
    private FineService fineService;
    private BorrowingService borrowingService;

    private static final String USER_ID = "U1";
    private static final String BOOK_ID = "B1";

    /**
     * Prepares the test environment by creating empty storage files
     * and initializing all required services using a temporary directory.
     *
     * @throws IOException if any of the mock DB files cannot be created
     */
    @BeforeEach
    void setUp() throws IOException {

        Files.write(tempDir.resolve("admins.txt"), List.of());
        Files.write(tempDir.resolve("librarians.txt"), List.of());
        Files.write(tempDir.resolve("users.txt"), List.of());
        Files.write(tempDir.resolve("loans.txt"), List.of());
        Files.write(tempDir.resolve("fines.txt"), List.of());

        Files.write(
                tempDir.resolve("books.txt"),
                List.of("B1;Test Book;Author;111;false")
        );

        storage = new FileStorage(tempDir.toString());
        loanService = new LoanService(storage);
        fineService = new FineService(storage);
        borrowingService = new BorrowingService(loanService, fineService);
    }

    /**
     * Ensures that a user with no overdue loans and no unpaid fines
     * can borrow a book successfully. A new {@link Loan} record
     * should be created and stored.
     */
    @Test
    void borrowBook_whenUserIsClean_createsLoan() {
        Loan loan = borrowingService.borrowBook(USER_ID, BOOK_ID);

        assertNotNull(loan);
        assertEquals(USER_ID, loan.getUserId());
        assertEquals(BOOK_ID, loan.getBookId());

        List<Loan> loans = storage.loadLoans();
        assertEquals(1, loans.size());
        assertEquals(USER_ID, loans.get(0).getUserId());
    }

    /**
     * Verifies that the borrowing operation is blocked when the user
     * has at least one overdue loan. The service must throw
     * {@link IllegalStateException} with a descriptive message.
     */
    @Test
    void borrowBook_whenUserHasOverdueLoans_throwsException() {
        LocalDate today = LocalDate.now();

        List<Loan> loans = new ArrayList<>();
        loans.add(new Loan(
                "L1",
                USER_ID,
                BOOK_ID,
                today.minusDays(40),
                today.minusDays(10),
                null
        ));
        storage.saveLoans(loans);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> borrowingService.borrowBook(USER_ID, BOOK_ID)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("overdue"),
                "Message should mention overdue loans");
    }

    /**
     * Ensures that borrowing is blocked when the user has unpaid fines.
     * A descriptive {@link IllegalStateException} must be thrown.
     */
    @Test
    void borrowBook_whenUserHasUnpaidFines_throwsException() {
        fineService.createFine(USER_ID, 20.0);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> borrowingService.borrowBook(USER_ID, BOOK_ID)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("unpaid"),
                "Message should mention unpaid fines");
    }

    /**
     * Verifies successful borrowing of CD media using
     * {@link BorrowingService#borrowCd(String, String)}.
     * The resulting {@link Loan} must have {@link MediaType#CD}.
     */
    @Test
    void borrowCd_whenUserHasNoFines_succeedsWithCdMediaType() {

        Loan loan = borrowingService.borrowCd("U1", "CD1");

        assertNotNull(loan);
        assertEquals("U1", loan.getUserId());
        assertEquals("CD1", loan.getBookId());
        assertEquals(MediaType.CD, loan.getMediaType());
    }

    @Test
    void borrowCd_whenUserHasUnpaidFines_throwsException() {
        fineService.createFine(USER_ID, 30.0); // Fine > 0

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> borrowingService.borrowCd(USER_ID, "CD1")
        );

        assertTrue(ex.getMessage().toLowerCase().contains("unpaid"));
    }
    @Test
    void borrowCd_whenUserHasOverdueLoans_throwsException() {
        LocalDate today = LocalDate.now();

        storage.saveLoans(List.of(
                new Loan("L1", USER_ID, BOOK_ID,
                        today.minusDays(30),   // borrow date
                        today.minusDays(5),    // due date in past = overdue
                        null)
        ));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> borrowingService.borrowCd(USER_ID, "CD1")
        );

        assertTrue(ex.getMessage().toLowerCase().contains("overdue"));
    }


}
