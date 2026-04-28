package com.authsystem.dto;

import java.time.LocalDateTime;
import java.util.List;

public class BackupCodesResponse {
    private List<String> codes;
    private int remaining;
    private LocalDateTime generatedAt;

    public BackupCodesResponse() {}

    public BackupCodesResponse(List<String> codes, int remaining, LocalDateTime generatedAt) {
        this.codes = codes;
        this.remaining = remaining;
        this.generatedAt = generatedAt;
    }

    public List<String> getCodes() {
        return codes;
    }

    public void setCodes(List<String> codes) {
        this.codes = codes;
    }

    public int getRemaining() {
        return remaining;
    }

    public void setRemaining(int remaining) {
        this.remaining = remaining;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}
