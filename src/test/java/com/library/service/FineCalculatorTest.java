package com.library.service;

import com.library.domain.FineCalculator;
import com.library.domain.MediaType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link FineCalculator} class.
 *
 * <p>This test suite verifies that the fine calculation logic behaves correctly
 * for different media types (BOOK and CD) and their associated overdue penalties.
 * The calculator applies a fixed fine when an item is overdue, based on its media type:</p>
 *
 * <ul>
 *     <li><b>BOOK:</b> 10 NIS fine when overdue.</li>
 *     <li><b>CD:</b> 20 NIS fine when overdue.</li>
 * </ul>
 *
 * <p>Both tests ensure that:</p>
 * <ul>
 *     <li>No fine is applied when overdue days = 0.</li>
 *     <li>The fine does not increase with the number of overdue days.</li>
 * </ul>
 *
 * @author Maram
 * @version 1.0
 */
class FineCalculatorTest {

    /**
     * Verifies that when calculating fines for {@link MediaType#BOOK},
     * the calculator returns:
     * <ul>
     *     <li>0.0 when the book is not overdue.</li>
     *     <li>10.0 when the book is overdue by any positive number of days.</li>
     * </ul>
     */
    @Test
    void calculate_bookFine_is10WhenOverdue() {
        FineCalculator calc = new FineCalculator();

        assertEquals(0.0, calc.calculate(MediaType.BOOK, 0));
        assertEquals(10.0, calc.calculate(MediaType.BOOK, 1));
        assertEquals(10.0, calc.calculate(MediaType.BOOK, 5));
    }

    /**
     * Verifies that when calculating fines for {@link MediaType#CD},
     * the calculator returns:
     * <ul>
     *     <li>0.0 when the CD is not overdue.</li>
     *     <li>20.0 when the CD is overdue by any positive number of days.</li>
     * </ul>
     */
    @Test
    void calculate_cdFine_is20WhenOverdue() {
        FineCalculator calc = new FineCalculator();

        assertEquals(0.0, calc.calculate(MediaType.CD, 0));
        assertEquals(20.0, calc.calculate(MediaType.CD, 1));
        assertEquals(20.0, calc.calculate(MediaType.CD, 10));
    }





    /**
     * Verifies that the calculator throws an exception
     * when calculating a fine for an unsupported media type.
     */
    @Test
    void calculate_throwsException_forUnsupportedMediaType() {
        FineCalculator calc = new FineCalculator();

        // Fake unsupported media type by passing null
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                calc.calculate(null, 5)
        );

        assertTrue(ex.getMessage().contains("null"));
    }











}
