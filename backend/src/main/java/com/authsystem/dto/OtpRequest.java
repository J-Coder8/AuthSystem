package com.authsystem.dto;

import jakarta.validation.constraints.NotBlank;

public class OtpRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "OTP code is required")
    private String code;

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}

