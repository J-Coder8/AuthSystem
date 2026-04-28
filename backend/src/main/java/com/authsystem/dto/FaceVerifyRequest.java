package com.authsystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FaceVerifyRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Base64 image is required")
    private String base64Image;  // Base64 encoded face image for verification
}
