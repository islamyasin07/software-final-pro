package com.library.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BookTest {

    @Test
    void constructor_setsFieldsAndGettersWork() {
        Book book = new Book("B1", "Clean Code", "Robert C. Martin", "1234567890", false);

        assertEquals("B1", book.getId());
        assertEquals("Clean Code", book.getTitle());
        assertEquals("Robert C. Martin", book.getAuthor());
        assertEquals("1234567890", book.getIsbn());
        assertFalse(book.isBorrowed());
    }

    @Test
    void setBorrowed_changesBorrowedFlag() {
        Book book = new Book("B1", "Clean Code", "Robert C. Martin", "1234567890", false);

        book.setBorrowed(true);
        assertTrue(book.isBorrowed());

        book.setBorrowed(false);
        assertFalse(book.isBorrowed());
    }
}
