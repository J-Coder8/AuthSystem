package com.authsystem.service;

import com.authsystem.model.AuthResponse;
import com.authsystem.model.BackupCodesResponse;
import com.authsystem.model.TotpSetupRequest;
import com.authsystem.model.TotpSetupResponse;
import com.authsystem.service.ApiService.ApiResponse;

public class TotpService {

    private final ApiService apiService;

    public TotpService() {
        this.apiService = new ApiService();
    }

    public ApiResponse<TotpSetupResponse> setupTotp(String token) {
        return apiService.post("/totp/setup", null, TotpSetupResponse.class, token);
    }

    public ApiResponse<AuthResponse> verifyTotpSetup(String secret, String code, String token) {
        TotpSetupRequest request = new TotpSetupRequest(secret, code);
        return apiService.post("/totp/setup/verify", request, AuthResponse.class, token);
    }

    public ApiResponse<Void> disableTotp(String token) {
        return apiService.post("/totp/disable", null, Void.class, token);
    }

    public ApiResponse<BackupCodesResponse> generateBackupCodes(String token) {
        return apiService.post("/totp/backup-codes", null, BackupCodesResponse.class, token);
    }
}
