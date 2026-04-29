package com.authsystem.model;

public class TotpSetupResponse {
    private String secret;
    private String otpauthUri;
    private String issuer;
    private String username;

    public TotpSetupResponse() {}

    public TotpSetupResponse(String secret, String otpauthUri, String issuer, String username) {
        this.secret = secret;
        this.otpauthUri = otpauthUri;
        this.issuer = issuer;
        this.username = username;
    }

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }

    public String getOtpauthUri() { return otpauthUri; }
    public void setOtpauthUri(String otpauthUri) { this.otpauthUri = otpauthUri; }

    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
