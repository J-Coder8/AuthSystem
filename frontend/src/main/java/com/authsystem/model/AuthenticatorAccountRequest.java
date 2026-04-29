package com.authsystem.model;

public class AuthenticatorAccountRequest {
    private String issuer;
    private String accountName;
    private String secret;

    public AuthenticatorAccountRequest() {}

    public AuthenticatorAccountRequest(String issuer, String accountName, String secret) {
        this.issuer = issuer;
        this.accountName = accountName;
        this.secret = secret;
    }

    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
}
