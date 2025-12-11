package com.library.service;

import com.library.domain.FileStorage;
import com.library.domain.Fine;
import com.library.domain.FineCalculator;
import com.library.domain.MediaType;

import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for managing fines within the library system.
 * <p>
 * This includes:
 * <ul>
 *     <li>Fetching user fines</li>
 *     <li>Calculating overdue fines using {@link FineCalculator}</li>
 *     <li>Creating fines for overdue items</li>
 *     <li>Processing fine payments</li>
 *     <li>Checking whether a user still owes money</li>
 * </ul>

 *
 * <p>
 * All fine data is persisted through {@link FileStorage}.
 * </p>
 *
 * @author Maram
 * @version 1.0
 */
public class FineService {

    /**
     * Storage handler for loading and saving fine records.
     */
    private final FileStorage storage;

    /**
     * Strategy-based fine calculator used for computing overdue amounts.
     */
    private final FineCalculator fineCalculator;

    /**
     * Creates a FineService with a default {@link FineCalculator}.
     *
     * @param storage file-based storage backend
     */
    public FineService(FileStorage storage) {
        this(storage, new FineCalculator());
    }

    /**
     * Creates a FineService with a custom fine calculator.
     *
     * @param storage        file storage backend
     * @param fineCalculator strategy calculator for fines
     */
    public FineService(FileStorage storage, FineCalculator fineCalculator) {
        this.storage = storage;
        this.fineCalculator = fineCalculator;
    }

    /**
     * Retrieves all fines for a specific user.
     *
     * @param userId user ID
     * @return list of all fines belonging to the user
     */
    public List<Fine> getUserFines(String userId) {
        List<Fine> all = storage.loadFines();
        List<Fine> result = new ArrayList<>();
        for (Fine f : all) {
            if (f.getUserId().equals(userId)) {
                result.add(f);
            }
        }
        return result;
    }

    /**
     * Calculates the total outstanding (unpaid) fine balance for a user.
     *
     * @param userId user ID
     * @return total unpaid amount
     */
    public double getUserOutstandingBalance(String userId) {
        double total = 0.0;

        for (Fine f : storage.loadFines()) {
            if (f.getUserId().trim().equals(userId.trim()) && !f.isPaid()) {
                total += f.getAmount();
            }
        }

        return total;
    }

    /**
     * Creates a new fine with a specific amount.
     *
     * @param userId the ID of the fined user
     * @param amount the amount of the fine
     * @return the created {@link Fine}
     */
    public Fine createFine(String userId, double amount) {
        List<Fine> fines = storage.loadFines();
        String id = "F" + (fines.size() + 1);

        Fine fine = new Fine(id, userId, amount, false);
        fines.add(fine);
        storage.saveFines(fines);
        return fine;
    }

    /**
     * Creates a fine for an overdue item based on media type and number of overdue days.
     * Fine calculation is delegated to {@link FineCalculator}.
     *
     * @param userId      borrower user ID
     * @param mediaType   type of borrowed item (BOOK or CD)
     * @param overdueDays number of days overdue
     * @return the created fine, or null if the calculated amount is zero
     */
    public Fine createFineForOverdue(String userId, MediaType mediaType, long overdueDays) {
        double amount = fineCalculator.calculate(mediaType, overdueDays);
        if (amount <= 0.0) {
            return null;
        }
        return createFine(userId, amount);
    }

    /**
     * Pays a portion or all of a user's outstanding fines.
     * <p>
     * Payment is applied to the oldest fines first.
     * Fines that are fully paid are marked as paid and set to amount = 0.
     * </p>
     *
     * @param userId       user ID making the payment
     * @param amountToPay  amount the user is paying
     * @return new outstanding balance after the payment
     */
    public double payFine(String userId, double amountToPay) {
        if (amountToPay <= 0) {
            return getUserOutstandingBalance(userId);
        }

        List<Fine> fines = storage.loadFines();
        double remainingToPay = amountToPay;

        for (Fine fine : fines) {
            if (!fine.getUserId().equals(userId) || fine.isPaid()) {
                continue;
            }

            if (remainingToPay <= 0) break;

            double fineAmount = fine.getAmount();

            if (remainingToPay >= fineAmount) {
                remainingToPay -= fineAmount;
                fine.setAmount(0);
                fine.setPaid(true);
            } else {
                fine.setAmount(fineAmount - remainingToPay);
                remainingToPay = 0;
            }
        }

        storage.saveFines(fines);

        return getUserOutstandingBalance(userId);
    }

    /**
     * Checks whether a user has any unpaid fines.
     *
     * @param userId user ID
     * @return true if the user still owes money
     */
    public boolean hasUnpaidFines(String userId) {
        return getUserOutstandingBalance(userId) > 0.0;
    }
}
