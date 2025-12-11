package com.library.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void constructor_setsAllFieldsAndGettersWork() {
        User user = new User("U1", "Dana", "dana@example.com", "secret");

        assertEquals("U1", user.getId());
        assertEquals("Dana", user.getName());
        assertEquals("dana@example.com", user.getEmail());
        assertEquals("secret", user.getPassword());
    }
}
