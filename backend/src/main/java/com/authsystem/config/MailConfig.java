package com.authsystem.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {

    @Value("${spring.mail.host:smtp.gmail.com}")
    private String host;

    @Value("${spring.mail.port:587}")
    private int port;

    @Value("${spring.mail.username:}")
    private String username;

    @Value("${spring.mail.password:}")
    private String password;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        
        // Only set credentials if they are provided
        if (username != null && !username.isEmpty() && !username.equals("YOUR_GMAIL@gmail.com")) {
            mailSender.setUsername(username);
            mailSender.setPassword(password);
            
            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
            props.put("mail.debug", "true");
            
            System.out.println("Email configured with username: " + username);
        } else {
            System.out.println("Email NOT configured - OTP will only be shown in console");
            // Return a dummy mail sender that won't crash but won't send emails
            mailSender.setHost("localhost");
            mailSender.setPort(0);
        }
        
        return mailSender;
    }
}

