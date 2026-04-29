package com.authsystem.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.authsystem.model.AuthResponse;
import com.authsystem.model.BackupCodesResponse;
import com.authsystem.model.ChangePasswordRequest;
import com.authsystem.model.LoginHistoryDto;
import com.authsystem.model.SessionInfo;
import com.authsystem.model.TotpSetupRequest;
import com.authsystem.model.TotpSetupResponse;
import com.authsystem.model.UpdateProfileRequest;
import com.authsystem.model.UserProfileResponse;
import com.authsystem.service.ApiService.ApiResponse;
import com.google.gson.reflect.TypeToken;

public class UserService {

    private final ApiService apiService;

    public UserService() {
        this.apiService = new ApiService();
    }

    public ApiResponse<UserProfileResponse> getProfile(String token) {
        return apiService.get("/auth/profile", UserProfileResponse.class, token);
    }

    public ApiResponse<UserProfileResponse> updateProfile(UpdateProfileRequest request, String token) {
        return apiService.put("/auth/profile", request, UserProfileResponse.class, token);
    }

    public ApiResponse<Void> changePassword(String currentPassword, String newPassword, String token) {
        ChangePasswordRequest request = new ChangePasswordRequest(currentPassword, newPassword);
        return apiService.post("/auth/change-password", request, Void.class, token);
    }

    public ApiResponse<List<LoginHistoryDto>> getLoginHistory(String token) {
        return apiService.getList("/auth/history", new TypeToken<List<LoginHistoryDto>>() {}, token);
    }

public ApiResponse<List<SessionInfo>> getSessions(String token) {
        // Backend returns List<Map<String, Object>> with keys: sessionId, device, ipAddress, lastActive (String), current (Boolean)
        ApiResponse<List<Map<String, Object>>> rawResponse = apiService.getList("/auth/sessions", new TypeToken<List<Map<String, Object>>>() {}, token);
        if (!rawResponse.isSuccess() || rawResponse.getData() == null) {
            return ApiResponse.error(rawResponse.getErrorMessage(), rawResponse.getStatusCode());
        }
        List<SessionInfo> sessions = new ArrayList<>();
        for (Map<String, Object> map : rawResponse.getData()) {
            SessionInfo session = new SessionInfo();
            session.setSessionId(map.get("sessionId") != null ? String.valueOf(map.get("sessionId")) : "");
            session.setDeviceName(map.get("device") != null ? String.valueOf(map.get("device")) : "");
            session.setIpAddress(map.get("ipAddress") != null ? String.valueOf(map.get("ipAddress")) : "");
            Object lastActive = map.get("lastActive");
            if (lastActive != null) {
                try {
                    session.setLastActiveAt(LocalDateTime.parse(String.valueOf(lastActive)));
                } catch (Exception e) {
                    // lastActive might be "N/A" or an unparsable string
                    session.setLastActiveAt(null);
                }
            }
            Object current = map.get("current");
            if (current instanceof Boolean) {
                session.setCurrent((Boolean) current);
            } else if (current != null) {
                session.setCurrent(Boolean.parseBoolean(String.valueOf(current)));
            }
            sessions.add(session);
        }
        return ApiResponse.success(sessions, rawResponse.getStatusCode());
    }


    public ApiResponse<Void> revokeSession(String sessionId, String token) {
        return apiService.delete("/auth/sessions/" + sessionId, Void.class, token);
    }

    public ApiResponse<Void> logoutAllDevices(String token) {
        return apiService.post("/auth/logout-all", null, Void.class, token);
    }

    public ApiResponse<BackupCodesResponse> generateBackupCodes(String token) {
        return apiService.post("/auth/backup-codes/generate", null, BackupCodesResponse.class, token);
    }

    public ApiResponse<TotpSetupResponse> setupTotp(String token) {
        return apiService.post("/auth/totp/setup", null, TotpSetupResponse.class, token);
    }

    public ApiResponse<AuthResponse> enableTotp(String secret, String code, String token) {
        return apiService.post("/auth/totp/enable", new TotpSetupRequest(secret, code), AuthResponse.class, token);
    }

    public ApiResponse<AuthResponse> disableTotp(String token) {
        return apiService.post("/auth/totp/disable", null, AuthResponse.class, token);
    }

    public ApiResponse<Integer> getTotpRemainingSeconds() {
        return apiService.get("/auth/totp/remaining-seconds", Integer.class);
    }

    public ApiResponse<AuthResponse> getTotpCode(String token) {
        return apiService.get("/auth/totp/code", AuthResponse.class, token);
    }

    public ApiResponse<Void> updateDarkMode(boolean darkMode, String token) {
        return apiService.post("/auth/settings/dark-mode", new DarkModeRequest(darkMode), Void.class, token);
    }

    public ApiResponse<Void> updateNotifications(boolean emailNotifications, boolean loginAlerts, String token) {
        return apiService.post("/auth/settings/notifications", new NotificationsRequest(emailNotifications, loginAlerts), Void.class, token);
    }

    public ApiResponse<Void> trustDevice(String token) {
        return apiService.post("/auth/settings/trust-device", null, Void.class, token);
    }

    @SuppressWarnings("unused")
    private static class DarkModeRequest {
        private final boolean darkMode;
        DarkModeRequest(boolean darkMode) { this.darkMode = darkMode; }
        public boolean isDarkMode() { return darkMode; }
    }

    @SuppressWarnings("unused")
    private static class NotificationsRequest {
        private final boolean emailNotifications;
        private final boolean loginAlerts;
        NotificationsRequest(boolean emailNotifications, boolean loginAlerts) {
            this.emailNotifications = emailNotifications;
            this.loginAlerts = loginAlerts;
        }
        public boolean isEmailNotifications() { return emailNotifications; }
        public boolean isLoginAlerts() { return loginAlerts; }
    }
}
