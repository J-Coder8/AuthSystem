package com.authsystem.service;

import java.util.List;

import com.authsystem.model.NotificationDto;
import com.authsystem.service.ApiService.ApiResponse;
import com.google.gson.reflect.TypeToken;

public class NotificationService {

    private final ApiService apiService;

    public NotificationService() {
        this.apiService = new ApiService();
    }

    public ApiResponse<List<NotificationDto>> getNotifications(String token) {
        return apiService.getList("/auth/notifications", new TypeToken<List<NotificationDto>>() {}, token);
    }

    public ApiResponse<Long> getUnreadCount(String token) {
        // Count unread notifications from the list
        ApiResponse<List<NotificationDto>> response = getNotifications(token);
        if (response.isSuccess() && response.getData() != null) {
            long count = response.getData().stream().filter(n -> !n.isRead()).count();
            return ApiResponse.success(count, 200);
        }
        return ApiResponse.error(response.getErrorMessage(), response.getStatusCode());
    }

    public ApiResponse<Void> markAsRead(Long notificationId, String token) {
        return apiService.post("/auth/notifications/" + notificationId + "/read", null, Void.class, token);
    }

    public ApiResponse<Void> markAllAsRead(String token) {
        return apiService.post("/auth/notifications/read-all", null, Void.class, token);
    }

    public ApiResponse<Void> deleteNotification(Long notificationId, String token) {
        return apiService.delete("/auth/notifications/" + notificationId, Void.class, token);
    }
}
