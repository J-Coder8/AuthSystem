package com.authsystem.model;

import java.time.LocalDateTime;
import java.time.Duration;

public class SessionInfo {
    private String sessionId;
    private String deviceName;
    private String ipAddress;
    private LocalDateTime createdAt;
    private LocalDateTime lastActiveAt;
    private boolean current;

    public SessionInfo() {}

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastActiveAt() { return lastActiveAt; }
    public void setLastActiveAt(LocalDateTime lastActiveAt) { this.lastActiveAt = lastActiveAt; }

    public boolean isCurrent() { return current; }
    public void setCurrent(boolean current) { this.current = current; }

    public String getDevice() {
        return deviceName != null ? deviceName : "Unknown";
    }

    public String getLastActive() {
        return lastActiveAt != null ? lastActiveAt.toString() : "Unknown";
    }

    public String getActiveFor() {
        if (lastActiveAt == null) {
            return "Unknown";
        }
        long minutes = Math.max(0, Duration.between(lastActiveAt, LocalDateTime.now()).toMinutes());
        if (minutes < 1) {
            return "Less than 1 minute";
        }
        if (minutes < 60) {
            return minutes + " minute" + (minutes == 1 ? "" : "s");
        }
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;
        if (hours < 24) {
            return hours + " hour" + (hours == 1 ? "" : "s")
                    + (remainingMinutes > 0 ? " " + remainingMinutes + " min" : "");
        }
        long days = hours / 24;
        return days + " day" + (days == 1 ? "" : "s");
    }
}
