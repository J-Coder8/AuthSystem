package com.authsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class FaceLoginResponse {

    private String username;
    private String email;
    private String fullName;
    private boolean faceMatched;
    private boolean requiresOtp;
    private boolean requiresFace;
    private boolean requiresTotp;
    private String message;
    private String tempToken;  // Temporary token for completing login with OTP
}
