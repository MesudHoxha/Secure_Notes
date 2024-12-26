package com.example.secure_notes;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class OTPSender {

    private static final String smtpHost = "smtp.gmail.com";
    private static final String smtpPort = "587";
    private static final String senderEmail = "admir.rama@student.uni-pr.edu";
    private static final String senderPassword = "bbpq madp nute bplj";

    public static boolean sendEmail(String email, String otp) {
        Properties properties = System.getProperties();
        properties.put("mail.smtp.host", smtpHost);
        properties.put("mail.smtp.port", smtpPort);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", true);

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(email));
            message.setSubject("Your two-factor authentication code");
            message.setText("Your authentication code is: " + otp);
            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            return false;
        }
    }

}
