package com.authsystem.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.authsystem.dto.AdminUserDto;
import com.authsystem.entity.User;
import com.authsystem.exception.UserNotFoundException;
import com.authsystem.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;

    public List<AdminUserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("nullness")
    public AdminUserDto getUserById(Long id) {
        @SuppressWarnings("nullness")
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return convertToDto(user);
    }

    public AdminUserDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
        return convertToDto(user);
    }

    @SuppressWarnings("nullness")
    public AdminUserDto enableUser(Long id) {
        @SuppressWarnings("nullness")
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        user.setEnabled(true);
        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
        return convertToDto(user);
    }

    @SuppressWarnings("nullness")
    public AdminUserDto disableUser(Long id) {
        @SuppressWarnings("nullness")
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        user.setEnabled(false);
        userRepository.save(user);
        return convertToDto(user);
    }

    @SuppressWarnings("nullness")
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @SuppressWarnings("nullness")
    public AdminUserDto updateUserRole(Long id, String role) {
        @SuppressWarnings("nullness")
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        user.setRole(role);
        userRepository.save(user);
        return convertToDto(user);
    }

    public long getTotalUsers() {
        return userRepository.count();
    }

    public long getActiveUsers() {
        return userRepository.findAll().stream()
                .filter(User::isEnabled)
                .count();
    }

    private AdminUserDto convertToDto(User user) {
        return new AdminUserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.isEnabled(),
                user.getRole(),
                user.isTotpEnabled(),
                user.getFailedAttempts(),
                user.getLockedUntil(),
                user.getLastLoginAt(),
                user.getLastLoginIp(),
                user.getCreatedAt()
        );
    }
}


