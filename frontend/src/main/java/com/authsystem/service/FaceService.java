package com.authsystem.service;

import com.authsystem.model.AuthResponse;
import com.authsystem.model.FaceLoginResponse;
import com.authsystem.model.FaceRegisterRequest;
import com.authsystem.model.FaceVerifyRequest;
import com.authsystem.service.ApiService.ApiResponse;

public class FaceService {

    private final ApiService apiService;

    public FaceService() {
        this.apiService = new ApiService();
    }

    public ApiResponse<FaceLoginResponse> verifyFace(String username, String base64Image) {
        FaceVerifyRequest request = new FaceVerifyRequest(username, base64Image);
        return apiService.post("/auth/face/verify", request, FaceLoginResponse.class);
    }

    public ApiResponse<Void> registerFace(String username, String base64Image, String token) {
        FaceRegisterRequest request = new FaceRegisterRequest(username, base64Image);
        return apiService.post("/auth/face/register", request, Void.class, token);
    }

    public ApiResponse<Void> deleteFace(String token) {
        return apiService.delete("/auth/face", Void.class, token);
    }

    public ApiResponse<Void> enableFaceLogin(String token) {
        return apiService.post("/auth/face/enable", null, Void.class, token);
    }

    public ApiResponse<Void> disableFaceLogin(String token) {
        return apiService.post("/auth/face/disable", null, Void.class, token);
    }

    public ApiResponse<AuthResponse> getFaceStatus(String token) {
        return apiService.get("/auth/face/status", AuthResponse.class, token);
    }
}
