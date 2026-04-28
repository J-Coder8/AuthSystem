package com.authsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
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
    

    
    // Constructor for 7 args (token, username, email, fullName, role, requiresOtp, message)
    public AuthResponse(String token, String username, String email, String fullName, String role, boolean requiresOtp, String message) {
        this.token = token;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.requiresOtp = requiresOtp;
        this.requiresTotp = false;
        this.message = message;
        this.requiresFace = false;
        this.totpCode = null;
        this.totpRemainingSeconds = null;
    }
    
    // 6 args with token null (no role)
    public AuthResponse(String token, String username, String email, String fullName, boolean requiresOtp, String message) {
        this.token = token;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.role = null;
        this.requiresOtp = requiresOtp;
        this.requiresTotp = false;
        this.message = message;
        this.requiresFace = false;
        this.totpCode = null;
        this.totpRemainingSeconds = null;
    }
    
    // Constructor for 9 args
    public AuthResponse(String token, String username, String email, String fullName, String role, boolean requiresOtp, String message, String totpCode, Integer totpRemainingSeconds) {
        this.token = token;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.requiresOtp = requiresOtp;
        this.requiresTotp = false;
        this.message = message;
        this.totpCode = totpCode;
        this.totpRemainingSeconds = totpRemainingSeconds;
        this.requiresFace = false;
    }
    
    // Constructor for 5 args
    public AuthResponse(String username, String email, String fullName, boolean requiresOtp, String message) {
        this.token = null;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.role = null;
        this.requiresOtp = requiresOtp;
        this.requiresTotp = false;
        this.message = message;
        this.requiresFace = false;
        this.totpCode = null;
        this.totpRemainingSeconds = null;
    }
}

