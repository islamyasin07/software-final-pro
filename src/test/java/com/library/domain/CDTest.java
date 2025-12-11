package com.library.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link CD} domain model.
 * <p>
 * These tests verify that all getters, setters, and constructor
 * behavior of the CD class function correctly.
 * </p>
 *
 * @author Maram
 * @version 1.0
 */
class CDTest {

    @Test
    void constructor_setsAllFieldsCorrectly() {
        CD cd = new CD("CD1", "Greatest Hits", "Artist A", false);

        assertEquals("CD1", cd.getId());
        assertEquals("Greatest Hits", cd.getTitle());
        assertEquals("Artist A", cd.getArtist());
        assertFalse(cd.isBorrowed());
    }

    @Test
    void setBorrowed_updatesBorrowedStatus() {
        CD cd = new CD("CD2", "Album X", "Singer B", false);

        cd.setBorrowed(true);

        assertTrue(cd.isBorrowed());
    }

    @Test
    void borrowedFlag_canBeSetBackToFalse() {
        CD cd = new CD("CD3", "Symphony No. 5", "Composer Y", true);

        cd.setBorrowed(false);

        assertFalse(cd.isBorrowed());
    }
}
