package com.library.presentation;

import com.library.domain.*;
import com.library.service.*;

import java.util.List;
import java.util.Scanner;

/**
 * Console-based menu interface for the library management system.
 * <p>
 * This class provides all user interaction through text-based input/output.
 * It supports admin, librarian, and user operations such as login,
 * searching books, borrowing media, paying fines, and managing records.
 * </p>
 *
 * <p>
 * All operations rely on the underlying service layer:
 * {@link AuthService}, {@link UserService}, {@link BookService},
 * {@link LoanService}, {@link FineService}, {@link BorrowingService},
 * and {@link ReminderService}.
 * </p>
 *
 * <p><strong>Note:</strong> This class does not enforce business rules;
 * it only handles user input and delegates logic to the services.</p>
 *
 * @author Asil
 * @version 1.0
 */

public class ConsoleMenu {

    private final AuthService authService;
    private final UserService userService;
    private final BookService bookService;
    private final LoanService loanService;
    private final FineService fineService;
    private final BorrowingService borrowingService;
    private final ReminderService reminderService;

    private final Scanner scanner = new Scanner(System.in);

    /**
     * Creates a new ConsoleMenu with all required services.
     *
     * @param authService     authentication and role service
     * @param userService     user registration and login service
     * @param bookService     book management service
     * @param loanService     loan handling service
     * @param fineService     fine calculation and payment service
     * @param reminderService overdue email reminder service
     */

    public ConsoleMenu(AuthService authService,
                       UserService userService,
                       BookService bookService,
                       LoanService loanService,
                       FineService fineService,
                       ReminderService reminderService) {

        this.authService = authService;
        this.userService = userService;
        this.bookService = bookService;
        this.loanService = loanService;
        this.fineService = fineService;
        this.reminderService = reminderService;

        this.borrowingService = new BorrowingService(loanService, fineService);
    }

    /**
     * Runs the main loop of the console menu, showing the main options
     * and dispatching user actions until the user chooses to exit.
     */

    public void run() {
        boolean running = true;

        while (running) {
            printMainMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    handleAdminLogin();
                    break;
                case "2":
                    handleLibrarianLogin();
                    break;
                case "3":
                    handleUserSignup();
                    break;
                case "4":
                    handleUserLogin();
                    break;
                case "5":
                    handleLogout();
                    break;
                case "6":
                    handleAddBook();
                    break;
                case "7":
                    handleSearchBook();
                    break;
                case "8":
                    handleBorrowBook();
                    break;
                case "9":
                    handleViewOverdueLoans();
                    break;
                case "10":
                    handlePayFine();
                    break;
                case "11":
                    handleSendOverdueReminders();
                    break;
                case "12":
                    handleUnregisterUser();
                    break;
                case "13":
                    handleViewMyLoans();
                    break;
                case "14":
                    System.out.println("Exiting... Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice, please try again.");
            }
        }
    }


    /**
     * Prints the main menu options for the library system.
     * <p>
     * This method only displays the menu; it does not read input or
     * trigger any actions.
     * </p>
     */

    private void printMainMenu() {
        System.out.println("\n=== Library System ===");
        System.out.println("1. Admin login");
        System.out.println("2. Librarian login");
        System.out.println("3. User sign up");
        System.out.println("4. User login");
        System.out.println("5. Logout");
        System.out.println("6. Add book (admin only)");
        System.out.println("7. Search book");
        System.out.println("8. Borrow book/CD (user only)");
        System.out.println("9. View overdue loans (librarian only)");
        System.out.println("10. Pay fine");
        System.out.println("11. Send overdue reminders");
        System.out.println("12. Unregister user (admin only)");
        System.out.println("13. View my loans (user only)");
        System.out.println("14. Exit");
        System.out.print("Choose option: ");
    }

    /**
     * Handles admin login interaction.
     * <p>
     * Prompts the user for email and password, verifies credentials
     * through {@link AuthService}, and logs in the admin if valid.
     * </p>
     * <p>
     * If an admin is already logged in, the method simply notifies the user.
     * </p>
     */

    private void handleAdminLogin() {
        if (authService.isAdminLoggedIn()) {
            System.out.println("An admin is already logged in: "
                    + authService.getCurrentAdmin().getName());
            return;
        }

        System.out.print("Enter admin email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        Admin admin = authService.login(email, password);
        if (admin != null) {
            System.out.println("Admin login successful. Welcome, " + admin.getName() + "!");
        } else {
            System.out.println("Invalid admin credentials. Login failed.");
        }
    }

    /**
     * Handles librarian login interaction.
     * <p>
     * Prompts the librarian for credentials and attempts to authenticate
     * using {@link AuthService#loginLibrarian(String, String)}.
     * </p>
     * <p>
     * If a librarian is already logged in, the method notifies the user.
     * </p>
     */

    private void handleLibrarianLogin() {
        if (authService.isLibrarianLoggedIn()) {
            System.out.println("A librarian is already logged in: "
                    + authService.getCurrentLibrarian().getName());
            return;
        }

        System.out.print("Enter librarian email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        Librarian librarian = authService.loginLibrarian(email, password);
        if (librarian != null) {
            System.out.println("Librarian login successful. Welcome, " + librarian.getName() + "!");
        } else {
            System.out.println("Invalid librarian credentials. Login failed.");
        }
    }

    /**
     * Logs out the currently logged-in admin or librarian.
     * <p>
     * If no authenticated staff member exists, the method simply displays a message.
     * </p>
     */

    private void handleLogout() {
        if (!authService.isAdminLoggedIn() && !authService.isLibrarianLoggedIn()) {
            System.out.println("No admin or librarian is currently logged in.");
            return;
        }
        authService.logout();
        System.out.println("Logout successful.");
    }


    /**
     * Handles a new user registration.
     * <p>
     * Prompts the user for name, email, and password. Delegates the actual
     * registration process to {@link UserService#register(String, String, String)}.
     * </p>
     * <p>
     * Displays success or error messages based on registration outcome.
     * </p>
     */

    private void handleUserSignup() {
        System.out.println("\n=== User Sign Up ===");

        System.out.print("Enter your name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter your email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        try {
            User user = userService.register(name, email, password);
            System.out.println("User registered successfully with ID: " + user.getId());
        } catch (IllegalArgumentException e) {
            System.out.println("Could not register user: " + e.getMessage());
        }
    }



    /**
     * Handles user login interaction.
     * <p>
     * Prompts for email and password, verifies credentials via
     * {@link UserService#login(String, String)}, and updates authentication state
     * using {@link AuthService#loginUser(String, String)}.
     * </p>
     * <p>
     * If a user is already logged in, the method simply notifies the user.
     * </p>
     */

    private void handleUserLogin() {
        if (authService.isUserLoggedIn()) {
            System.out.println("Already logged in as: " + authService.getCurrentUser().getName());
            return;
        }

        System.out.println("\n=== User Login ===");
        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        var user = userService.login(email, password);
        if (user != null) {
            authService.logout(); // clear other logins
            authService.loginUser(email, password);
            System.out.println("Welcome, " + user.getName() + "!");
        } else {
            System.out.println("Invalid email or password.");
        }
    }

    /**
     * Handles adding a new book to the system.
     * <p>
     * Only admins may perform this operation. Prompts for title, author,
     * and ISBN, then delegates book creation to {@link BookService#addBook(String, String, String)}.
     * </p>
     * <p>
     * Displays success or failure messages depending on whether the book already exists.
     * </p>
     */

    private void handleAddBook() {
        if (!authService.isAdminLoggedIn()) {
            System.out.println("You must login as admin to add books.");
            return;
        }

        System.out.print("Enter book title: ");
        String title = scanner.nextLine().trim();

        System.out.print("Enter book author: ");
        String author = scanner.nextLine().trim();

        System.out.print("Enter book ISBN: ");
        String isbn = scanner.nextLine().trim();

        Book book = bookService.addBook(title, author, isbn);
        if (book == null) {
            System.out.println("A book with this ISBN already exists. No book added.");
        } else {
            System.out.println("Book added successfully with ID: " + book.getId());
        }
    }

    /**
     * Handles book searching by title, author, or ISBN.
     * <p>
     * Prompts the user for a search mode and delegates search work to
     * the appropriate {@link BookService} method.
     * </p>
     */

    private void handleSearchBook() {
        System.out.println("\nSearch by:");
        System.out.println("1. Title");
        System.out.println("2. Author");
        System.out.println("3. ISBN");
        System.out.print("Choose option: ");

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1":
                System.out.print("Enter part of title: ");
                searchAndPrintBooks(bookService.searchByTitle(scanner.nextLine().trim()));
                break;
            case "2":
                System.out.print("Enter part of author name: ");
                searchAndPrintBooks(bookService.searchByAuthor(scanner.nextLine().trim()));
                break;
            case "3":
                System.out.print("Enter ISBN: ");
                Book book = bookService.searchByIsbn(scanner.nextLine().trim());
                if (book != null) {
                    printBook(book);
                } else {
                    System.out.println("No book found with that ISBN.");
                }
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    /**
     * Prints a list of books to the console.
     *
     * @param books the list of matching books
     */

    private void searchAndPrintBooks(List<Book> books) {
        if (books.isEmpty()) {
            System.out.println("No matching books found.");
            return;
        }
        System.out.println("\n=== Search Results ===");
        for (Book b : books) {
            printBook(b);
        }
    }

    /**
     * Prints a single book's details in a formatted line.
     *
     * @param b the book to print
     */

    private void printBook(Book b) {
        System.out.println("- ID: " + b.getId()
                + " | Title: " + b.getTitle()
                + " | Author: " + b.getAuthor()
                + " | ISBN: " + b.getIsbn()
                + " | Borrowed: " + (b.isBorrowed() ? "Yes" : "No"));
    }


    /**
     * Handles the borrowing process for books and CDs.
     * <p>
     * Ensures a user is logged in, then prompts the user to choose a media type.
     * Depending on the choice, delegates borrowing to:
     * <ul>
     *     <li>{@link BorrowingService#borrowBook(String, String)}</li>
     *     <li>{@link BorrowingService#borrowCd(String, String)}</li>
     * </ul>
     * </p>
     *
     * <p>
     * Displays success messages with loan details, or error messages if borrowing fails.
     * </p>
     */

    private void handleBorrowBook() {
        System.out.println("\n=== Borrow Item (Book / CD) ===");

        if (!authService.isUserLoggedIn()) {
            System.out.println("You must be logged in as a user to borrow.");
            return;
        }

        String userId = authService.getCurrentUser().getId();

        System.out.println("Choose media type:");
        System.out.println("1. Book");
        System.out.println("2. CD");
        System.out.print("Enter choice (1 or 2): ");
        String typeChoice = scanner.nextLine().trim();

        try {
            Loan loan;

            if ("1".equals(typeChoice)) {
                System.out.print("Enter book ID to borrow (e.g., B1): ");
                String bookId = scanner.nextLine().trim();

                loan = borrowingService.borrowBook(userId, bookId);

            } else if ("2".equals(typeChoice)) {
                System.out.print("Enter CD ID to borrow (e.g., CD1): ");
                String cdId = scanner.nextLine().trim();

                loan = borrowingService.borrowCd(userId, cdId);

            } else {
                System.out.println("Invalid media type choice.");
                return;
            }

            System.out.println("Item borrowed successfully with loan ID: " + loan.getId());
            System.out.println("Borrow date: " + loan.getBorrowDate()
                    + ", Due date: " + loan.getDueDate());

        } catch (IllegalStateException e) {
            System.out.println("Could not borrow item: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Displays all overdue loans.
     * <p>
     * This operation is restricted to librarians only. Retrieves all overdue loans
     * using {@link LoanService#getOverdueLoans()} and prints them to the console.
     * </p>
     * <p>
     * If no overdue loans exist, a message is displayed instead.
     * </p>
     */

    private void handleViewOverdueLoans() {
        if (!authService.isLibrarianLoggedIn()) {
            System.out.println("You must login as librarian to view overdue loans.");
            return;
        }

        List<Loan> overdue = loanService.getOverdueLoans();
        if (overdue.isEmpty()) {
            System.out.println("No overdue loans.");
            return;
        }

        System.out.println("\n=== Overdue Loans ===");
        for (Loan loan : overdue) {
            System.out.println("- Loan ID: " + loan.getId()
                    + " | User ID: " + loan.getUserId()
                    + " | Book ID: " + loan.getBookId()
                    + " | Borrow date: " + loan.getBorrowDate()
                    + " | Due date: " + loan.getDueDate());
        }
    }

    /**
     * Handles fine payment for logged-in users.
     * <p>
     * Prompts the user for payment amount, processes it through
     * {@link FineService#payFine(String, double)}, and displays updated balances.
     * </p>
     * <p>
     * If the user has no outstanding fines or is not logged in, a message is shown.
     * </p>
     */

    private void handlePayFine() {
        System.out.println("\n=== Pay Fine ===");

        if (!authService.isUserLoggedIn()) {
            System.out.println("You must be logged in as a user to pay your fines.txt.");
            return;
        }

        String userId = authService.getCurrentUser().getId();

        double balance = fineService.getUserOutstandingBalance(userId);
        if (balance <= 0) {
            System.out.println("You have no outstanding fines.txt.");
            return;
        }

        System.out.println("Your current outstanding fines.txt = " + balance + " NIS");
        System.out.print("Enter amount to pay: ");

        String input = scanner.nextLine().trim();
        double amount;
        try {
            amount = Double.parseDouble(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount.");
            return;
        }

        double newBalance = fineService.payFine(userId, amount);
        System.out.println("Payment processed. Remaining balance = " + newBalance + " NIS");

        if (newBalance == 0) {
            System.out.println("All fines.txt are fully paid. You have regained borrowing rights.");
        }
    }


    /**
     * Sends email reminders for all overdue loans.
     * <p>
     * This operation is restricted to admins only. It triggers the
     * {@link ReminderService#sendOverdueReminders()} method, which identifies
     * overdue loans and dispatches reminder emails.
     * </p>
     * <p>
     * Displays the number of reminders sent or appropriate error messages
     * if something fails.
     * </p>
     */

    private void handleSendOverdueReminders() {
        if (!authService.isAdminLoggedIn()) {
            System.out.println("You must login as admin to send overdue reminders.");
            return;
        }

        System.out.println("\n=== Send Overdue Reminders ===");
        try {
            int count = reminderService.sendOverdueReminders();
            if (count == 0) {
                System.out.println("No overdue loans found. No emails were sent.");
            } else {
                System.out.println("Successfully sent " + count + " reminder email(s).");
            }
        } catch (Exception e) {
            System.out.println("Failed to send reminders: " + e.getMessage());
        }
    }

    /**
     * Displays all loans associated with the currently logged-in user.
     * <p>
     * If the user is not logged in or has no loans, appropriate messages are shown.
     * Otherwise, each loan's key information (ID, book/media ID, dates) is printed.
     * </p>
     */

    private void handleViewMyLoans() {
        if (!authService.isUserLoggedIn()) {
            System.out.println("You must log in as a user to view your loans.");
            return;
        }

        var user = authService.getCurrentUser();
        List<Loan> loans = loanService.getLoansForUser(user.getId());

        if (loans.isEmpty()) {
            System.out.println("You have no loans.");
            return;
        }

        System.out.println("\n=== Your Loans ===");
        for (Loan loan : loans) {
            System.out.println("- Loan ID: " + loan.getId() +
                    " | Book ID: " + loan.getBookId() +
                    " | Borrow: " + loan.getBorrowDate() +
                    " | Due: " + loan.getDueDate() +
                    " | Returned: " +
                    (loan.getReturnDate() == null ? "No" : loan.getReturnDate()));
        }
    }

    /**
     * Allows an admin to unregister (delete) a user account.
     * <p>
     * This operation is restricted to admins only. It prompts for a user ID,
     * then delegates the deletion logic to
     * {@link UserService#unregisterUser(String, LoanService, FineService)}.
     * </p>
     * <p>
     * Displays success messages or error messages depending on whether the
     * user has unpaid fines or outstanding loans.
     * </p>
     */

    private void handleUnregisterUser() {
        System.out.println("\n=== Unregister User ===");

        if (!authService.isAdminLoggedIn()) {
            System.out.println("You must login as admin to unregister a user.");
            return;
        }

        System.out.print("Enter user ID to unregister (e.g., U1): ");
        String userId = scanner.nextLine().trim();

        try {
            userService.unregisterUser(userId, loanService, fineService);
            System.out.println("User " + userId + " was unregistered successfully.");
        } catch (IllegalStateException e) {
            System.out.println("Could not unregister user: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
