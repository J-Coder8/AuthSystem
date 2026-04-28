package com.authsystem.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column
    private LocalDateTime passwordChangedAt;

    @Column(nullable = false)
    private String fullName;

    @Column
    private String phone;

    @Column
    private String bio;

    @Column
    private String profilePicture;

    @Column
    private String address;

    @Column
    private String city;

    @Column
    private String country;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private int failedAttempts = 0;

    @Column
    private LocalDateTime lockedUntil;

    @Column(nullable = false)
    private String role = "USER";

    @Column(length = 255)
    private String totpSecret;

    @Column(nullable = false)
    private boolean totpEnabled = false;

    @Column
    private LocalDateTime totpVerifiedAt;

    @Column(nullable = false)
    private boolean darkMode = false;

    @Column
    private boolean emailNotifications = true;

    @Column
    private boolean loginAlerts = true;

    @Column
    private String recoveryEmail;

    @Column
    private String recoveryPhone;

    @Column(length = 1000)
    private String activeSessions;

    @Column(length = 1000)
    private String trustedDevices;

    @Column(length = 2000)
    private String backupCodes;

    @Column
    private LocalDateTime backupCodesGeneratedAt;

    @Column
    private LocalDateTime lastLoginAt;

    @Column
    private String lastLoginIp;

    @Column
    private String lastLoginDevice;

    // Face recognition fields
    @Column(columnDefinition = "TEXT")
    private String faceEncoding;  // Store face biometric template as JSON

    @Column(nullable = false)
    private boolean faceEnabled = false;  // Enable/disable face login

    @Column
    private LocalDateTime faceRegisteredAt;  // When face was registered

    @Column
    private String faceImagePath;  // Optional: store face image path

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    // Default constructor
    public User() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getPasswordChangedAt() {
        return passwordChangedAt;
    }

    public void setPasswordChangedAt(LocalDateTime passwordChangedAt) {
        this.passwordChangedAt = passwordChangedAt;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(LocalDateTime lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getTotpSecret() {
        return totpSecret;
    }

    public void setTotpSecret(String totpSecret) {
        this.totpSecret = totpSecret;
    }

    public boolean isTotpEnabled() {
        return totpEnabled;
    }

    public void setTotpEnabled(boolean totpEnabled) {
        this.totpEnabled = totpEnabled;
    }

    public LocalDateTime getTotpVerifiedAt() {
        return totpVerifiedAt;
    }

    public void setTotpVerifiedAt(LocalDateTime totpVerifiedAt) {
        this.totpVerifiedAt = totpVerifiedAt;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
    }

    public boolean isEmailNotifications() {
        return emailNotifications;
    }

    public void setEmailNotifications(boolean emailNotifications) {
        this.emailNotifications = emailNotifications;
    }

    public boolean isLoginAlerts() {
        return loginAlerts;
    }

    public void setLoginAlerts(boolean loginAlerts) {
        this.loginAlerts = loginAlerts;
    }

    public String getRecoveryEmail() {
        return recoveryEmail;
    }

    public void setRecoveryEmail(String recoveryEmail) {
        this.recoveryEmail = recoveryEmail;
    }

    public String getRecoveryPhone() {
        return recoveryPhone;
    }

    public void setRecoveryPhone(String recoveryPhone) {
        this.recoveryPhone = recoveryPhone;
    }

    public String getActiveSessions() {
        return activeSessions;
    }

    public void setActiveSessions(String activeSessions) {
        this.activeSessions = activeSessions;
    }

    public String getTrustedDevices() {
        return trustedDevices;
    }

    public void setTrustedDevices(String trustedDevices) {
        this.trustedDevices = trustedDevices;
    }

    public String getBackupCodes() {
        return backupCodes;
    }

    public void setBackupCodes(String backupCodes) {
        this.backupCodes = backupCodes;
    }

    public LocalDateTime getBackupCodesGeneratedAt() {
        return backupCodesGeneratedAt;
    }

    public void setBackupCodesGeneratedAt(LocalDateTime backupCodesGeneratedAt) {
        this.backupCodesGeneratedAt = backupCodesGeneratedAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }

    public String getLastLoginDevice() {
        return lastLoginDevice;
    }

    public void setLastLoginDevice(String lastLoginDevice) {
        this.lastLoginDevice = lastLoginDevice;
    }

    // Face recognition getters and setters
    public String getFaceEncoding() {
        return faceEncoding;
    }

    public void setFaceEncoding(String faceEncoding) {
        this.faceEncoding = faceEncoding;
    }

    public boolean isFaceEnabled() {
        return faceEnabled;
    }

    public void setFaceEnabled(boolean faceEnabled) {
        this.faceEnabled = faceEnabled;
    }

    public LocalDateTime getFaceRegisteredAt() {
        return faceRegisteredAt;
    }

    public void setFaceRegisteredAt(LocalDateTime faceRegisteredAt) {
        this.faceRegisteredAt = faceRegisteredAt;
    }

    public String getFaceImagePath() {
        return faceImagePath;
    }

    public void setFaceImagePath(String faceImagePath) {
        this.faceImagePath = faceImagePath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isAccountLocked() {
        return lockedUntil != null && LocalDateTime.now().isBefore(lockedUntil);
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
