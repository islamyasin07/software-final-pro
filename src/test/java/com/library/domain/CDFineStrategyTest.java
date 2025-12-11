package com.library.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CDFineStrategyTest {

    @Test
    void calculateFine_returnsZeroWhenNotOverdue() {
        CDFineStrategy s = new CDFineStrategy();

        assertEquals(0.0, s.calculateFine(0));
        assertEquals(0.0, s.calculateFine(-5));
    }

    @Test
    void calculateFine_returnsFlatTwentyWhenOverdue() {
        CDFineStrategy s = new CDFineStrategy();

        assertEquals(20.0, s.calculateFine(1));
        assertEquals(20.0, s.calculateFine(30));
    }
}
