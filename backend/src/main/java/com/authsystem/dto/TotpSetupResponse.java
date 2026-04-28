package com.authsystem.dto;

public class TotpSetupResponse {

    private String secret;
    private String otpauthUri;
    private String issuer;
    private String accountName;

    public TotpSetupResponse() {}

    public TotpSetupResponse(String secret, String otpauthUri, String issuer, String accountName) {
        this.secret = secret;
        this.otpauthUri = otpauthUri;
        this.issuer = issuer;
        this.accountName = accountName;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getOtpauthUri() {
        return otpauthUri;
    }

    public void setOtpauthUri(String otpauthUri) {
        this.otpauthUri = otpauthUri;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
}
