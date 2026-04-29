package com.authsystem.model;

import java.time.LocalDateTime;

public class LoginHistoryDto {
    private Long id;
    private LocalDateTime loginTime;
    private String ipAddress;
    private String userAgent;
    private boolean success;
    private String failureReason;
    private Integer riskScore;
    private String riskFactors;

    public LoginHistoryDto() {}

    public LoginHistoryDto(Long id, LocalDateTime loginTime, String ipAddress, String userAgent,
                           boolean success, String failureReason, Integer riskScore, String riskFactors) {
        this.id = id;
        this.loginTime = loginTime;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.success = success;
        this.failureReason = failureReason;
        this.riskScore = riskScore;
        this.riskFactors = riskFactors;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getLoginTime() { return loginTime; }
    public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public Integer getRiskScore() { return riskScore; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }

    public String getRiskFactors() { return riskFactors; }
    public void setRiskFactors(String riskFactors) { this.riskFactors = riskFactors; }
}
