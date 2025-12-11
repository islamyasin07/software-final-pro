package com.library.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FineTest {

    @Test
    void constructor_setsFieldsAndGettersWork() {
        Fine fine = new Fine("F1", "U1", 50.0, false);

        assertEquals("F1", fine.getId());
        assertEquals("U1", fine.getUserId());
        assertEquals(50.0, fine.getAmount());
        assertFalse(fine.isPaid());
    }

    @Test
    void setters_updateAmountAndPaid() {
        Fine fine = new Fine("F1", "U1", 10.0, false);

        fine.setAmount(25.5);
        fine.setPaid(true);

        assertEquals(25.5, fine.getAmount());
        assertTrue(fine.isPaid());
    }
}
