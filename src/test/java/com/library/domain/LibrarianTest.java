package com.library.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LibrarianTest {

    @Test
    void librarian_extendsUserAndUsesSuperConstructor() {
        Librarian lib = new Librarian("L1", "Librarian", "lib@example.com", "pwd");

        assertEquals("L1", lib.getId());
        assertEquals("Librarian", lib.getName());
        assertEquals("lib@example.com", lib.getEmail());
        assertEquals("pwd", lib.getPassword());
    }
}
