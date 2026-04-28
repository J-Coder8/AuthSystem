package com.authsystem.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import jakarta.validation.constraints.NotBlank;

public class ForgotPasswordRequest {

    @JsonAlias("email")
    @NotBlank(message = "Username or email is required")
    private String identifier;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
