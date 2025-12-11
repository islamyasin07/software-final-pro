package com.library.service;

import com.library.domain.Book;
import com.library.domain.FileStorage;
import com.library.domain.Loan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.library.domain.MediaType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link LoanService}.
 *
 * <p>This test class verifies all borrowing, returning, and
 * loan-status operations, including:
 * <ul>
 *   <li>Borrowing books and CDs</li>
 *   <li>Preventing borrowing already borrowed items</li>
 *   <li>Returning items and updating availability</li>
 *   <li>Detecting overdue and active loans</li>
 *   <li>Handling invalid loan or book IDs</li>
 * </ul>
 *
 * <p>All tests use a temporary folder to simulate the storage layer.</p>
 */
class LoanServiceTest {

    /** Temporary directory used for file-based storage during tests. */
    @TempDir
    Path tempDir;

    private FileStorage storage;
    private LoanService loanService;

    /**
     * Prepares a fresh storage directory and loads one sample book
     * before each test.
     *
     * @throws IOException if writing initial test files fails
     */
    @BeforeEach
    void setUp() throws IOException {

        Files.write(tempDir.resolve("admins.txt"), List.of());
        Files.write(tempDir.resolve("librarians.txt"), List.of());
        Files.write(tempDir.resolve("loans.txt"), List.of());
        Files.write(tempDir.resolve("fines.txt.txt"), List.of());

        Files.write(
                tempDir.resolve("books.txt"),
                List.of("B1;Harry Potter;Rowling;111;false")
        );

        storage = new FileStorage(tempDir.toString());
        loanService = new LoanService(storage);
    }

    /**
     * Verifies that borrowing a CD:
     * <ul>
     *   <li>Creates a loan record</li>
     *   <li>Assigns media type = CD</li>
     *   <li>Sets due date to 7 days from borrow date</li>
     * </ul>
     */
    @Test
    void borrowCd_createsLoanWith7DayDueDate_andMediaTypeCd() {
        Loan loan = loanService.borrowCd("U1", "CD1");

        assertNotNull(loan);
        assertEquals("U1", loan.getUserId());
        assertEquals("CD1", loan.getBookId());
        assertEquals(loan.getBorrowDate().plusDays(7), loan.getDueDate());
        assertEquals(MediaType.CD, loan.getMediaType());
    }

    /**
     * Tests that borrowing a book:
     * <ul>
     *   <li>Marks the book as borrowed</li>
     *   <li>Creates a loan entry</li>
     *   <li>Sets due date to 28 days</li>
     * </ul>
     */
    @Test
    void borrowBook_marksBookAsBorrowed_andCreatesLoanFor28Days() {
        Loan loan = loanService.borrowBook("U1", "B1");

        assertNotNull(loan);
        assertEquals("U1", loan.getUserId());
        assertEquals("B1", loan.getBookId());
        assertEquals(loan.getBorrowDate().plusDays(28), loan.getDueDate());

        List<Book> booksAfter = storage.loadBooks();
        assertEquals(1, booksAfter.size());
        assertTrue(booksAfter.get(0).isBorrowed());

        List<Loan> loans = storage.loadLoans();
        assertEquals(1, loans.size());
        assertEquals("U1", loans.get(0).getUserId());
    }

    /**
     * Ensures that borrowing a book that is already borrowed
     * throws an IllegalStateException.
     */
    @Test
    void borrowBook_onAlreadyBorrowedBook_throwsException() {
        loanService.borrowBook("U1", "B1");

        assertThrows(IllegalStateException.class,
                () -> loanService.borrowBook("U2", "B1"));
    }

    /**
     * Tests that returning a book:
     * <ul>
     *   <li>Sets its return date</li>
     *   <li>Marks the book as available again</li>
     * </ul>
     */
    @Test
    void returnBook_setsReturnDate_andMarksBookAvailable() {

        Loan loan = loanService.borrowBook("U1", "B1");
        String loanId = loan.getId();

        loanService.returnBook(loanId);

        List<Loan> loans = storage.loadLoans();
        assertEquals(1, loans.size());
        assertTrue(loans.get(0).isReturned());

        List<Book> books = storage.loadBooks();
        assertEquals(1, books.size());
        assertFalse(books.get(0).isBorrowed());
    }

    /**
     * Verifies that getOverdueLoans returns only:
     * <ul>
     *   <li>Loans past their due date</li>
     *   <li>Loans that are not returned</li>
     * </ul>
     *
     * @throws IOException if writing test loan file fails
     */
    @Test
    void getOverdueLoans_returnsOnlyLoansPastDueDate_andNotReturned() throws IOException {

        List<Loan> loans = new ArrayList<>();
        LocalDate today = LocalDate.now();

        loans.add(new Loan("L1", "U1", "B1",
                today.minusDays(30),
                today.minusDays(2),
                null));

        loans.add(new Loan("L2", "U2", "B1",
                today.minusDays(5),
                today.plusDays(5),
                null));

        loans.add(new Loan("L3", "U3", "B1",
                today.minusDays(40),
                today.minusDays(10),
                today.minusDays(5))); // returned

        storage.saveLoans(loans);

        List<Loan> overdue = loanService.getOverdueLoans();

        assertEquals(1, overdue.size());
        assertEquals("L1", overdue.get(0).getId());
    }

    /**
     * Ensures that borrowing using a non-existing book ID throws an exception.
     */
    @Test
    void borrowBook_whenBookIdNotFound_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> loanService.borrowBook("U1", "B-DOES-NOT-EXIST"));
    }

    /**
     * Ensures that returning an unknown loan ID throws an exception.
     */
    @Test
    void returnBook_whenLoanIdUnknown_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> loanService.returnBook("L-404"));
    }

    /**
     * Tests overdue-loan detection for a specific user.
     *
     * @throws IOException if saving test loans fails
     */
    @Test
    void hasOverdueLoans_checksOnlyUnreturnedLoansForUser() throws IOException {
        LocalDate today = LocalDate.now();

        List<Loan> loans = new ArrayList<>();

        loans.add(new Loan("L1", "U1", "B1",
                today.minusDays(20), today.minusDays(1), null));

        loans.add(new Loan("L2", "U1", "B2",
                today.minusDays(30), today.minusDays(5), today.minusDays(2)));

        loans.add(new Loan("L3", "U2", "B3",
                today.minusDays(25), today.minusDays(3), null));

        loans.add(new Loan("L4", "U1", "B4",
                today.minusDays(2), today.plusDays(5), null));

        loans.add(new Loan("L5", "U3", "B5",
                today.minusDays(2), today.plusDays(10), null));

        storage.saveLoans(loans);

        assertTrue(loanService.hasOverdueLoans("U1"));
        assertTrue(loanService.hasOverdueLoans("U2"));
        assertFalse(loanService.hasOverdueLoans("U3"));
    }

    /**
     * Tests detection of active (unreturned) loans for a user.
     *
     * @throws IOException if storage saving fails
     */
    @Test
    void hasActiveLoans_detectsUnreturnedLoansForUser() throws IOException {
        LocalDate today = LocalDate.now();
        List<Loan> loans = new ArrayList<>();

        loans.add(new Loan("L1", "U1", "B1",
                today.minusDays(10), today.minusDays(2), today.minusDays(1)));

        loans.add(new Loan("L2", "U1", "B2",
                today.minusDays(3), today.plusDays(10), null));

        loans.add(new Loan("L3", "U2", "B3",
                today.minusDays(5), today.plusDays(2), null));

        storage.saveLoans(loans);

        assertTrue(loanService.hasActiveLoans("U1"));
        assertTrue(loanService.hasActiveLoans("U2"));

        loanService.returnBook("L3");
        assertFalse(loanService.hasActiveLoans("U2"));
    }
}
