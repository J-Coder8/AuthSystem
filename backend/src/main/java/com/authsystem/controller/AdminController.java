package com.authsystem.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.authsystem.dto.AdminUserDto;
import com.authsystem.security.JwtUtil;
import com.authsystem.service.AdminService;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final AdminService adminService;
    private final JwtUtil jwtUtil;

    public AdminController(AdminService adminService, JwtUtil jwtUtil) {
        this.adminService = adminService;
        this.jwtUtil = jwtUtil;
    }

    private boolean isAdmin(String token) {
        if (token == null) return false;
        String rawToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        if (!jwtUtil.validateToken(rawToken)) return false;
        String role = jwtUtil.extractClaim(rawToken, "role");
        return "ADMIN".equals(role);
    }

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserDto>> getAllUsers(@RequestHeader("Authorization") String token) {
        if (!isAdmin(token)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<AdminUserDto> getUserById(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        if (!isAdmin(token)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    @PostMapping("/users/{id}/enable")
    public ResponseEntity<AdminUserDto> enableUser(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        if (!isAdmin(token)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(adminService.enableUser(id));
    }

    @PostMapping("/users/{id}/disable")
    public ResponseEntity<AdminUserDto> disableUser(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        if (!isAdmin(token)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(adminService.disableUser(id));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id) {
        if (!isAdmin(token)) return ResponseEntity.status(403).build();
        adminService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<AdminUserDto> updateUserRole(
            @RequestHeader("Authorization") String token,
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        if (!isAdmin(token)) return ResponseEntity.status(403).build();
        String role = request.get("role");
        return ResponseEntity.ok(adminService.updateUserRole(id, role));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(@RequestHeader("Authorization") String token) {
        if (!isAdmin(token)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(Map.of(
                "totalUsers", adminService.getTotalUsers(),
                "activeUsers", adminService.getActiveUsers()
        ));
    }
}

