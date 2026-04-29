package com.authsystem.service;

import java.util.List;

import com.authsystem.model.AdminUserDto;
import com.authsystem.service.ApiService.ApiResponse;
import com.google.gson.reflect.TypeToken;

public class AdminService {

    private final ApiService apiService;

    public AdminService() {
        this.apiService = new ApiService();
    }

    public ApiResponse<List<AdminUserDto>> getAllUsers(String token) {
        return apiService.getList("/admin/users", new TypeToken<List<AdminUserDto>>() {}, token);
    }

    public ApiResponse<Void> enableUser(Long userId, String token) {
        return apiService.post("/admin/users/" + userId + "/enable", null, Void.class, token);
    }

    public ApiResponse<Void> disableUser(Long userId, String token) {
        return apiService.post("/admin/users/" + userId + "/disable", null, Void.class, token);
    }

    public ApiResponse<Void> deleteUser(Long userId, String token) {
        return apiService.delete("/admin/users/" + userId, Void.class, token);
    }

    public ApiResponse<Void> updateUserRole(Long userId, String role, String token) {
        return apiService.put("/admin/users/" + userId + "/role", new RoleRequest(role), Void.class, token);
    }

    @SuppressWarnings("unused")
    private static class RoleRequest {
        private final String role;
        RoleRequest(String role) { this.role = role; }
        public String getRole() { return role; }
    }
}
