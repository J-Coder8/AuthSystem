package com.authsystem.service;

import com.authsystem.model.AuthResponse;
import com.authsystem.model.FaceLoginResponse;
import com.authsystem.model.FaceVerifyRequest;
import com.authsystem.model.LoginRequest;
import com.authsystem.model.OtpRequest;
import com.authsystem.model.OtpRequestOnly;
import com.authsystem.model.RegisterRequest;
import com.authsystem.model.ResetPasswordRequest;
import com.authsystem.service.ApiService.ApiResponse;

public class AuthService {

    private final ApiService apiService;

    public AuthService() {
        this.apiService = new ApiService();
    }

    public ApiResponse<AuthResponse> login(String username, String password) {
        LoginRequest request = new LoginRequest(username, password);
        return apiService.post("/auth/login", request, AuthResponse.class);
    }

    public ApiResponse<AuthResponse> verifyOtp(String username, String otpCode) {
        OtpRequest request = new OtpRequest(username, otpCode);
        return apiService.post("/auth/verify-otp", request, AuthResponse.class);
    }

    public ApiResponse<AuthResponse> resendOtp(String username) {
        return apiService.post("/auth/request-otp", new OtpRequestOnly(username), AuthResponse.class);
    }

    public ApiResponse<AuthResponse> register(String username, String email, String fullName, String password) {
        RegisterRequest request = new RegisterRequest(username, email, fullName, password);
        return apiService.post("/auth/register", request, AuthResponse.class);
    }

    public ApiResponse<Void> forgotPassword(String identifier) {
        com.authsystem.model.ForgotPasswordRequest request = new com.authsystem.model.ForgotPasswordRequest(identifier);
        return apiService.post("/auth/forgot-password", request, Void.class);
    }

    public ApiResponse<Void> resetPassword(String identifier, String otpCode, String newPassword) {
        ResetPasswordRequest request = new ResetPasswordRequest(identifier, otpCode, newPassword);
        return apiService.post("/auth/reset-password", request, Void.class);
    }

    public ApiResponse<FaceLoginResponse> verifyFace(String username, String base64Image) {
        FaceVerifyRequest request = new FaceVerifyRequest(username, base64Image);
        return apiService.post("/auth/face/verify", request, FaceLoginResponse.class);
    }

    public ApiResponse<Void> logout(String token) {
        return apiService.post("/auth/logout", null, Void.class, token);
    }

    public ApiResponse<Void> logoutAllDevices(String token) {
        return apiService.post("/auth/logout-all", null, Void.class, token);
    }
}
