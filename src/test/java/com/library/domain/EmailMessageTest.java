package com.library.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmailMessageTest {

    @Test
    void constructor_setsFieldsAndGettersWork() {
        EmailMessage msg = new EmailMessage(
                "user@example.com",
                "Subject",
                "Body text"
        );

        assertEquals("user@example.com", msg.getTo());
        assertEquals("Subject", msg.getSubject());
        assertEquals("Body text", msg.getBody());
    }
}
