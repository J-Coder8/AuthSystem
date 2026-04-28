package com.authsystem.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.authsystem.dto.AuthResponse;
import com.authsystem.dto.FaceLoginResponse;
import com.authsystem.dto.FaceRegisterRequest;
import com.authsystem.dto.FaceVerifyRequest;
import com.authsystem.security.JwtUtil;
import com.authsystem.service.FaceService;
import com.authsystem.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth/face")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FaceController {

    private final FaceService faceService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * Register user's face
     * POST /api/auth/face/register
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerFace(
            @Valid @RequestBody FaceRegisterRequest request,
            @RequestHeader("Authorization") String token) {
        
        String rawToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        if (!jwtUtil.validateToken(rawToken)) {
            return ResponseEntity.status(401).build();
        }
        String username = jwtUtil.extractUsername(rawToken);
        if (username == null || username.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        
        // Verify the user is registering their own face
        if (!username.equals(request.getUsername())) {
            return ResponseEntity.status(403).body(AuthResponse.builder()
                    .message("Cannot register face for another user")
                    .build());
        }
        
        try {
            AuthResponse response = faceService.registerFace(request.getUsername(), request.getBase64Image());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(AuthResponse.builder()
                    .message("Face registration failed: " + e.getMessage())
                    .build());
        }
    }

    /**
     * Verify face for login
     * POST /api/auth/face/verify
     */
    @PostMapping("/verify")
    public ResponseEntity<FaceLoginResponse> verifyFace(@Valid @RequestBody FaceVerifyRequest request) {
        try {
            // First verify the face
            boolean matched = faceService.isFaceMatch(request.getUsername(), request.getBase64Image());
            
            if (matched) {
                // Send OTP to user's email
                AuthResponse otpResponse = userService.requestOtpByEmailOrUsername(request.getUsername());
                boolean requiresTotp = otpResponse.isRequiresTotp();
                String message = requiresTotp
                        ? "Face verified. Enter your TOTP code to complete login."
                        : "Face verified. Please enter OTP to complete login.";
                
                logger.info("Face verification successful for user: {}. OTP sent.", request.getUsername());

                return ResponseEntity.ok(FaceLoginResponse.builder()
                        .username(request.getUsername())
                        .email(otpResponse.getEmail())
                        .fullName(otpResponse.getFullName())
                        .faceMatched(true)
                        .requiresOtp(true)
                        .requiresFace(false)
                        .requiresTotp(requiresTotp)
                        .message(message)
                        .build());
            } else {
                logger.warn("Face verification failed for user: {}", request.getUsername());
                return ResponseEntity.badRequest().body(FaceLoginResponse.builder()
                        .username(request.getUsername())
                        .faceMatched(false)
                        .requiresFace(true)
                        .requiresTotp(false)
                        .message("Face verification failed. Please try again.")
                        .build());
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(FaceLoginResponse.builder()
                    .username(request.getUsername())
                    .faceMatched(false)
                    .requiresFace(true)
                    .requiresTotp(false)
                    .message("Face verification failed: " + e.getMessage())
                    .build());
        }
    }

    /**
     * Enable face login for user
     * POST /api/auth/face/enable
     */
    @PostMapping("/enable")
    public ResponseEntity<AuthResponse> enableFace(@RequestHeader("Authorization") String token) {
        String rawToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        if (!jwtUtil.validateToken(rawToken)) {
            return ResponseEntity.status(401).build();
        }
        String username = jwtUtil.extractUsername(rawToken);
        if (username == null || username.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        
        try {
            AuthResponse response = faceService.enableFace(username);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(AuthResponse.builder()
                    .message("Failed to enable face login: " + e.getMessage())
                    .build());
        }
    }

    /**
     * Disable face login for user
     * POST /api/auth/face/disable
     */
    @PostMapping("/disable")
    public ResponseEntity<AuthResponse> disableFace(@RequestHeader("Authorization") String token) {
        String rawToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        if (!jwtUtil.validateToken(rawToken)) {
            return ResponseEntity.status(401).build();
        }
        String username = jwtUtil.extractUsername(rawToken);
        if (username == null || username.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        
        AuthResponse response = faceService.disableFace(username);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete face data for user
     * DELETE /api/auth/face
     */
    @DeleteMapping
    public ResponseEntity<AuthResponse> deleteFace(@RequestHeader("Authorization") String token) {
        String rawToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        if (!jwtUtil.validateToken(rawToken)) {
            return ResponseEntity.status(401).build();
        }
        String username = jwtUtil.extractUsername(rawToken);
        if (username == null || username.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        
        AuthResponse response = faceService.deleteFace(username);
        return ResponseEntity.ok(response);
    }

    /**
     * Check if user has face registered
     * GET /api/auth/face/status
     */
    @GetMapping("/status")
    public ResponseEntity<AuthResponse> getFaceStatus(@RequestHeader("Authorization") String token) {
        String rawToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        if (!jwtUtil.validateToken(rawToken)) {
            return ResponseEntity.status(401).build();
        }
        String username = jwtUtil.extractUsername(rawToken);
        if (username == null || username.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        
        boolean hasFace = faceService.hasFaceRegistered(username);
        boolean isEnabled = faceService.isFaceEnabled(username);
        
        return ResponseEntity.ok(AuthResponse.builder()
                .username(username)
                .message(hasFace ? "Face registered" : "No face registered")
                .requiresFace(hasFace && isEnabled)
                .build());
    }
    
    // Import logger
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(FaceController.class);
}
