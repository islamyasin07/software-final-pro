package com.library.service;

import com.library.domain.Book;
import com.library.domain.FileStorage;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides operations for managing books in the library system.
 * <p>
 * This service interacts with {@link FileStorage} to load, save, search,
 * and add books. It ensures that ISBNs remain unique and supports
 * multiple search mechanisms (title, author, ISBN).
 * </p>
 *
 * <p>
 * All book data is persisted in a text-based storage format.
 * </p>
 *
 * @author Maram
 * @version 1.0
 */
public class BookService {

    /**
     * Storage handler used to load and save book data.
     */
    private final FileStorage storage;

    /**
     * Creates a new BookService instance.
     *
     * @param storage the storage backend used for book persistence
     */
    public BookService(FileStorage storage) {
        this.storage = storage;
    }

    /**
     * Adds a new book to the library.
     * <p>
     * ISBNs must be unique. If a book already exists with the same ISBN,
     * the method returns {@code null}.
     * </p>
     *
     * @param title  the book's title
     * @param author the name of the author
     * @param isbn   the book's ISBN identifier
     * @return the newly added {@link Book}, or {@code null} if a duplicate ISBN exists
     */
    public Book addBook(String title, String author, String isbn) {
        List<Book> books = storage.loadBooks();

        for (Book b : books) {
            if (b.getIsbn().equalsIgnoreCase(isbn)) {
                return null; // Duplicate ISBN, do not add
            }
        }

        String id = "B" + (books.size() + 1);

        Book newBook = new Book(id, title, author, isbn, false);
        books.add(newBook);

        storage.saveBooks(books);

        return newBook;
    }

    /**
     * Searches for books whose titles contain the given keyword.
     *
     * @param titlePart a partial or full title keyword
     * @return list of books matching the search term
     */
    public List<Book> searchByTitle(String titlePart) {
        List<Book> result = new ArrayList<>();
        String keyword = titlePart.toLowerCase();

        for (Book b : storage.loadBooks()) {
            if (b.getTitle().toLowerCase().contains(keyword)) {
                result.add(b);
            }
        }
        return result;
    }

    /**
     * Searches for books based on the author's name.
     *
     * @param authorPart a partial or full author name keyword
     * @return list of books whose author names contain the keyword
     */
    public List<Book> searchByAuthor(String authorPart) {
        List<Book> result = new ArrayList<>();
        String keyword = authorPart.toLowerCase();

        for (Book b : storage.loadBooks()) {
            if (b.getAuthor().toLowerCase().contains(keyword)) {
                result.add(b);
            }
        }
        return result;
    }

    /**
     * Searches for a single book using its ISBN.
     *
     * @param isbn the ISBN to search for
     * @return the matching {@link Book}, or {@code null} if not found
     */
    public Book searchByIsbn(String isbn) {
        for (Book b : storage.loadBooks()) {
            if (b.getIsbn().equalsIgnoreCase(isbn)) {
                return b;
            }
        }
        return null;
    }

    /**
     * Retrieves all books currently stored in the system.
     *
     * @return list of all books
     */
    public List<Book> getAllBooks() {
        return storage.loadBooks();
    }
}
