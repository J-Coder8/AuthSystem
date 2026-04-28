package com.authsystem.dto;

public class AuthenticatorAccountRequest {
    private String issuer;
    private String accountName;
    private String secret;

    public AuthenticatorAccountRequest() {}

    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
}
