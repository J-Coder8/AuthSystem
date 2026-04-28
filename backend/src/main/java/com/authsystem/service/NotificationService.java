package com.authsystem.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.authsystem.dto.NotificationDto;
import com.authsystem.entity.Notification;
import com.authsystem.entity.User;
import com.authsystem.exception.UserNotFoundException;
import com.authsystem.repository.NotificationRepository;
import com.authsystem.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public void createNotification(User user, String title, String message, String type, String severity) {
        if (user == null) {
            return;
        }
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setSeverity(severity);
        notification.setRead(false);
        notificationRepository.save(notification);
    }

    public List<NotificationDto> getNotifications(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return notificationRepository.findTop50ByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return notificationRepository.countByUserAndReadFalse(user);
    }

    public void markRead(String username, Long id) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        Notification notification = notificationRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @SuppressWarnings("nullness")
    public void markAllRead(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        List<Notification> notifications = notificationRepository.findTop50ByUserOrderByCreatedAtDesc(user);
        for (Notification notification : notifications) {
            if (!notification.isRead()) {
                notification.setRead(true);
            }
        }
        notificationRepository.saveAll(notifications);
    }

    @SuppressWarnings("nullness")
    public void delete(String username, Long id) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        @SuppressWarnings("nullness")
        Notification notification = notificationRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notificationRepository.delete(notification);
    }

    private NotificationDto toDto(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.getSeverity(),
                notification.getCreatedAt(),
                notification.isRead()
        );
    }
}

