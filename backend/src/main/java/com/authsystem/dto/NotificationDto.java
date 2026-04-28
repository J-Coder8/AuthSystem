package com.authsystem.dto;

import java.time.LocalDateTime;

public class NotificationDto {
    private Long id;
    private String title;
    private String message;
    private String type;
    private String severity;
    private LocalDateTime createdAt;
    private boolean read;

    public NotificationDto() {}

    public NotificationDto(Long id, String title, String message, String type, String severity,
                           LocalDateTime createdAt, boolean read) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type;
        this.severity = severity;
        this.createdAt = createdAt;
        this.read = read;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
}

