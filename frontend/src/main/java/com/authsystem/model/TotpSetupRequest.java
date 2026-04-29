package com.authsystem.model;

public class TotpSetupRequest {
    private String secret;
    private String code;

    public TotpSetupRequest() {}

    public TotpSetupRequest(String secret, String code) {
        this.secret = secret;
        this.code = code;
    }

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
