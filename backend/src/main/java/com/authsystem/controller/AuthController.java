package com.authsystem.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.authsystem.dto.AuthResponse;
import com.authsystem.dto.BackupCodesResponse;
import com.authsystem.dto.ChangePasswordRequest;
import com.authsystem.dto.ForgotPasswordRequest;
import com.authsystem.dto.LoginHistoryDto;
import com.authsystem.dto.LoginRequest;
import com.authsystem.dto.OtpRequest;
import com.authsystem.dto.OtpRequestOnly;
import com.authsystem.dto.RegisterRequest;
import com.authsystem.dto.ResetPasswordRequest;
import com.authsystem.dto.TotpSetupRequest;
import com.authsystem.dto.TotpSetupResponse;
import com.authsystem.dto.UpdateProfileRequest;
import com.authsystem.dto.UserProfileResponse;
import com.authsystem.security.JwtUtil;
import com.authsystem.service.TotpService;
import com.authsystem.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

private final UserService userService;
private final JwtUtil jwtUtil;
private final TotpService totpService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        String deviceName = getDeviceName(httpRequest);
        return ResponseEntity.ok(userService.login(request, ipAddress, deviceName));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(
            @Valid @RequestBody OtpRequest request,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        String deviceName = getDeviceName(httpRequest);
        return ResponseEntity.ok(userService.verifyOtp(request, ipAddress, deviceName));
    }

    @Transactional
    @PostMapping("/resend-otp")
    public ResponseEntity<AuthResponse> resendOtp(
            @Valid @RequestBody OtpRequest request) {
        return ResponseEntity.ok(userService.resendOtp(request.getUsername()));
    }

    /**
     * Request OTP by email or username - no password needed
     */
    @Transactional
    @PostMapping("/request-otp")
    public ResponseEntity<AuthResponse> requestOtp(
            @Valid @RequestBody OtpRequestOnly request) {
        return ResponseEntity.ok(userService.requestOtpByEmailOrUsername(request.getEmailOrUsername()));
    }

    /**
     * Get current TOTP code (for after successful login)
     */
    @GetMapping("/totp/code")
    public ResponseEntity<AuthResponse> getTotpCode(
            @RequestHeader("Authorization") String token) {
        String rawToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        if (!jwtUtil.validateToken(rawToken)) {
            return ResponseEntity.status(401).build();
        }
        String username = jwtUtil.extractUsername(rawToken);
        if (username == null || username.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(userService.getTotpCode(username));
    }

    @GetMapping("/history")
    public ResponseEntity<List<LoginHistoryDto>> getLoginHistory(
            @RequestHeader("Authorization") String token) {
        String rawToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        if (!jwtUtil.validateToken(rawToken)) {
            return ResponseEntity.status(401).build();
        }
        String username = jwtUtil.extractUsername(rawToken);
        if (username == null || username.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(userService.getLoginHistory(username));
    }

    // TOTP (Google Authenticator) endpoints
    @PostMapping("/totp/setup")
    public ResponseEntity<TotpSetupResponse> setupTotp(
            @RequestHeader("Authorization") String token) {
        String rawToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        if (!jwtUtil.validateToken(rawToken)) {
            return ResponseEntity.status(401).build();
        }
        String username = jwtUtil.extractUsername(rawToken);
        if (username == null || username.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        
        // Generate new TOTP secret
        String secret = userService.generateTotpSecret(username);
        String issuer = "AuthSystem";
        String otpauthUri = totpService.buildOtpAuthUri(issuer, username, secret);
        return ResponseEntity.ok(new TotpSetupResponse(secret, otpauthUri, issuer, username));
    }

    @PostMapping("/totp/enable")
    public ResponseEntity<AuthResponse> enableTotp(
            @RequestHeader("Authorization") String token,
            @RequestBody TotpSetupRequest request) {
        String rawToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        if (!jwtUtil.validateToken(rawToken)) {
            return ResponseEntity.status(401).build();
        }
        String username = jwtUtil.extractUsername(rawToken);
        if (username == null || username.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        
        // Get secret from request and verify with the provided code
        String secret = request.getSecret();
        String code = request.getCode();
        
        return ResponseEntity.ok(userService.enableTotp(username, secret, code));
    }

    @PostMapping("/totp/disable")
    public ResponseEntity<AuthResponse> disableTotp(
            @RequestHeader("Authorization") String token) {
        String rawToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        if (!jwtUtil.validateToken(rawToken)) {
            return ResponseEntity.status(401).build();
        }
        String username = jwtUtil.extractUsername(rawToken);
        if (username == null || username.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        
        return ResponseEntity.ok(userService.disableTotp(username));
    }

    @PostMapping("/backup-codes/generate")
    public ResponseEntity<BackupCodesResponse> generateBackupCodes(
            @RequestHeader("Authorization") String token) {
        String rawToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        if (!jwtUtil.validateToken(rawToken)) {
            return ResponseEntity.status(401).build();
        }
        String username = jwtUtil.extractUsername(rawToken);
        if (username == null || username.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(userService.generateBackupCodes(username));
    }

@GetMapping("/totp/remaining-seconds")
    public ResponseEntity<Integer> getTotpRemainingSeconds() {
        return ResponseEntity.ok(userService.getTotpRemainingSeconds());
    }

    // ========== User Profile Management ==========
    
    /**
     * Get current user's profile
     */
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(@RequestHeader("Authorization") String token) {
        String rawToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        if (!jwtUtil.validateToken(rawToken)) {
            return ResponseEntity.status(401).build();
        }
        String username = jwtUtil.extractUsername(rawToken);
        if (username == null || username.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(userService.getProfile(username));
    }

    /**
     * Update current user's profile
     */
    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody UpdateProfileRequest request) {
        String rawToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        if (!jwtUtil.validateToken(rawToken)) {
            return ResponseEntity.status(401).build();
        }
        String username = jwtUtil.extractUsername(rawToken);
        if (username == null || username.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(userService.updateProfile(username, request));
    }

    /**
     * Change password
     */
    @PostMapping("/change-password")
    public ResponseEntity<AuthResponse> changePassword(
            @RequestHeader("Authorization") String token,
            @RequestBody ChangePasswordRequest request) {
        String rawToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        if (!jwtUtil.validateToken(rawToken)) {
            return ResponseEntity.status(401).build();
        }
        String username = jwtUtil.extractUsername(rawToken);
        if (username == null || username.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(userService.changePassword(username, request));
    }

    /**
     * Update profile picture
     */
    @PostMapping("/profile/picture")
    public ResponseEntity<AuthResponse> updateProfilePicture(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> request) {
        String rawToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        if (!jwtUtil.validateToken(rawToken)) {
            return ResponseEntity.status(401).build();
        }
        String username = jwtUtil.extractUsername(rawToken);
        if (username == null || username.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        String profilePicture = request.get("profilePicture");
        return ResponseEntity.ok(userService.updateProfilePicture(username, profilePicture));
    }

    // ========== Session Management ==========

    /**
     * Get active sessions
     */
    @GetMapping("/sessions")
    public ResponseEntity<List<Map<String, Object>>> getActiveSessions(@RequestHeader("Authorization") String token) {
        String rawToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        if (!jwtUtil.validateToken(rawToken)) {
            return ResponseEntity.status(401).build();
        }
        String username = jwtUtil.extractUsername(rawToken);
        if (username == null || username.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(userService.getActiveSessions(username));
    }

    /**
     * Revoke a specific session
     */
    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<AuthResponse> revokeSession(
            @RequestHeader("Authorization") String token,
            @PathVariable String sessionId) {
        String rawToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        if (!jwtUtil.validateToken(rawToken)) {
            return ResponseEntity.status(401).build();
        }
        String username = jwtUtil.extractUsername(rawToken);
        if (username == null || username.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(userService.revokeSession(username, sessionId));
    }

    /**
     * Logout from all devices
     */
    @PostMapping("/logout-all")
    public ResponseEntity<AuthResponse> logoutFromAllDevices(@RequestHeader("Authorization") String token) {
        String rawToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        if (!jwtUtil.validateToken(rawToken)) {
            return ResponseEntity.status(401).build();
        }
        String username = jwtUtil.extractUsername(rawToken);
        if (username == null || username.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(userService.logoutFromAllDevices(username, rawToken));
    }

// ========== Account Recovery ==========

    /**
     * Request password reset OTP
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<AuthResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(userService.forgotPassword(request.getIdentifier()));
    }

    /**
     * Reset password with OTP
     */
    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(userService.resetPassword(request));
    }

    // ========== Settings ==========

    /**
     * Toggle dark mode
     */
    @PostMapping("/settings/dark-mode")
    public ResponseEntity<AuthResponse> toggleDarkMode(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Boolean> request) {
        String rawToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        if (!jwtUtil.validateToken(rawToken)) {
            return ResponseEntity.status(401).build();
        }
        String username = jwtUtil.extractUsername(rawToken);
        if (username == null || username.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        Boolean darkMode = request.get("darkMode");
        return ResponseEntity.ok(userService.toggleDarkMode(username, darkMode != null && darkMode));
    }

    /**
     * Update email notifications
     */
    @PostMapping("/settings/notifications")
    public ResponseEntity<AuthResponse> updateNotifications(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Boolean> request) {
        String rawToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        if (!jwtUtil.validateToken(rawToken)) {
            return ResponseEntity.status(401).build();
        }
        String username = jwtUtil.extractUsername(rawToken);
        if (username == null || username.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        Boolean emailNotifications = request.get("emailNotifications");
        Boolean loginAlerts = request.get("loginAlerts");
        return ResponseEntity.ok(userService.updateNotifications(username, 
                emailNotifications != null ? emailNotifications : true,
                loginAlerts != null ? loginAlerts : true));
    }

    /**
     * Trust current device for reduced risk scoring
     */
    @PostMapping("/settings/trust-device")
    public ResponseEntity<AuthResponse> trustDevice(@RequestHeader("Authorization") String token,
            HttpServletRequest httpRequest) {
        String rawToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        if (!jwtUtil.validateToken(rawToken)) {
            return ResponseEntity.status(401).build();
        }
        String username = jwtUtil.extractUsername(rawToken);
        if (username == null || username.isBlank()) {
            return ResponseEntity.status(401).build();
        }

        String deviceName = getDeviceName(httpRequest);
        return ResponseEntity.ok(userService.trustDevice(username, deviceName));
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String getDeviceName(HttpServletRequest request) {
        String deviceName = request.getHeader("X-Device-Name");
        if (deviceName == null || deviceName.isBlank()) {
            deviceName = request.getHeader("User-Agent");
        }
        return deviceName != null && !deviceName.isBlank() ? deviceName : "Unknown Device";
    }
}
