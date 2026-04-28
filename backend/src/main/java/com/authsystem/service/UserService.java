package com.authsystem.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.authsystem.config.DebugProperties;
import com.authsystem.dto.AuthResponse;
import com.authsystem.dto.BackupCodesResponse;
import com.authsystem.dto.ChangePasswordRequest;
import com.authsystem.dto.LoginHistoryDto;
import com.authsystem.dto.LoginRequest;
import com.authsystem.dto.OtpRequest;
import com.authsystem.dto.RegisterRequest;
import com.authsystem.dto.ResetPasswordRequest;
import com.authsystem.dto.UpdateProfileRequest;
import com.authsystem.dto.UserProfileResponse;
import com.authsystem.entity.LoginHistory;
import com.authsystem.entity.OtpCode;
import com.authsystem.entity.User;
import com.authsystem.exception.UserAlreadyExistsException;
import com.authsystem.exception.UserNotFoundException;
import com.authsystem.repository.LoginHistoryRepository;
import com.authsystem.repository.OtpCodeRepository;
import com.authsystem.repository.UserRepository;
import com.authsystem.security.JwtUtil;

@Service
public class UserService implements UserDetailsService {

    private static final String PASSWORD_POLICY_MESSAGE =
            "Password must be at least 8 characters and include uppercase, lowercase, and a number.";
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int ACCOUNT_LOCK_MINUTES = 15;
    private static final int OTP_EXPIRY_MINUTES = 5;

    private final UserRepository userRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final OtpCodeRepository otpCodeRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final Random random = new Random();
    
    // Services for email and TOTP
    private final EmailService emailService;
    private final TotpService totpService;
    private final NotificationService notificationService;
    private final BackupCodeService backupCodeService;
    private final DebugProperties debugProperties;

    public UserService(
            UserRepository userRepository,
            LoginHistoryRepository loginHistoryRepository,
            OtpCodeRepository otpCodeRepository,
            JwtUtil jwtUtil,
            EmailService emailService,
            TotpService totpService,
            NotificationService notificationService,
            BackupCodeService backupCodeService,
            DebugProperties debugProperties) {
        this.userRepository = userRepository;
        this.loginHistoryRepository = loginHistoryRepository;
        this.otpCodeRepository = otpCodeRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.emailService = emailService;
        this.totpService = totpService;
        this.notificationService = notificationService;
        this.backupCodeService = backupCodeService;
        this.debugProperties = debugProperties;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername()) || userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Username or email already exists");
        }

        validatePasswordPolicy(request.getPassword());

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setEnabled(true);
        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        // Send welcome email
        try {
            emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());
        } catch (Exception e) {
            System.out.println("Welcome email could not be sent: " + e.getMessage());
        }

        String token = jwtUtil.generateToken(user.getUsername());
        return new AuthResponse(
                token,
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                false,
                "Registration successful");
    }

    public AuthResponse login(LoginRequest request, String ipAddress, String deviceName) {
        User user = findUserByIdentifier(request.getUsername());

        if (user.isAccountLocked()) {
            throw new RuntimeException(buildLockedMessage(user.getLockedUntil()));
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException(registerFailedAttempt(
                    user,
                    ipAddress,
                    deviceName,
                    "Invalid credentials",
                    "Invalid credentials."));
        }

        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        // If TOTP is enabled, verify with TOTP instead of email OTP
        if (shouldUseTotp(user)) {
            logTotpIfEnabled(user);
            return AuthResponse.builder()
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .role(user.getRole())
                    .requiresOtp(true)
                    .requiresTotp(true)
                    .message("Enter your TOTP code or a backup code")
                    .build();
        } else if (user.isTotpEnabled() && user.getTotpSecret() != null && debugProperties.isForceEmailOtp()) {
            System.out.println("Force email OTP enabled - bypassing TOTP for user: " + user.getUsername());
        }

        // Otherwise, send email OTP
        String otp = generateOtp(user);
        publishOtpToBackend(user, otp, "Login OTP");

        return new AuthResponse(
                null,
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                true,
                buildLoginOtpMessage());
    }

    public AuthResponse verifyOtp(OtpRequest request, String ipAddress, String deviceName) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.isAccountLocked()) {
            throw new RuntimeException(buildLockedMessage(user.getLockedUntil()));
        }

        // Check if TOTP is enabled for this user
        if (shouldUseTotp(user)) {
            // Verify TOTP code
            boolean usedBackupCode = false;
            boolean totpValid = totpService.verifyCode(user.getTotpSecret(), request.getCode());
            if (!totpValid) {
                BackupCodeService.ConsumeResult backupResult =
                        backupCodeService.consumeCode(user.getBackupCodes(), request.getCode());
                if (!backupResult.getMatched()) {
                    throw new RuntimeException(registerFailedAttempt(
                            user,
                            ipAddress,
                            deviceName,
                            "Invalid TOTP or backup code",
                            "Invalid TOTP or backup code."));
                }

                user.setBackupCodes(backupResult.getUpdatedHashes());
                userRepository.save(user);
                usedBackupCode = true;
                if (user.isLoginAlerts()) {
                    notificationService.createNotification(user, "Backup code used",
                            "A backup code was used to sign in to your account.",
                            "SECURITY", "WARNING");
                }
            }

            user.setFailedAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);

            String token = jwtUtil.generateToken(user.getUsername());
            updateLastLogin(user, ipAddress, deviceName);
            userRepository.save(user);
            recordLoginHistory(user, true, ipAddress, deviceName, null);
            return AuthResponse.builder()
                    .token(token)
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .role(user.getRole())
                    .requiresOtp(false)
                    .message(usedBackupCode ? "Login successful with backup code" : "Login successful with TOTP")
                    .build();
        }

        // Otherwise, verify email OTP
        OtpCode otpCode = otpCodeRepository.findTopByUserAndUsedFalseOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new RuntimeException("OTP not requested"));

        if (!otpCode.isValid()) {
            otpCodeRepository.delete(otpCode);
            throw new RuntimeException("OTP expired");
        }

        if (!otpCode.getCode().equals(request.getCode())) {
            throw new RuntimeException(registerFailedAttempt(
                    user,
                    ipAddress,
                    deviceName,
                    "Invalid OTP",
                    "Invalid OTP."));
        }

        otpCode.setUsed(true);
        otpCodeRepository.save(otpCode);

        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        updateLastLogin(user, ipAddress, deviceName);
        userRepository.save(user);
        String token = jwtUtil.generateToken(user.getUsername());
        recordLoginHistory(user, true, ipAddress, deviceName, null);
        return new AuthResponse(
                token,
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                false,
                "Login successful");
    }

    public List<LoginHistoryDto> getLoginHistory(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return loginHistoryRepository.findTop10ByUserOrderByLoginTimeDesc(user)
                .stream()
                .map(history -> new LoginHistoryDto(
                        history.getId(),
                        history.getLoginTime(),
                        history.getIpAddress(),
                        history.getUserAgent(),
                        history.isSuccess(),
                        history.getFailureReason(),
                        history.getRiskScore(),
                        history.getRiskFactors()))
                .collect(Collectors.toList());
    }

    /**
     * Generate a new TOTP secret for a user (first step of setup)
     */
    @SuppressWarnings("unused")
    public String generateTotpSecret(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        String secret = totpService.generateSecret();
        return secret;
    }

    /**
     * Generate a new set of backup codes for a user.
     * Returns the plain codes once; only hashes are stored.
     */
    public BackupCodesResponse generateBackupCodes(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<String> codes = backupCodeService.generateCodes(10);
        String hashed = backupCodeService.hashCodes(codes);
        LocalDateTime generatedAt = LocalDateTime.now();

        user.setBackupCodes(hashed);
        user.setBackupCodesGeneratedAt(generatedAt);
        userRepository.save(user);

        if (user.isLoginAlerts()) {
            notificationService.createNotification(user, "Backup codes generated",
                    "A new set of backup codes was generated for your account.",
                    "SECURITY", "INFO");
        }

        return new BackupCodesResponse(codes, codes.size(), generatedAt);
    }

    /**
     * Enable TOTP for a user after verifying the code
     */
    public AuthResponse enableTotp(String username, String secret, String code) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        // Verify the code first
        if (!totpService.verifyCode(secret, code)) {
            throw new RuntimeException("Invalid TOTP code. Please try again.");
        }
        
        user.setTotpSecret(secret);
        user.setTotpEnabled(true);
        user.setTotpVerifiedAt(LocalDateTime.now());
        userRepository.save(user);

        if (user.isLoginAlerts()) {
            notificationService.createNotification(user, "Two-factor enabled",
                    "Two-factor authentication (TOTP) was enabled on your account.",
                    "SECURITY", "INFO");
        }
        
        return new AuthResponse(
                null,
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                false,
                "TOTP enabled successfully! You can now use Google Authenticator to login.");
    }

    /**
     * Disable TOTP for a user
     */
    public AuthResponse disableTotp(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        user.setTotpSecret(null);
        user.setTotpEnabled(false);
        user.setTotpVerifiedAt(null);
        userRepository.save(user);

        if (user.isLoginAlerts()) {
            notificationService.createNotification(user, "Two-factor disabled",
                    "Two-factor authentication (TOTP) was disabled on your account.",
                    "SECURITY", "WARNING");
        }
        
        return new AuthResponse(
                null,
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                false,
                "TOTP disabled successfully!");
    }

    /**
     * Get remaining seconds for TOTP countdown
     */
    public int getTotpRemainingSeconds() {
        return totpService.getRemainingSeconds();
    }

    /**
     * Resend OTP (for backward compatibility)
     */
    public AuthResponse resendOtp(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (shouldUseTotp(user)) {
            logTotpIfEnabled(user);
            return AuthResponse.builder()
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .requiresOtp(true)
                    .requiresTotp(true)
                    .message("TOTP is enabled. Please use your authenticator or a backup code.")
                    .build();
        } else if (user.isTotpEnabled() && user.getTotpSecret() != null && debugProperties.isForceEmailOtp()) {
            System.out.println("Force email OTP enabled - bypassing TOTP for user: " + user.getUsername());
        }

        String otp = generateOtp(user);
        publishOtpToBackend(user, otp, "Login OTP");

        return new AuthResponse(
                null,
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                true,
                buildLoginOtpMessage());
    }

    /**
     * Request OTP by email or username - no password needed
     */
    public AuthResponse requestOtpByEmailOrUsername(String emailOrUsername) {
        User user = findUserByIdentifier(emailOrUsername);
        
        if (shouldUseTotp(user)) {
            logTotpIfEnabled(user);
            return AuthResponse.builder()
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .requiresOtp(true)
                    .requiresTotp(true)
                    .message("Enter your TOTP code or a backup code")
                    .build();
        } else if (user.isTotpEnabled() && user.getTotpSecret() != null && debugProperties.isForceEmailOtp()) {
            System.out.println("Force email OTP enabled - bypassing TOTP for user: " + user.getUsername());
        }

        String otp = generateOtp(user);
        publishOtpToBackend(user, otp, "Login OTP");

        return new AuthResponse(
                null,
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                true,
                buildLoginOtpMessage());
    }
    
    /**
     * Get current TOTP code for a user
     */
    public AuthResponse getTotpCode(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        if (!user.isTotpEnabled() || user.getTotpSecret() == null) {
            throw new RuntimeException("TOTP not enabled for this user");
        }
        
        String totpCode = totpService.generateCurrentCode(user.getTotpSecret());
        int remainingSeconds = totpService.getRemainingSeconds();
        
        return new AuthResponse(
                null,
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                false,
                "TOTP code generated",
                totpCode,
                remainingSeconds);
    }

    private String generateOtp(User user) {
        String otp = String.format("%06d", random.nextInt(1_000_000));
        otpCodeRepository.deleteByUser(user);

        OtpCode otpCode = new OtpCode();
        otpCode.setCode(otp);
        otpCode.setUser(user);
        otpCode.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        otpCode.setUsed(false);
        otpCodeRepository.save(otpCode);
        return otp;
    }

    private void logTotpIfEnabled(User user) {
        if (!debugProperties.isPrintTotp()) {
            return;
        }
        try {
            String totpCode = totpService.generateCurrentCode(user.getTotpSecret());
            int remainingSeconds = totpService.getRemainingSeconds();
            System.out.println("===========================================");
            System.out.println("TOTP for user: " + user.getUsername());
            System.out.println("Email: " + user.getEmail());
            System.out.println("TOTP CODE: " + totpCode + " (valid " + remainingSeconds + "s)");
            System.out.println("===========================================");
        } catch (Exception e) {
            System.out.println("Failed to generate TOTP for console: " + e.getMessage());
        }
    }

    private String buildLoginOtpMessage() {
        return "OTP generated. Check the backend console. It expires in " + OTP_EXPIRY_MINUTES + " minutes.";
    }

    private String buildPasswordResetOtpMessage() {
        return "Password reset OTP generated. Check the backend console. It expires in " + OTP_EXPIRY_MINUTES + " minutes.";
    }

    private boolean shouldUseTotp(User user) {
        return user.isTotpEnabled()
                && user.getTotpSecret() != null
                && !debugProperties.isForceEmailOtp();
    }

private void recordLoginHistory(
            User user,
            boolean success,
            String ipAddress,
            String deviceName,
            String failureReason) {
        LoginHistory history = new LoginHistory();
        history.setUser(user);
        history.setSuccess(success);
        history.setIpAddress(ipAddress);
        history.setUserAgent(deviceName);
        history.setFailureReason(failureReason);

        RiskAssessment risk = assessRisk(user, ipAddress, deviceName, success);
        history.setRiskScore(risk.score());
        history.setRiskFactors(risk.factors());

        loginHistoryRepository.save(history);
        maybeCreateLoginNotification(user, success, ipAddress, deviceName, failureReason, risk);
    }

    private void updateLastLogin(User user, String ipAddress, String deviceName) {
        user.setLastLoginAt(LocalDateTime.now());
        user.setLastLoginIp(ipAddress);
        user.setLastLoginDevice(deviceName);
    }

    private RiskAssessment assessRisk(User user, String ipAddress, String deviceName, boolean success) {
        int score = 0;
        List<String> factors = new ArrayList<>();

        if (ipAddress != null && user.getLastLoginIp() != null && !ipAddress.equals(user.getLastLoginIp())) {
            score += 40;
            factors.add("NEW_IP");
        }

        boolean trusted = isTrustedDevice(user, deviceName, success);
        if (deviceName != null && !deviceName.isBlank() && !trusted) {
            score += 40;
            factors.add("UNTRUSTED_DEVICE");
        }

        if (!success) {
            score += 20;
            factors.add("FAILED_ATTEMPT");
        }

        score = Math.min(score, 100);
        String factorText = factors.isEmpty() ? "LOW_RISK" : String.join(",", factors);
        return new RiskAssessment(score, factorText);
    }

    private boolean isTrustedDevice(User user, String deviceName, boolean success) {
        if (deviceName == null || deviceName.isBlank()) {
            return false;
        }
        List<String> trusted = getTrustedDevices(user);
        if (trusted.isEmpty()) {
            // Auto-trust the first device only on successful login
            if (success) {
                addTrustedDevice(user, deviceName);
                return true;
            }
            return false;
        }
        return trusted.stream().anyMatch(device -> device.equalsIgnoreCase(deviceName));
    }

    private List<String> getTrustedDevices(User user) {
        if (user.getTrustedDevices() == null || user.getTrustedDevices().isBlank()) {
            return new ArrayList<>();
        }
        return List.of(user.getTrustedDevices().split(","))
                .stream()
                .map(String::trim)
                .filter(device -> !device.isBlank())
                .collect(Collectors.toList());
    }

    private void addTrustedDevice(User user, String deviceName) {
        if (deviceName == null || deviceName.isBlank()) {
            return;
        }
        List<String> trusted = getTrustedDevices(user);
        if (trusted.stream().noneMatch(device -> device.equalsIgnoreCase(deviceName))) {
            trusted.add(deviceName);
            user.setTrustedDevices(String.join(",", trusted));
            userRepository.save(user);
        }
    }

    private void maybeCreateLoginNotification(
            User user,
            boolean success,
            String ipAddress,
            String deviceName,
            String failureReason,
            RiskAssessment risk) {
        if (user == null || !user.isLoginAlerts()) {
            return;
        }

        String ip = ipAddress != null ? ipAddress : "Unknown IP";
        String device = deviceName != null ? deviceName : "Unknown Device";

        if (!success) {
            String message = "Failed login from " + ip + " on " + device +
                    (failureReason != null ? ". Reason: " + failureReason : ".");
            notificationService.createNotification(user, "Failed login attempt", message, "LOGIN", "WARNING");
            return;
        }

        if (risk != null && !"LOW_RISK".equals(risk.factors())) {
            String message = "New login from " + ip + " on " + device +
                    ". Risk: " + risk.score() + "% (" + risk.factors() + ").";
            String severity = risk.score() >= 60 ? "WARNING" : "INFO";
            notificationService.createNotification(user, "New login detected", message, "LOGIN", severity);
        }
    }

    private record RiskAssessment(int score, String factors) {}

    // ========== New Methods for Expansion ==========

    /**
     * Get user profile
     */
    public UserProfileResponse getProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        int backupRemaining = backupCodeService.remainingCount(user.getBackupCodes());

        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getBio(),
                user.getProfilePicture(),
                user.getAddress(),
                user.getCity(),
                user.getCountry(),
                user.isEnabled(),
                user.getRole(),
                user.isTotpEnabled(),
                user.isFaceEnabled(),
                user.getFaceRegisteredAt(),
                user.getLastLoginAt(),
                user.getLastLoginIp(),
                user.getLastLoginDevice(),
                user.isDarkMode(),
                user.isEmailNotifications(),
                user.isLoginAlerts(),
                backupRemaining,
                user.getBackupCodesGeneratedAt(),
                user.getPasswordChangedAt(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    /**
     * Update user profile
     */
    @SuppressWarnings("nullness")
    public UserProfileResponse updateProfile(String username, UpdateProfileRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            user.setCity(request.getCity());
        }
        if (request.getCountry() != null) {
            user.setCountry(request.getCountry());
        }
        if (request.getEmailNotifications() != null) {
            user.setEmailNotifications(request.getEmailNotifications());
        }
        if (request.getLoginAlerts() != null) {
            user.setLoginAlerts(request.getLoginAlerts());
        }
        if (request.getDarkMode() != null) {
            user.setDarkMode(request.getDarkMode());
        }
        
        @SuppressWarnings("nullness")
        User savedUser = userRepository.save(user);
        user = savedUser;

        if (user.isLoginAlerts()) {
            notificationService.createNotification(user, "Profile updated",
                    "Your profile details were updated.",
                    "PROFILE", "INFO");
        }
        
        return getProfile(username);
    }

    /**
     * Change password
     */
    public AuthResponse changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        // Check if new passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New passwords do not match");
        }

        validatePasswordPolicy(request.getNewPassword());

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("New password must be different from your current password");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);

        if (user.isLoginAlerts()) {
            notificationService.createNotification(user, "Password changed",
                    "Your account password was changed successfully.",
                    "SECURITY", "INFO");
        }
        
        return new AuthResponse(
                null,
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                false,
                "Password changed successfully");
    }

    /**
     * Update profile picture
     */
    public AuthResponse updateProfilePicture(String username, String profilePicture) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        user.setProfilePicture(profilePicture);
        userRepository.save(user);

        if (user.isLoginAlerts()) {
            notificationService.createNotification(user, "Profile picture updated",
                    "Your profile picture was updated.",
                    "PROFILE", "INFO");
        }
        
        return new AuthResponse(
                null,
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                false,
                "Profile picture updated successfully");
    }

    /**
     * Get active sessions
     */
    public List<Map<String, Object>> getActiveSessions(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        List<Map<String, Object>> sessions = new ArrayList<>();
        
        // Create a default session if none exists
        if (user.getActiveSessions() == null || user.getActiveSessions().isEmpty()) {
            Map<String, Object> defaultSession = new HashMap<>();
            defaultSession.put("sessionId", "current");
            defaultSession.put("device", user.getLastLoginDevice() != null ? user.getLastLoginDevice() : "Unknown");
            defaultSession.put("ipAddress", user.getLastLoginIp() != null ? user.getLastLoginIp() : "Unknown");
            defaultSession.put("lastActive", user.getLastLoginAt() != null ? user.getLastLoginAt().toString() : "N/A");
            defaultSession.put("current", true);
            sessions.add(defaultSession);
        } else {
            // Parse existing sessions
            String[] sessionIds = user.getActiveSessions().split(",");
            for (String sessionId : sessionIds) {
                Map<String, Object> session = new HashMap<>();
                session.put("sessionId", sessionId.trim());
                session.put("device", "Active Session");
                session.put("ipAddress", "N/A");
                session.put("lastActive", "N/A");
                session.put("current", false);
                sessions.add(session);
            }
            
            // Add current session
            Map<String, Object> currentSession = new HashMap<>();
            currentSession.put("sessionId", "current");
            currentSession.put("device", user.getLastLoginDevice() != null ? user.getLastLoginDevice() : "Current Device");
            currentSession.put("ipAddress", user.getLastLoginIp() != null ? user.getLastLoginIp() : "Current IP");
            currentSession.put("lastActive", user.getLastLoginAt() != null ? user.getLastLoginAt().toString() : "Now");
            currentSession.put("current", true);
            sessions.add(0, currentSession);
        }
        
        return sessions;
    }

    /**
     * Revoke a specific session
     */
    public AuthResponse revokeSession(String username, String sessionId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        if ("current".equals(sessionId)) {
            throw new RuntimeException("Cannot revoke current session");
        }
        
        // Remove session from active sessions
        if (user.getActiveSessions() != null && !user.getActiveSessions().isEmpty()) {
            String[] sessions = user.getActiveSessions().split(",");
            StringBuilder sb = new StringBuilder();
            for (String session : sessions) {
                if (!session.trim().equals(sessionId)) {
                    if (sb.length() > 0) {
                        sb.append(",");
                    }
                    sb.append(session.trim());
                }
            }
            user.setActiveSessions(sb.toString());
            userRepository.save(user);
        }
        
        return new AuthResponse(
                null,
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                false,
                "Session revoked successfully");
    }

    /**
     * Logout from all devices
     */
    public AuthResponse logoutFromAllDevices(String username, String currentToken) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        // Clear all sessions
        user.setActiveSessions("");
        userRepository.save(user);
        
        return new AuthResponse(
                null,
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                false,
                "Logged out from all devices successfully");
    }

    /**
     * Forgot password - send reset OTP
     */
    public AuthResponse forgotPassword(String identifier) {
        User user = findUserByIdentifier(identifier);

        String otp = generateOtp(user);
        publishOtpToBackend(user, otp, "Password Reset OTP");
        
        return new AuthResponse(
                null,
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                true,
                buildPasswordResetOtpMessage());
    }

    private void publishOtpToBackend(User user, String otp, String label) {
        String username = user != null ? user.getUsername() : "unknown";
        String email = user != null ? user.getEmail() : "unknown";
        System.out.println("===========================================");
        System.out.println(label + " for user: " + username);
        System.out.println("Email: " + email);
        System.out.println("OTP CODE: " + otp);
        System.out.println("Expires in: " + OTP_EXPIRY_MINUTES + " minutes");
        System.out.println("===========================================");
    }

    /**
     * Reset password with OTP
     */
    public AuthResponse resetPassword(ResetPasswordRequest request) {
        User user = findUserByIdentifier(request.getIdentifier());
        String otp = request.getOtp() != null ? request.getOtp().trim() : "";
        String newPassword = request.getNewPassword();
        String confirmPassword = request.getConfirmPassword();

        if (otp.isBlank()) {
            throw new RuntimeException("OTP is required");
        }

        if (newPassword == null || confirmPassword == null || !newPassword.equals(confirmPassword)) {
            throw new RuntimeException("New passwords do not match");
        }

        // Verify OTP
        OtpCode otpCode = otpCodeRepository.findTopByUserAndUsedFalseOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new RuntimeException("No OTP requested"));
        
        if (!otpCode.isValid()) {
            otpCodeRepository.delete(otpCode);
            throw new RuntimeException("OTP expired");
        }
        
        if (!otpCode.getCode().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        validatePasswordPolicy(newPassword);

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new RuntimeException("New password must be different from your current password");
        }
        
        // Mark OTP as used
        otpCode.setUsed(true);
        otpCodeRepository.save(otpCode);
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        if (user.isLoginAlerts()) {
            notificationService.createNotification(user, "Password reset",
                    "Your account password was reset successfully.",
                    "SECURITY", "INFO");
        }
        
        return new AuthResponse(
                null,
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                false,
                "Password reset successfully");
    }

    /**
     * Toggle dark mode
     */
    public AuthResponse toggleDarkMode(String username, boolean darkMode) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        user.setDarkMode(darkMode);
        userRepository.save(user);
        
        return new AuthResponse(
                null,
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                false,
                darkMode ? "Dark mode enabled" : "Dark mode disabled");
    }

    /**
     * Update notification settings
     */
    public AuthResponse updateNotifications(String username, boolean emailNotifications, boolean loginAlerts) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        user.setEmailNotifications(emailNotifications);
        user.setLoginAlerts(loginAlerts);
        userRepository.save(user);
        
        return new AuthResponse(
                null,
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                false,
                "Notification settings updated");
    }

    /**
     * Trust the current device for reduced risk scoring
     */
    public AuthResponse trustDevice(String username, String deviceName) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        addTrustedDevice(user, deviceName);

        if (user.isLoginAlerts()) {
            notificationService.createNotification(user, "Device trusted",
                    "Device marked as trusted: " + (deviceName != null ? deviceName : "Unknown"),
                    "SECURITY", "INFO");
        }

        return new AuthResponse(
                null,
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                false,
                "Device trusted successfully");
    }

    private void validatePasswordPolicy(String password) {
        if (password == null
                || password.length() < 8
                || password.chars().noneMatch(Character::isUpperCase)
                || password.chars().noneMatch(Character::isLowerCase)
                || password.chars().noneMatch(Character::isDigit)) {
            throw new RuntimeException(PASSWORD_POLICY_MESSAGE);
        }
    }

    private User findUserByIdentifier(String identifier) {
        String normalizedIdentifier = normalizeIdentifier(identifier);
        return userRepository.findByUsernameOrEmail(normalizedIdentifier, normalizedIdentifier)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + normalizedIdentifier));
    }

    private String normalizeIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new RuntimeException("Username or email is required");
        }
        return identifier.trim();
    }

    private String registerFailedAttempt(
            User user,
            String ipAddress,
            String deviceName,
            String failureReason,
            String baseMessage) {
        int attempts = user.getFailedAttempts() + 1;
        user.setFailedAttempts(attempts);

        String message;
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(ACCOUNT_LOCK_MINUTES));
            message = baseMessage + " Too many failed attempts. Account locked for "
                    + ACCOUNT_LOCK_MINUTES + " minutes.";
        } else {
            int remainingAttempts = MAX_FAILED_ATTEMPTS - attempts;
            message = baseMessage + " " + remainingAttempts + " "
                    + (remainingAttempts == 1 ? "attempt" : "attempts")
                    + " remaining before a " + ACCOUNT_LOCK_MINUTES + "-minute lockout.";
        }

        userRepository.save(user);
        recordLoginHistory(user, false, ipAddress, deviceName, failureReason);
        return message;
    }

    private String buildLockedMessage(LocalDateTime lockedUntil) {
        if (lockedUntil == null) {
            return "Account is locked. Try again later.";
        }

        Duration remaining = Duration.between(LocalDateTime.now(), lockedUntil);
        if (remaining.isNegative() || remaining.isZero()) {
            return "Account is locked. Try again shortly.";
        }

        long totalSeconds = remaining.getSeconds();
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        if (minutes > 0) {
            return "Account is locked. Try again in " + minutes + "m " + seconds + "s.";
        }
        return "Account is locked. Try again in " + seconds + "s.";
    }
}
