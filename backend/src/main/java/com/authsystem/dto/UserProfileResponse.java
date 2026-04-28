package com.authsystem.dto;

import java.time.LocalDateTime;

public class UserProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String bio;
    private String profilePicture;
    private String address;
    private String city;
    private String country;
    private boolean enabled;
    private String role;
    private boolean totpEnabled;
    private boolean faceEnabled;
    private LocalDateTime faceRegisteredAt;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private String lastLoginDevice;
    private boolean darkMode;
    private boolean emailNotifications;
    private boolean loginAlerts;
    private Integer backupCodesRemaining;
    private LocalDateTime backupCodesGeneratedAt;
    private LocalDateTime passwordChangedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserProfileResponse() {}

    public UserProfileResponse(Long id, String username, String email, String fullName, String phone, String bio, 
                               String profilePicture, String address, String city, String country, boolean enabled,
                               String role, boolean totpEnabled, boolean faceEnabled, LocalDateTime faceRegisteredAt,
                               LocalDateTime lastLoginAt, String lastLoginIp, String lastLoginDevice, 
                               boolean darkMode, boolean emailNotifications, boolean loginAlerts,
                               Integer backupCodesRemaining, LocalDateTime backupCodesGeneratedAt,
                               LocalDateTime passwordChangedAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.bio = bio;
        this.profilePicture = profilePicture;
        this.address = address;
        this.city = city;
        this.country = country;
        this.enabled = enabled;
        this.role = role;
        this.totpEnabled = totpEnabled;
        this.faceEnabled = faceEnabled;
        this.faceRegisteredAt = faceRegisteredAt;
        this.lastLoginAt = lastLoginAt;
        this.lastLoginIp = lastLoginIp;
        this.lastLoginDevice = lastLoginDevice;
        this.darkMode = darkMode;
        this.emailNotifications = emailNotifications;
        this.loginAlerts = loginAlerts;
        this.backupCodesRemaining = backupCodesRemaining;
        this.backupCodesGeneratedAt = backupCodesGeneratedAt;
        this.passwordChangedAt = passwordChangedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isTotpEnabled() { return totpEnabled; }
    public void setTotpEnabled(boolean totpEnabled) { this.totpEnabled = totpEnabled; }

    public boolean isFaceEnabled() { return faceEnabled; }
    public void setFaceEnabled(boolean faceEnabled) { this.faceEnabled = faceEnabled; }

    public LocalDateTime getFaceRegisteredAt() { return faceRegisteredAt; }
    public void setFaceRegisteredAt(LocalDateTime faceRegisteredAt) { this.faceRegisteredAt = faceRegisteredAt; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public String getLastLoginIp() { return lastLoginIp; }
    public void setLastLoginIp(String lastLoginIp) { this.lastLoginIp = lastLoginIp; }

    public String getLastLoginDevice() { return lastLoginDevice; }
    public void setLastLoginDevice(String lastLoginDevice) { this.lastLoginDevice = lastLoginDevice; }

    public boolean isDarkMode() { return darkMode; }
    public void setDarkMode(boolean darkMode) { this.darkMode = darkMode; }

    public boolean isEmailNotifications() { return emailNotifications; }
    public void setEmailNotifications(boolean emailNotifications) { this.emailNotifications = emailNotifications; }

    public boolean isLoginAlerts() { return loginAlerts; }
    public void setLoginAlerts(boolean loginAlerts) { this.loginAlerts = loginAlerts; }

    public Integer getBackupCodesRemaining() { return backupCodesRemaining; }
    public void setBackupCodesRemaining(Integer backupCodesRemaining) { this.backupCodesRemaining = backupCodesRemaining; }

    public LocalDateTime getBackupCodesGeneratedAt() { return backupCodesGeneratedAt; }
    public void setBackupCodesGeneratedAt(LocalDateTime backupCodesGeneratedAt) { this.backupCodesGeneratedAt = backupCodesGeneratedAt; }

    public LocalDateTime getPasswordChangedAt() { return passwordChangedAt; }
    public void setPasswordChangedAt(LocalDateTime passwordChangedAt) { this.passwordChangedAt = passwordChangedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
