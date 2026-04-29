package com.authsystem.model;

public class ForgotPasswordRequest {
    private String identifier;

    public ForgotPasswordRequest() {}

    public ForgotPasswordRequest(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }
}
