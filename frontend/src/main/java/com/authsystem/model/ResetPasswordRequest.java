package com.authsystem.model;

public class ResetPasswordRequest {
    private String identifier;
    private String otpCode;
    private String newPassword;

    public ResetPasswordRequest() {}

    public ResetPasswordRequest(String identifier, String otpCode, String newPassword) {
        this.identifier = identifier;
        this.otpCode = otpCode;
        this.newPassword = newPassword;
    }

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public String getOtpCode() { return otpCode; }
    public void setOtpCode(String otpCode) { this.otpCode = otpCode; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
