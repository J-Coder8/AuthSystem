package com.authsystem.model;

public class FaceRegisterRequest {
    private String username;
    private String base64Image;

    public FaceRegisterRequest() {}

    public FaceRegisterRequest(String username, String base64Image) {
        this.username = username;
        this.base64Image = base64Image;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getBase64Image() { return base64Image; }
    public void setBase64Image(String base64Image) { this.base64Image = base64Image; }
}
