package com.authsystem.service;

import com.authsystem.model.AuthenticatorAccountDto;
import com.authsystem.model.AuthenticatorAccountRequest;
import com.authsystem.service.ApiService.ApiResponse;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class AuthenticatorService {

    private final ApiService apiService;

    public AuthenticatorService() {
        this.apiService = new ApiService();
    }

    public ApiResponse<List<AuthenticatorAccountDto>> getAccounts(String token) {
        return apiService.getList("/auth/authenticator/accounts", new TypeToken<List<AuthenticatorAccountDto>>() {}, token);
    }

    public ApiResponse<AuthenticatorAccountDto> addAccount(AuthenticatorAccountRequest request, String token) {
        return apiService.post("/auth/authenticator/accounts", request, AuthenticatorAccountDto.class, token);
    }

    public ApiResponse<Void> deleteAccount(Long accountId, String token) {
        return apiService.delete("/auth/authenticator/accounts/" + accountId, Void.class, token);
    }

    public ApiResponse<List<AuthenticatorAccountDto>> refreshCodes(String token) {
        return apiService.getList("/auth/authenticator/codes", new TypeToken<List<AuthenticatorAccountDto>>() {}, token);
    }
}
