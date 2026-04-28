package com.authsystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OtpRequestOnly {

    @NotBlank(message = "Email or username is required")
    private String emailOrUsername;
    
    public String getEmailOrUsername() {
        return this.emailOrUsername;
    }
}

