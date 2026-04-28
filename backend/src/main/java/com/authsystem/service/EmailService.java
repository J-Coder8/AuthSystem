package com.authsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    public record DeliveryStatus(boolean delivered, String failureReason) {}

    private JavaMailSender mailSender;
    private String fromEmail;

    @Autowired(required = false)
    public void setMailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Value("${spring.mail.username:}")
    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    public boolean isDeliveryConfigured() {
        return mailSender != null
                && fromEmail != null
                && !fromEmail.isBlank()
                && !fromEmail.contains("YOUR_");
    }

    public DeliveryStatus sendOtpEmail(String toEmail, String otp, String username) {
        if (toEmail == null || toEmail.isBlank()) {
            return new DeliveryStatus(false, "This account does not have an email address for OTP delivery.");
        }

        if (!isDeliveryConfigured()) {
            printCodeToConsole("OTP for user", username, toEmail, "OTP CODE", otp);
            return new DeliveryStatus(false,
                    "OTP email delivery is not configured. Set AUTH_GMAIL_USER and AUTH_GMAIL_APP_PASSWORD, or add them to backend/mail.env when using run.bat.");
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Your OTP Code - Authentication System");
            message.setText(String.format(
                "Hello %s,\n\n" +
                "Your One-Time Password (OTP) for authentication is:\n\n" +
                "OTP: %s\n\n" +
                "This OTP will expire in 5 minutes.\n\n" +
                "If you did not request this OTP, please ignore this email.\n\n" +
                "Best regards,\n" +
                "Authentication System Team",
                username, otp
            ));
            
            mailSender.send(message);
            System.out.println("OTP email sent successfully to: " + toEmail);
            return new DeliveryStatus(true, null);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
            printCodeToConsole("OTP for user", username, toEmail, "OTP CODE", otp);
            return new DeliveryStatus(false, "Failed to send the OTP email. Please check the Gmail configuration.");
        }
    }

    public void sendWelcomeEmail(String toEmail, String username) {
        if (mailSender == null || fromEmail == null || fromEmail.isEmpty() || fromEmail.contains("YOUR_")) {
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to Authentication System");
            message.setText(String.format(
                "Hello %s,\n\n" +
                "Welcome to our Authentication System!\n\n" +
                "Your account has been successfully created.\n\n" +
                "You can now login using your username and password.\n\n" +
                "Best regards,\n" +
                "Authentication System Team",
                username
            ));
            
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Welcome email failed: " + e.getMessage());
        }
    }

    public DeliveryStatus sendPasswordResetEmail(String toEmail, String otp, String username) {
        if (toEmail == null || toEmail.isBlank()) {
            return new DeliveryStatus(false, "This account does not have an email address for password reset.");
        }

        if (!isDeliveryConfigured()) {
            printCodeToConsole("Password Reset for user", username, toEmail, "Reset OTP CODE", otp);
            return new DeliveryStatus(false,
                    "Password reset email delivery is not configured. Set AUTH_GMAIL_USER and AUTH_GMAIL_APP_PASSWORD, or add them to backend/mail.env when using run.bat.");
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Password Reset - Authentication System");
            message.setText(String.format(
                "Hello %s,\n\n" +
                "You requested a password reset for your account.\n\n" +
                "Your Password Reset OTP is:\n\n" +
                "OTP: %s\n\n" +
                "This OTP will expire in 5 minutes.\n\n" +
                "If you did not request this password reset, please ignore this email.\n\n" +
                "Best regards,\n" +
                "Authentication System Team",
                username, otp
            ));
            
            mailSender.send(message);
            System.out.println("Password reset email sent successfully to: " + toEmail);
            return new DeliveryStatus(true, null);
        } catch (Exception e) {
            System.err.println("Failed to send password reset email: " + e.getMessage());
            printCodeToConsole("Password Reset for user", username, toEmail, "Reset OTP CODE", otp);
            return new DeliveryStatus(false,
                    "Failed to send the password reset email. Please check the Gmail configuration.");
        }
    }

    private void printCodeToConsole(String title, String username, String toEmail, String codeLabel, String code) {
        System.out.println("===========================================");
        System.out.println(title + ": " + username);
        System.out.println("Email: " + toEmail);
        System.out.println(codeLabel + ": " + code);
        System.out.println("===========================================");
    }
}
