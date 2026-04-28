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
        if (!userRepository.existsByUsername("JaytheGreat")) {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            User user = new User();
            user.setUsername("JaytheGreat");
            user.setEmail("jay@example.com");
            user.setFullName("Jay the Great");
            user.setPassword(encoder.encode("Password123!"));
            user.setPasswordChangedAt(LocalDateTime.now());
            user.setEnabled(true);
            user.setRole("USER");
            userRepository.save(user);
            System.out.println("Created test user: JaytheGreat / Password123! / jay@example.com");
        } else {
            System.out.println("Test user JaytheGreat already exists.");
        }
    }
}

