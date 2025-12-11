package com.library.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MediaTypeTest {

    @Test
    void enum_hasExpectedValues() {
        assertEquals(MediaType.BOOK, MediaType.valueOf("BOOK"));
        assertEquals(MediaType.CD, MediaType.valueOf("CD"));
        assertEquals(2, MediaType.values().length);
    }
}
