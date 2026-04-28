package com.authsystem.dto;

import java.time.LocalDateTime;

public class AdminUserDto {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private boolean enabled;
    private String role;
    private boolean totpEnabled;
    private int failedAttempts;
    private LocalDateTime lockedUntil;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private LocalDateTime createdAt;

    // Constructor
    public AdminUserDto(Long id, String username, String email, String fullName, String phone, boolean enabled, String role, boolean totpEnabled, int failedAttempts, LocalDateTime lockedUntil, LocalDateTime lastLoginAt, String lastLoginIp, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.enabled = enabled;
        this.role = role;
        this.totpEnabled = totpEnabled;
        this.failedAttempts = failedAttempts;
        this.lockedUntil = lockedUntil;
        this.lastLoginAt = lastLoginAt;
        this.lastLoginIp = lastLoginIp;
        this.createdAt = createdAt;
    }

    // Getters only (DTO pattern)
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getRole() {
        return role;
    }

    public boolean isTotpEnabled() {
        return totpEnabled;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

