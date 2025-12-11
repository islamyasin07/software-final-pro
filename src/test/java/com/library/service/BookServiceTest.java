package com.library.service;

import com.library.domain.Book;
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
 * Unit tests for the {@link BookService} class.
 *
 * <p>This test suite verifies all core behaviors related to book
 * management, including adding books, preventing duplicate ISBNs,
 * and searching by title, author, and ISBN.</p>
 *
 * <p>Each test uses a temporary directory provided by {@link TempDir}
 * to ensure isolated, file-based persistence without affecting
 * any real system data.</p>
 *
 * @author Maram
 * @version 1.0
 */
class BookServiceTest {

    /**
     * Temporary directory automatically created by JUnit
     * to hold mock data files during the tests.
     */
    @TempDir
    Path tempDir;

    /** Service under test. */
    private BookService bookService;

    /** Storage object used for reading/writing book data. */
    private FileStorage storage;

    /**
     * Initializes the test environment by preparing empty
     * storage files for books and administrators, then creating
     * a fresh {@link BookService} instance.
     *
     * @throws IOException if temporary files cannot be written
     */
    @BeforeEach
    void setUp() throws IOException {

        Path adminsFile = tempDir.resolve("admins.txt");
        Files.write(adminsFile, List.of());

        Path booksFile = tempDir.resolve("books.txt");
        Files.write(booksFile, List.of());

        storage = new FileStorage(tempDir.toString());
        bookService = new BookService(storage);
    }

    /**
     * Verifies that adding a book:
     * <ul>
     *     <li>Creates a valid {@link Book} object</li>
     *     <li>Persists the book into books.txt</li>
     *     <li>Stores correct title, author, and ISBN</li>
     * </ul>
     */
    @Test
    void addBook_addsNewBookAndPersistsIt() throws IOException {
        Book book = bookService.addBook("Harry Potter", "Rowling", "111");

        assertNotNull(book);
        assertEquals("Harry Potter", book.getTitle());
        assertEquals("Rowling", book.getAuthor());
        assertEquals("111", book.getIsbn());

        List<String> lines = Files.readAllLines(tempDir.resolve("books.txt"));
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).contains("Harry Potter"));
    }

    /**
     * Ensures that the service prevents adding another book
     * with a duplicate ISBN and returns {@code null}.
     */
    @Test
    void addBook_withDuplicateIsbn_returnsNullAndDoesNotAdd() throws IOException {

        Book first = bookService.addBook("Book1", "Author1", "123");
        assertNotNull(first);

        Book second = bookService.addBook("Book2", "Author2", "123");
        assertNull(second, "Second book with same ISBN should not be added");

        List<String> lines = Files.readAllLines(tempDir.resolve("books.txt"));
        assertEquals(1, lines.size());
    }

    /**
     * Verifies that searching by title (case-insensitive)
     * returns all books whose titles contain the search term.
     */
    @Test
    void searchByTitle_findsMatchingBooks() {
        bookService.addBook("Java Programming", "A", "1");
        bookService.addBook("Advanced Java", "B", "2");
        bookService.addBook("Python Basics", "C", "3");

        List<Book> result = bookService.searchByTitle("java");

        assertEquals(2, result.size());
    }

    /**
     * Verifies that searching by author (case-insensitive)
     * returns all books whose author names match the search term.
     */
    @Test
    void searchByAuthor_findsMatchingBooks() {
        bookService.addBook("Book1", "Aseel", "10");
        bookService.addBook("Book2", "ASEEL Q", "11");
        bookService.addBook("Book3", "Someone Else", "12");

        List<Book> result = bookService.searchByAuthor("aseel");

        assertEquals(2, result.size());
    }

    /**
     * Ensures search by ISBN returns:
     * <ul>
     *     <li>The correct single matching book</li>
     *     <li>{@code null} when searching for a non-existent ISBN</li>
     * </ul>
     */
    @Test
    void searchByIsbn_returnsSingleBookOrNull() {
        bookService.addBook("Book1", "A", "111");
        bookService.addBook("Book2", "B", "222");

        Book found = bookService.searchByIsbn("222");
        assertNotNull(found);
        assertEquals("Book2", found.getTitle());

        Book notFound = bookService.searchByIsbn("999");
        assertNull(notFound);
    }
}
