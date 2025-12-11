package com.library.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BookFineStrategyTest {

    @Test
    void calculateFine_returnsZeroWhenNotOverdue() {
        BookFineStrategy s = new BookFineStrategy();

        assertEquals(0.0, s.calculateFine(0));
        assertEquals(0.0, s.calculateFine(-3));
    }

    @Test
    void calculateFine_returnsFlatTenWhenOverdue() {
        BookFineStrategy s = new BookFineStrategy();

        assertEquals(10.0, s.calculateFine(1));
        assertEquals(10.0, s.calculateFine(10));
    }
}
