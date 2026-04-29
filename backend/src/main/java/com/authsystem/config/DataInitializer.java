package com.authsystem.config;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.authsystem.entity.User;
import com.authsystem.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        // Create test user if not exists
        if (!userRepository.existsByUsername("testuser")) {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            User user = new User();
            user.setUsername("testuser");
            user.setEmail("testuser@example.com");
            user.setFullName("Test User");
            user.setPhone("+1-555-1234");
            user.setBio("Test user profile");
            user.setPassword(encoder.encode("Test123!"));
            user.setPasswordChangedAt(LocalDateTime.now());
            user.setEnabled(true);
            user.setRole("USER");
            user.setEmailNotifications(true);
            user.setLoginAlerts(true);
            userRepository.save(user);
            System.out.println("Created test user: testuser / Test123! / testuser@example.com");
        } else {
            System.out.println("Test user testuser already exists.");
        }
        
        // Create test notifications
        User testUser = userRepository.findByUsername("testuser").orElse(null);
        if (testUser != null) {
            testUser.setLoginAlerts(true);
            // Add sample notifications via service if available
            System.out.println("Test data ready for testuser");
        }
    }
}

