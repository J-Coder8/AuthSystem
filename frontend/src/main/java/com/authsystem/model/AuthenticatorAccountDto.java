package com.authsystem.model;

import java.time.LocalDateTime;

public class AuthenticatorAccountDto {
    private Long id;
    private String issuer;
    private String accountName;
    private LocalDateTime createdAt;
    private String code;
    private Integer remainingSeconds;

    public AuthenticatorAccountDto() {}

    public AuthenticatorAccountDto(Long id, String issuer, String accountName,
                                   LocalDateTime createdAt, String code, Integer remainingSeconds) {
        this.id = id;
        this.issuer = issuer;
        this.accountName = accountName;
        this.createdAt = createdAt;
        this.code = code;
        this.remainingSeconds = remainingSeconds;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Integer getRemainingSeconds() { return remainingSeconds; }
    public void setRemainingSeconds(Integer remainingSeconds) { this.remainingSeconds = remainingSeconds; }
}
