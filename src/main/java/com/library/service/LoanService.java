package com.library.service;

import com.library.domain.Book;
import com.library.domain.FileStorage;
import com.library.domain.Loan;
import com.library.domain.MediaType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for managing the borrowing, returning, and
 * tracking of book and CD loans in the library system.
 * <p>
 * This service interacts with {@link FileStorage} to persist loan data
 * and ensures borrowing rules such as:
 * <ul>
 *     <li>Items cannot be borrowed if already checked out</li>
 *     <li>Due dates depend on media type (Books = 28 days, CDs = 7 days)</li>
 *     <li>Returning a book resets its availability</li>
 *     <li>Overdue loans can be detected</li>
 * </ul>

 *
 * <p>
 * Supports both BOOK and CD loans using {@link MediaType}.
 * </p>
 *
 * @author Maram
 * @version 1.0
 */
public class LoanService {

    /**
     * Storage backend used to load and save books and loans.
     */
    private final FileStorage storage;

    /**
     * Creates a LoanService instance.
     *
     * @param storage the persistent file-based storage system
     */
    public LoanService(FileStorage storage) {
        this.storage = storage;
    }

    /**
     * Retrieves all loans belonging to a specific user.
     *
     * @param userId user ID
     * @return list of the user's loans
     */
    public List<Loan> getLoansForUser(String userId) {
        return storage.loadLoans().stream()
                .filter(l -> l.getUserId().equals(userId))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Borrows a book for a user.
     * <p>
     * Validates:
     * <ul>
     *     <li>The book exists</li>
     *     <li>The book is not already borrowed</li>
     * </ul>
     * Sets:
     * <ul>
     *     <li>Borrow date = today</li>
     *     <li>Due date = today + 28 days</li>
     * </ul>
     *
     * @param userId user who is borrowing
     * @param bookId ID of the book
     * @return the created {@link Loan}
     *
     * @throws IllegalArgumentException if the book ID does not exist
     * @throws IllegalStateException    if the book is already borrowed
     */
    public Loan borrowBook(String userId, String bookId) {

        List<Book> books = storage.loadBooks();

        Book target = null;
        for (Book b : books) {
            if (b.getId().equals(bookId)) {
                target = b;
                break;
            }
        }

        if (target == null) {
            throw new IllegalArgumentException("Book with id " + bookId + " not found");
        }

        if (target.isBorrowed()) {
            throw new IllegalStateException("Book is already borrowed");
        }

        target.setBorrowed(true);
        storage.saveBooks(books);

        List<Loan> loans = storage.loadLoans();
        String loanId = "L" + (loans.size() + 1);

        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = borrowDate.plusDays(28);

        Loan loan = new Loan(
                loanId,
                userId,
                bookId,
                borrowDate,
                dueDate,
                null,
                MediaType.BOOK

        );

        loans.add(loan);
        storage.saveLoans(loans);

        return loan;
    }

    /**
     * Returns a borrowed book.
     * <p>
     * Updates:
     * <ul>
     *     <li>Loan return date = today</li>
     *     <li>Book availability = true</li>
     * </ul>
     *
     * @param loanId ID of the loan to return
     *
     * @throws IllegalArgumentException if the loan does not exist
     */
    public void returnBook(String loanId) {
        List<Loan> loans = storage.loadLoans();
        Loan targetLoan = null;

        for (Loan loan : loans) {
            if (loan.getId().equals(loanId)) {
                targetLoan = loan;
                break;
            }
        }

        if (targetLoan == null) {
            throw new IllegalArgumentException("Loan with id " + loanId + " not found");
        }

        if (targetLoan.isReturned()) {
            return; // Already returned
        }

        targetLoan.markReturned(LocalDate.now());
        storage.saveLoans(loans);

        List<Book> books = storage.loadBooks();
        for (Book book : books) {
            if (book.getId().equals(targetLoan.getBookId())) {
                book.setBorrowed(false);
                break;
            }
        }
        storage.saveBooks(books);
    }

    /**
     * Returns a list of all overdue loans.
     *
     * @return list of overdue loans
     */
    public List<Loan> getOverdueLoans() {
        LocalDate today = LocalDate.now();
        List<Loan> loans = storage.loadLoans();
        List<Loan> overdue = new ArrayList<>();

        for (Loan loan : loans) {
            if (loan.isOverdue(today)) {
                overdue.add(loan);
            }
        }
        return overdue;
    }

    /**
     * Retrieves all loans in the system.
     *
     * @return list of every stored loan
     */
    public List<Loan> getAllLoans() {
        return storage.loadLoans();
    }

    /**
     * Checks whether a user has at least one overdue loan.
     *
     * @param userId user ID
     * @return true if the user has overdue items
     */
    public boolean hasOverdueLoans(String userId) {
        LocalDate today = LocalDate.now();
        List<Loan> loans = storage.loadLoans();

        for (Loan loan : loans) {

            if (!loan.getUserId().equals(userId)) {
                continue;
            }

            if (loan.getReturnDate() != null) {
                continue;
            }

            if (loan.getDueDate().isBefore(today)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks whether a user has any active (not returned) loans.
     *
     * @param userId user ID
     * @return true if the user currently holds items
     */
    public boolean hasActiveLoans(String userId) {
        List<Loan> loans = storage.loadLoans();

        for (Loan loan : loans) {
            if (!loan.getUserId().equals(userId)) {
                continue;
            }

            if (!loan.isReturned()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Creates a loan for a CD.
     * <p>
     * CD borrowing rules:
     * <ul>
     *     <li>Borrow period = 7 days</li>
     * </ul>
     *
     * @param userId user borrowing the CD
     * @param cdId   CD ID
     * @return the created CD {@link Loan}
     */
    public Loan borrowCd(String userId, String cdId) {

        List<Loan> loans = storage.loadLoans();
        String loanId = "L" + (loans.size() + 1);

        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = borrowDate.plusDays(7);

        Loan loan = new Loan(
                loanId,
                userId,
                cdId,
                borrowDate,
                dueDate,
                null,
                MediaType.CD
        );

        loans.add(loan);
        storage.saveLoans(loans);

        return loan;
    }
}
