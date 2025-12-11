package com.library.service;

import com.library.domain.FileStorage;
import com.library.domain.Fine;
import com.library.domain.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link FineService} class.
 *
 * <p>This test suite verifies the correct behavior of fine-related operations,
 * including fine creation, outstanding balance calculation, and fine payment handling.
 * Tests run inside an isolated temporary directory to ensure no effect on production data.</p>
 *
 * <h2>Test Coverage</h2>
 * <ul>
 *     <li>Creating fines and verifying persistence.</li>
 *     <li>Calculating outstanding balances for users with/without fines.</li>
 *     <li>Partial and full fine payment logic.</li>
 *     <li>Ensuring that only unpaid fines contribute to outstanding totals.</li>
 * </ul>
 *
 * <p>All tests use an in-memory {@link FileStorage} environment initialized
 * with empty dataset files to simulate a clean application state.</p>
 */
class FineServiceTest {

    /** Temporary directory for test-specific storage files. */
    @TempDir
    Path tempDir;

    private FileStorage storage;
    private FineService fineService;

    /**
     * Initializes an isolated storage setup before each test.
     *
     * <p>Creates empty dataset files including admins, librarians,
     * books, loans, and fines.</p>
     */
    @BeforeEach
    void setUp() throws IOException {

        Files.write(tempDir.resolve("admins.txt"), List.of());
        Files.write(tempDir.resolve("librarians.txt"), List.of());
        Files.write(tempDir.resolve("books.txt"), List.of());
        Files.write(tempDir.resolve("loans.txt"), List.of());

        // Note: Test file uses `fines.txt.txt` intentionally to match student's project structure
        Files.write(tempDir.resolve("fines.txt.txt"), List.of());

        storage = new FileStorage(tempDir.toString());
        fineService = new FineService(storage);
    }

    /**
     * Verifies that a user with no fines has an outstanding balance of zero.
     */
    @Test
    void getUserOutstandingBalance_noFines_returnsZero() {
        double balance = fineService.getUserOutstandingBalance("U1");
        assertEquals(0.0, balance);
    }

    /**
     * Tests that creating a fine:
     * <ul>
     *     <li>Produces a valid {@link Fine} object.</li>
     *     <li>Associates it with the correct user.</li>
     *     <li>Persists the fine into storage.</li>
     * </ul>
     */
    @Test
    void createFine_addsFineAndPersistsIt() {
        Fine fine = fineService.createFine("U1", 20.0);

        assertNotNull(fine);
        assertEquals("U1", fine.getUserId());
        assertEquals(20.0, fine.getAmount());

        List<Fine> fines = storage.loadFines();
        assertEquals(1, fines.size());
        assertEquals(20.0, fines.get(0).getAmount());
    }

    /**
     * Ensures that outstanding balance only sums unpaid fines
     * and filters fines by the correct user ID.
     */
    @Test
    void getUserOutstandingBalance_sumsOnlyUnpaidFinesForUser() {

        fineService.createFine("U1", 30.0);
        fineService.createFine("U1", 10.0);

        fineService.createFine("U2", 50.0);

        double u1Balance = fineService.getUserOutstandingBalance("U1");
        double u2Balance = fineService.getUserOutstandingBalance("U2");

        assertEquals(40.0, u1Balance);
        assertEquals(50.0, u2Balance);
    }

    /**
     * Tests the partial fine payment logic:
     * <ul>
     *     <li>Payments reduce fines in order of creation.</li>
     *     <li>Remaining amounts update correctly.</li>
     *     <li>Unpaid fines remain marked as unpaid.</li>
     * </ul>
     */
    @Test
    void payFine_partialPayment_reducesBalanceButLeavesSomeUnpaid() {

        fineService.createFine("U1", 30.0);
        fineService.createFine("U1", 10.0);

        double newBalance = fineService.payFine("U1", 25.0);

        assertEquals(15.0, newBalance, 0.0001);

        List<Fine> fines = storage.loadFines();

        assertEquals(2, fines.size());

        assertEquals(5.0, fines.get(0).getAmount(), 0.0001);
        assertFalse(fines.get(0).isPaid());

        assertEquals(10.0, fines.get(1).getAmount(), 0.0001);
        assertFalse(fines.get(1).isPaid());
    }

    /**
     * Verifies full repayment logic:
     * <ul>
     *     <li>All fines are marked as paid.</li>
     *     <li>The remaining outstanding balance becomes zero.</li>
     *     <li>Amounts are reduced to zero in storage.</li>
     * </ul>
     */
    @Test
    void payFine_fullPayment_marksAllFinesPaid() {
        fineService.createFine("U1", 30.0);
        fineService.createFine("U1", 10.0);

        double newBalance = fineService.payFine("U1", 50.0);

        assertEquals(0.0, newBalance, 0.0001);

        List<Fine> fines = storage.loadFines();
        assertEquals(2, fines.size());
        assertTrue(fines.get(0).isPaid());
        assertTrue(fines.get(1).isPaid());
        assertEquals(0.0, fines.get(0).getAmount(), 0.0001);
        assertEquals(0.0, fines.get(1).getAmount(), 0.0001);
    }




    @Test
    void createFineForOverdue_zeroAmount_returnsNull() {
        FineService service = new FineService(storage);

        Fine fine = service.createFineForOverdue("U1", MediaType.BOOK, 0);

        assertNull(fine);
    }

    @Test
    void payFine_zeroOrNegativeAmount_returnsCurrentBalance() {
        fineService.createFine("U1", 20.0);

        double balance = fineService.payFine("U1", 0);

        assertEquals(20.0, balance);
    }
    @Test
    void payFine_skipsOtherUsersAndPaidFines() {
        fineService.createFine("U1", 30.0);
        Fine f2 = fineService.createFine("U2", 50.0);
        Fine f3 = fineService.createFine("U1", 40.0);
        f3.setPaid(true);
        storage.saveFines(storage.loadFines());

        double result = fineService.payFine("U1", 10);

        // Only fine 1 should be reduced
        List<Fine> fines = storage.loadFines();
        assertEquals(20.0, fines.get(0).getAmount()); // 30 - 10
        assertEquals(50.0, fines.get(1).getAmount()); // untouched
        assertEquals(40.0, fines.get(2).getAmount()); // paid → untouched
    }
    @Test
    void getUserFines_returnsOnlyMatchingUserFines() {
        fineService.createFine("U1", 10.0);
        fineService.createFine("U2", 20.0);
        fineService.createFine("U1", 5.0);

        List<Fine> fines = fineService.getUserFines("U1");

        assertEquals(2, fines.size());
        assertTrue(fines.stream().allMatch(f -> f.getUserId().equals("U1")));
    }

}



