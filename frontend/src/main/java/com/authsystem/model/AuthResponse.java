package com.authsystem.model;

public class AuthResponse {
    private String token;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private boolean requiresOtp;
    private boolean requiresTotp;
    private boolean requiresFace;
    private String message;
    private String totpCode;
    private Integer totpRemainingSeconds;
    private String deliveryEmail;
    private boolean darkMode;

    public AuthResponse() {}

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isRequiresOtp() { return requiresOtp; }
    public void setRequiresOtp(boolean requiresOtp) { this.requiresOtp = requiresOtp; }

    public boolean isRequiresTotp() { return requiresTotp; }
    public void setRequiresTotp(boolean requiresTotp) { this.requiresTotp = requiresTotp; }

    public boolean isRequiresFace() { return requiresFace; }
    public void setRequiresFace(boolean requiresFace) { this.requiresFace = requiresFace; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTotpCode() { return totpCode; }
    public void setTotpCode(String totpCode) { this.totpCode = totpCode; }

    public Integer getTotpRemainingSeconds() { return totpRemainingSeconds; }
    public void setTotpRemainingSeconds(Integer totpRemainingSeconds) { this.totpRemainingSeconds = totpRemainingSeconds; }

    public String getDeliveryEmail() { return deliveryEmail; }
    public void setDeliveryEmail(String deliveryEmail) { this.deliveryEmail = deliveryEmail; }

    public boolean isDarkMode() { return darkMode; }
    public void setDarkMode(boolean darkMode) { this.darkMode = darkMode; }
}
