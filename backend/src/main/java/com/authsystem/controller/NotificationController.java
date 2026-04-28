package com.authsystem.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.authsystem.dto.NotificationDto;
import com.authsystem.security.JwtUtil;
import com.authsystem.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<List<NotificationDto>> getNotifications(@RequestHeader("Authorization") String token) {
        String username = extractUsername(token);
        if (username == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(notificationService.getNotifications(username));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@RequestHeader("Authorization") String token) {
        String username = extractUsername(token);
        if (username == null) {
            return ResponseEntity.status(401).build();
        }
        long count = notificationService.getUnreadCount(username);
        return ResponseEntity.ok(Map.of("unread", count));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@RequestHeader("Authorization") String token,
                                         @PathVariable Long id) {
        String username = extractUsername(token);
        if (username == null) {
            return ResponseEntity.status(401).build();
        }
        notificationService.markRead(username, id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllRead(@RequestHeader("Authorization") String token) {
        String username = extractUsername(token);
        if (username == null) {
            return ResponseEntity.status(401).build();
        }
        notificationService.markAllRead(username);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@RequestHeader("Authorization") String token,
                                       @PathVariable Long id) {
        String username = extractUsername(token);
        if (username == null) {
            return ResponseEntity.status(401).build();
        }
        notificationService.delete(username, id);
        return ResponseEntity.ok().build();
    }

    private String extractUsername(String token) {
        String rawToken = token != null && token.startsWith("Bearer ") ? token.substring(7) : token;
        if (rawToken == null || !jwtUtil.validateToken(rawToken)) {
            return null;
        }
        return jwtUtil.extractUsername(rawToken);
    }
}

