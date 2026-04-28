package com.authsystem.config;

import org.springframework.stereotype.Component;

@Component
public class DebugProperties {
    
    private boolean forceEmailOtp = false;
    private boolean printTotp = false;
    
    public boolean isForceEmailOtp() {
        return forceEmailOtp;
    }
    
    public void setForceEmailOtp(boolean forceEmailOtp) {
        this.forceEmailOtp = forceEmailOtp;
    }
    
    public boolean isPrintTotp() {
        return printTotp;
    }
    
    public void setPrintTotp(boolean printTotp) {
        this.printTotp = printTotp;
    }
}

