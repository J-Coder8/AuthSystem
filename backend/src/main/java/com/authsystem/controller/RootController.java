package com.authsystem.controller;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Serves the web frontend at the root path and exposes a lightweight
 * health-check endpoint at /api/health.
 */
@Controller
public class RootController {

    /**
     * Forward the root path to the static index.html so that Spring Boot's
     * ResourceHttpRequestHandler picks it up from src/main/resources/static/.
     */
    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    /**
     * Simple health-check endpoint.
     * Returns HTTP 200 with basic service metadata.
     */
    @GetMapping("/api/health")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status",    "UP");
        body.put("service",   "AuthSystem");
        body.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(body);
    }
}
