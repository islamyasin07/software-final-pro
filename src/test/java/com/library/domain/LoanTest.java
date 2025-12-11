package com.library.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class LoanTest {

    @Test
    void isReturned_returnsTrueWhenReturnDateIsNotNull() {
        Loan loan = new Loan(
                "L1",
                "U1",
                "B1",
                LocalDate.now().minusDays(5),
                LocalDate.now().plusDays(5),
                null
        );

        assertFalse(loan.isReturned());

        loan.markReturned(LocalDate.now());
        assertTrue(loan.isReturned());
    }

    @Test
    void isOverdue_returnsTrueOnlyWhenNotReturnedAndTodayAfterDueDate() {
        LocalDate borrowDate = LocalDate.now().minusDays(10);
        LocalDate dueDate = LocalDate.now().minusDays(3);

        Loan loan = new Loan("L1", "U1", "B1",
                borrowDate,
                dueDate,
                null
        );

        assertTrue(loan.isOverdue(LocalDate.now()));
    }

    @Test
    void isOverdue_returnsFalseWhenReturned() {
        LocalDate borrowDate = LocalDate.now().minusDays(10);
        LocalDate dueDate = LocalDate.now().minusDays(3);

        Loan loan = new Loan("L1", "U1", "B1",
                borrowDate,
                dueDate,
                LocalDate.now() // returned
        );

        assertFalse(loan.isOverdue(LocalDate.now()));
    }

    @Test
    void constructor_setsMediaTypeToBookForOldConstructor() {
        Loan loan = new Loan(
                "L1",
                "U1",
                "B1",
                LocalDate.now(),
                LocalDate.now().plusDays(7),
                null
        );

        assertEquals(MediaType.BOOK, loan.getMediaType());
    }

    @Test
    void constructor_acceptsMediaTypeForNewConstructor() {
        Loan loan = new Loan(
                "L2",
                "U1",
                "CD1",
                LocalDate.now(),
                LocalDate.now().plusDays(7),
                null,
                MediaType.CD
        );

        assertEquals(MediaType.CD, loan.getMediaType());
    }

    @Test
    void oldConstructor_defaultsMediaTypeToBook() {
        LocalDate borrow = LocalDate.of(2024, 1, 1);
        LocalDate due    = LocalDate.of(2024, 1, 10);

        Loan loan = new Loan("L1", "U1", "B1", borrow, due, null);

        assertEquals("L1", loan.getId());
        assertEquals("U1", loan.getUserId());
        assertEquals("B1", loan.getBookId());
        assertEquals(borrow, loan.getBorrowDate());
        assertEquals(due, loan.getDueDate());
        assertNull(loan.getReturnDate());
        assertEquals(MediaType.BOOK, loan.getMediaType());
    }

    @Test
    void newConstructor_usesPassedMediaTypeOrDefaultsWhenNull() {
        LocalDate borrow = LocalDate.of(2024, 1, 1);
        LocalDate due    = LocalDate.of(2024, 1, 10);

        Loan cdLoan = new Loan("L2", "U2", "C1", borrow, due, null, MediaType.CD);
        assertEquals(MediaType.CD, cdLoan.getMediaType());

        Loan nullTypeLoan = new Loan("L3", "U3", "B2", borrow, due, null, null);
        assertEquals(MediaType.BOOK, nullTypeLoan.getMediaType());
    }

    @Test
    void isReturned_and_markReturned_behaveCorrectly() {
        LocalDate borrow = LocalDate.of(2024, 1, 1);
        LocalDate due    = LocalDate.of(2024, 1, 10);
        Loan loan = new Loan("L1", "U1", "B1", borrow, due, null);

        assertFalse(loan.isReturned());

        LocalDate ret = LocalDate.of(2024, 1, 5);
        loan.markReturned(ret);

        assertTrue(loan.isReturned());
        assertEquals(ret, loan.getReturnDate());
    }

    @Test
    void isOverdue_trueOnlyWhenNotReturnedAndAfterDueDate() {
        LocalDate borrow = LocalDate.of(2024, 1, 1);
        LocalDate due    = LocalDate.of(2024, 1, 10);
        Loan loan = new Loan("L1", "U1", "B1", borrow, due, null);

        assertFalse(loan.isOverdue(LocalDate.of(2024, 1, 9)));
        assertFalse(loan.isOverdue(LocalDate.of(2024, 1, 10)));
        assertTrue(loan.isOverdue(LocalDate.of(2024, 1, 11)));

        loan.markReturned(LocalDate.of(2024, 1, 8));
        assertFalse(loan.isOverdue(LocalDate.of(2024, 1, 20)));
    }
}
