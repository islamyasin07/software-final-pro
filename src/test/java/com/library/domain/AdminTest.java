package com.library.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AdminTest {

    @Test
    void admin_extendsUserAndUsesSuperConstructor() {
        Admin admin = new Admin("A1", "Admin Name", "admin@example.com", "pwd");

        assertEquals("A1", admin.getId());
        assertEquals("Admin Name", admin.getName());
        assertEquals("admin@example.com", admin.getEmail());
        assertEquals("pwd", admin.getPassword());
    }
}
