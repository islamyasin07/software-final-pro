package com.library.service;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

/**
 * Unit tests for the {@link EmailService} class.
 *
 * <p>This test suite verifies the behavior of the email-sending logic by
 * mocking the JavaMail {@link Transport} class. No real network operation
 * is performed; instead, the static send method is intercepted to validate
 * correct invocation and exception handling.</p>
 *
 * <p>The tests ensure:</p>
 * <ul>
 *   <li>Email sending succeeds when Transport.send executes normally.</li>
 *   <li>MessagingException thrown by Transport.send is wrapped into a RuntimeException.</li>
 * </ul>
 *
 * @author Maram
 * @version 1.0
 */
class EmailServiceTest {

    /**
     * Ensures that {@link EmailService#sendEmail(String, String, String)}
     * successfully invokes {@link Transport#send(Message)} without throwing
     * exceptions. Uses Mockito's {@link MockedStatic} to mock the static method.
     */
    @Test
    void sendEmail_invokesTransportSendSuccessfully() {
        EmailService service = new EmailService("sender@example.com", "password");

        try (MockedStatic<Transport> transportMock = mockStatic(Transport.class)) {

            assertDoesNotThrow(() ->
                    service.sendEmail("to@example.com", "Subject", "Body")
            );

            transportMock.verify(() ->
                    Transport.send(any(Message.class))
            );
        }
    }

    /**
     * Verifies that when {@link Transport#send(Message)} throws a
     * {@link MessagingException}, the EmailService correctly wraps it
     * inside a {@link RuntimeException}. This ensures consistent error
     * propagation and prevents checked exceptions from leaking outward.
     */
    @Test
    void sendEmail_whenTransportFails_wrapsMessagingException() {
        EmailService service = new EmailService("sender@example.com", "password");

        try (MockedStatic<Transport> transportMock = mockStatic(Transport.class)) {

            transportMock.when(() ->
                    Transport.send(any(Message.class))
            ).thenThrow(new MessagingException("SMTP error"));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> service.sendEmail("to@example.com", "Subject", "Body")
            );

            assertTrue(ex.getCause() instanceof MessagingException);
        }
    }
}
