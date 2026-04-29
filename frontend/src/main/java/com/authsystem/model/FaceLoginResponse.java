package com.authsystem.model;

public class FaceLoginResponse {
    private String username;
    private String email;
    private String fullName;
    private boolean faceMatched;
    private boolean requiresOtp;
    private boolean requiresFace;
    private boolean requiresTotp;
    private String message;
    private String tempToken;

    public FaceLoginResponse() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public boolean isFaceMatched() { return faceMatched; }
    public void setFaceMatched(boolean faceMatched) { this.faceMatched = faceMatched; }

    public boolean isRequiresOtp() { return requiresOtp; }
    public void setRequiresOtp(boolean requiresOtp) { this.requiresOtp = requiresOtp; }

    public boolean isRequiresFace() { return requiresFace; }
    public void setRequiresFace(boolean requiresFace) { this.requiresFace = requiresFace; }

    public boolean isRequiresTotp() { return requiresTotp; }
    public void setRequiresTotp(boolean requiresTotp) { this.requiresTotp = requiresTotp; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTempToken() { return tempToken; }
    public void setTempToken(String tempToken) { this.tempToken = tempToken; }
}
