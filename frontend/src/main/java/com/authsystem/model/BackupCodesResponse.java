package com.authsystem.model;

import java.util.List;
import java.time.LocalDateTime;

public class BackupCodesResponse {
    private List<String> codes;
    private int remaining;
    private LocalDateTime generatedAt;
    private String message;

    public BackupCodesResponse() {}

    public BackupCodesResponse(List<String> codes, String message) {
        this.codes = codes;
        this.message = message;
    }

    public List<String> getCodes() { return codes; }
    public void setCodes(List<String> codes) { this.codes = codes; }

    public List<String> getBackupCodes() { return codes; }
    public void setBackupCodes(List<String> backupCodes) { this.codes = backupCodes; }

    public int getRemaining() { return remaining; }
    public void setRemaining(int remaining) { this.remaining = remaining; }

    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
