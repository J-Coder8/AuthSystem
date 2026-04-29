package com.authsystem.model;

public class OtpRequestOnly {
    private String emailOrUsername;

    public OtpRequestOnly() {}

    public OtpRequestOnly(String emailOrUsername) {
        this.emailOrUsername = emailOrUsername;
    }

    public String getEmailOrUsername() { return emailOrUsername; }
    public void setEmailOrUsername(String emailOrUsername) { this.emailOrUsername = emailOrUsername; }
}
