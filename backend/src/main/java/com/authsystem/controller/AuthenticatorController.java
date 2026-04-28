package com.authsystem.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.authsystem.dto.AuthenticatorAccountDto;
import com.authsystem.dto.AuthenticatorAccountRequest;
import com.authsystem.security.JwtUtil;
import com.authsystem.service.AuthenticatorService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth/authenticator")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthenticatorController {

    private final AuthenticatorService authenticatorService;
    private final JwtUtil jwtUtil;

    @GetMapping("/accounts")
    public ResponseEntity<List<AuthenticatorAccountDto>> listAccounts(
            @RequestHeader("Authorization") String token) {
        String username = extractUsername(token);
        if (username == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(authenticatorService.listAccounts(username));
    }

    @GetMapping("/codes")
    public ResponseEntity<List<AuthenticatorAccountDto>> listCodes(
            @RequestHeader("Authorization") String token) {
        String username = extractUsername(token);
        if (username == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(authenticatorService.listCodes(username));
    }

    @PostMapping("/accounts")
    public ResponseEntity<AuthenticatorAccountDto> addAccount(
            @RequestHeader("Authorization") String token,
            @RequestBody AuthenticatorAccountRequest request) {
        String username = extractUsername(token);
        if (username == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(authenticatorService.addAccount(username, request));
    }

    @DeleteMapping("/accounts/{id}")
    public ResponseEntity<Void> deleteAccount(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        String username = extractUsername(token);
        if (username == null) {
            return ResponseEntity.status(401).build();
        }
        authenticatorService.deleteAccount(username, id);
        return ResponseEntity.ok().build();
    }

    private String extractUsername(String token) {
        String rawToken = token != null && token.startsWith("Bearer ") ? token.substring(7) : token;
        if (rawToken == null || !jwtUtil.validateToken(rawToken)) {
            return null;
        }
        return jwtUtil.extractUsername(rawToken);
    }
}
