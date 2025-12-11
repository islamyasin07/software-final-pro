package com.library.domain;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all file-based persistence for the library system.
 * <p>
 * The FileStorage class is responsible for loading and saving all library data
 * including admins, librarians, users, books, loans, and fines.
 * Data is stored in plain text files inside a base directory.
 * </p>
 *
 * <p>Each record is stored using semicolon-separated format.</p>
 *
 * @author Maram
 * @version 1.0
 */
public class FileStorage {

    /**
     * The base directory where all data files are stored.
     */
    private final Path baseDir;

    /**
     * Creates a new FileStorage instance.
     *
     * @param baseDirName the directory where the text files will be stored
     */
    public FileStorage(String baseDirName) {
        this.baseDir = Paths.get(baseDirName);
    }

    /* ============================
       Private helper file path methods
       ============================ */

    /**
     * @return path to admins.txt file
     */
    private Path adminsFile() {
        return baseDir.resolve("admins.txt");
    }

    /**
     * @return path to librarians.txt file
     */
    private Path librariansFile() {
        return baseDir.resolve("librarians.txt");
    }

    /**
     * @return path to users.txt file
     */
    private Path usersFile() {
        return baseDir.resolve("users.txt");
    }

    /**
     * @return path to books.txt file
     */
    private Path booksFile() {
        return baseDir.resolve("books.txt");
    }

    /**
     * @return path to loans.txt file
     */
    private Path loansFile() {
        return baseDir.resolve("loans.txt");
    }

    /**
     * @return path to fines.txt file
     */
    private Path finesFile() {
        return baseDir.resolve("fines.txt");
    }


    /* ============================
       Admins
       ============================ */

    /**
     * Loads all admins from admins.txt.
     *
     * @return list of Admin objects
     */
    public List<Admin> loadAdmins() {
        List<Admin> admins = new ArrayList<>();
        try {
            if (!Files.exists(adminsFile())) {
                return admins;
            }
            for (String line : Files.readAllLines(adminsFile())) {
                if (line.isBlank()) continue;
                String[] parts = line.split(";");
                if (parts.length < 4) continue;
                String id = parts[0];
                String name = parts[1];
                String email = parts[2];
                String password = parts[3];
                admins.add(new Admin(id, name, email, password));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load admins", e);
        }
        return admins;
    }

    /**
     * Saves a list of admins to admins.txt.
     *
     * @param admins the admin list to save
     */
    public void saveAdmins(List<Admin> admins) {
        List<String> lines = new ArrayList<>();
        for (Admin a : admins) {
            String line = String.join(";",
                    a.getId(),
                    a.getName(),
                    a.getEmail(),
                    a.getPassword()
            );
            lines.add(line);
        }
        try {
            Files.createDirectories(baseDir);
            Files.write(adminsFile(), lines,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save admins", e);
        }
    }


    /* ============================
       Librarians
       ============================ */

    /**
     * Loads all librarians from librarians.txt.
     *
     * @return list of Librarian objects
     */
    public List<Librarian> loadLibrarians() {
        List<Librarian> librarians = new ArrayList<>();
        try {
            if (!Files.exists(librariansFile())) {
                return librarians;
            }
            for (String line : Files.readAllLines(librariansFile())) {
                if (line.isBlank()) continue;
                String[] parts = line.split(";");
                if (parts.length < 4) continue;
                String id = parts[0];
                String name = parts[1];
                String email = parts[2];
                String password = parts[3];
                librarians.add(new Librarian(id, name, email, password));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load librarians", e);
        }
        return librarians;
    }


    /* ============================
       Users
       ============================ */

    /**
     * Loads all users from users.txt.
     *
     * @return list of User objects
     */
    public List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        try {
            if (!Files.exists(usersFile())) {
                return users;
            }
            for (String line : Files.readAllLines(usersFile())) {
                if (line.isBlank()) continue;
                String[] parts = line.split(";");
                if (parts.length < 4) continue;

                String id = parts[0];
                String name = parts[1];
                String email = parts[2];
                String password = parts[3];

                users.add(new User(id, name, email, password));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load users.txt", e);
        }
        return users;
    }

    /**
     * Saves all users to users.txt.
     *
     * @param users list of users to save
     */
    public void saveUsers(List<User> users) {
        List<String> lines = new ArrayList<>();
        for (User u : users) {
            String line = String.join(";",
                    u.getId(),
                    u.getName(),
                    u.getEmail(),
                    u.getPassword()
            );
            lines.add(line);
        }
        try {
            Files.createDirectories(baseDir);
            Files.write(usersFile(), lines,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save users.txt", e);
        }
    }


    /* ============================
       Books
       ============================ */

    /**
     * Loads all books from books.txt.
     *
     * @return list of Book objects
     */
    public List<Book> loadBooks() {
        List<Book> books = new ArrayList<>();
        try {
            if (!Files.exists(booksFile())) {
                return books;
            }
            for (String line : Files.readAllLines(booksFile())) {
                if (line.isBlank()) continue;
                String[] parts = line.split(";");
                if (parts.length < 5) continue;
                String id = parts[0];
                String title = parts[1];
                String author = parts[2];
                String isbn = parts[3];
                boolean borrowed = Boolean.parseBoolean(parts[4]);
                books.add(new Book(id, title, author, isbn, borrowed));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load books", e);
        }
        return books;
    }

    /**
     * Saves all books to books.txt.
     *
     * @param books list of books to save
     */
    public void saveBooks(List<Book> books) {
        List<String> lines = new ArrayList<>();
        for (Book b : books) {
            String line = String.join(";",
                    b.getId(),
                    b.getTitle(),
                    b.getAuthor(),
                    b.getIsbn(),
                    Boolean.toString(b.isBorrowed())
            );
            lines.add(line);
        }
        try {
            Files.createDirectories(baseDir);
            Files.write(booksFile(), lines,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save books", e);
        }
    }


    /* ============================
       Loans
       ============================ */

    /**
     * Loads all loans from loans.txt.
     *
     * @return list of Loan objects
     */
    public List<Loan> loadLoans() {
        List<Loan> loans = new ArrayList<>();
        try {
            if (!Files.exists(loansFile())) {
                return loans;
            }
            for (String line : Files.readAllLines(loansFile())) {
                if (line.isBlank()) continue;

                String[] parts = line.split(";", -1);
                if (parts.length < 6) continue;

                String id = parts[0];
                String userId = parts[1];
                String bookId = parts[2];
                LocalDate borrowDate = LocalDate.parse(parts[3]);
                LocalDate dueDate = LocalDate.parse(parts[4]);
                LocalDate returnDate = parts[5].isEmpty() ? null : LocalDate.parse(parts[5]);

                MediaType mediaType = MediaType.BOOK;
                if (parts.length >= 7 && !parts[6].isBlank()) {
                    mediaType = MediaType.valueOf(parts[6]);
                }

                loans.add(new Loan(id, userId, bookId, borrowDate, dueDate, returnDate, mediaType));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load loans", e);
        }
        return loans;
    }

    /**
     * Saves all loans to loans.txt.
     *
     * @param loans list of Loan objects to save
     */
    public void saveLoans(List<Loan> loans) {
        List<String> lines = new ArrayList<>();
        for (Loan loan : loans) {
            String returnDateStr = (loan.getReturnDate() == null)
                    ? ""
                    : loan.getReturnDate().toString();

            String line = String.join(";",
                    loan.getId(),
                    loan.getUserId(),
                    loan.getBookId(),
                    loan.getBorrowDate().toString(),
                    loan.getDueDate().toString(),
                    returnDateStr,
                    loan.getMediaType().name()
            );
            lines.add(line);
        }
        try {
            Files.createDirectories(baseDir);
            Files.write(loansFile(), lines,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save loans", e);
        }
    }


    /* ============================
       Fines
       ============================ */

    /**
     * Loads all fines from fines.txt.
     *
     * @return list of Fine objects
     */
    public List<Fine> loadFines() {
        List<Fine> fines = new ArrayList<>();
        try {
            if (!Files.exists(finesFile())) {
                return fines;
            }
            for (String line : Files.readAllLines(finesFile())) {
                if (line.isBlank()) continue;
                String[] parts = line.split(";");
                if (parts.length < 4) continue;

                String id = parts[0];
                String userId = parts[1];
                double amount = Double.parseDouble(parts[2]);
                boolean paid = Boolean.parseBoolean(parts[3]);

                fines.add(new Fine(id, userId, amount, paid));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load fines.txt", e);
        }
        return fines;
    }

    /**
     * Saves all fines to fines.txt.
     *
     * @param fines list of fines to save
     */
    public void saveFines(List<Fine> fines) {
        List<String> lines = new ArrayList<>();
        for (Fine fine : fines) {
            String line = String.join(";",
                    fine.getId(),
                    fine.getUserId(),
                    Double.toString(fine.getAmount()),
                    Boolean.toString(fine.isPaid())
            );
            lines.add(line);
        }
        try {
            Files.createDirectories(baseDir);
            Files.write(finesFile(), lines,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save fines.txt", e);
        }
    }

    /**
     * Saves librarians list to librarians.txt.
     *
     * @param librarians list of librarians
     */
    public void saveLibrarians(List<Librarian> librarians) {
        List<String> lines = new ArrayList<>();
        for (Librarian l : librarians) {
            String line = String.join(";",
                    l.getId(),
                    l.getName(),
                    l.getEmail(),
                    l.getPassword()
            );
            lines.add(line);
        }
        try {
            Files.createDirectories(baseDir);
            Files.write(librariansFile(), lines,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save librarians", e);
        }
    }
}
