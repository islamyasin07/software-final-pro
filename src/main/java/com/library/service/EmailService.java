package com.library.service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Service responsible for sending email notifications using SMTP.
 * <p>
 * This class uses Gmail's SMTP server with TLS encryption to send emails.
 * The username and password provided must correspond to an application
 * password (App Password), not the user's regular Gmail password.
 * </p>
 *
 * <p>
 * Used by {@link ReminderService} for sending overdue reminders
 * and can be used for other system notifications.
 * </p>
 *
 * @author Maram
 * @version 1.0
 */
public class EmailService {

    /**
     * The email address used as the sender.
     */
    private final String username;

    /**
     * The app-specific password used for SMTP authentication.
     */
    private final String password;

    /**
     * Creates a new EmailService using the provided SMTP credentials.
     *
     * @param username the sender email address
     * @param password the app password for SMTP authentication
     */
    public EmailService(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Sends an email message using Gmail SMTP.
     *
     * @param to      recipient email address
     * @param subject email subject
     * @param body    plain text email body
     *
     * @throws RuntimeException if the email fails to send
     */
    public void sendEmail(String to, String subject, String body) {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
