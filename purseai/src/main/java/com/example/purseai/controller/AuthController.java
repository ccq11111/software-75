package com.example.purseai.controller;

import com.example.purseai.dto.AuthResponse;
import com.example.purseai.dto.LoginRequest;
import com.example.purseai.dto.RegisterRequest;
import com.example.purseai.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/v1/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        log.info("Received register request for username: {}", request.getUsername());
        try {
            AuthResponse response = authService.register(request);
            log.info("Successfully registered user: {}", request.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during registration: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        log.info("Received login request for username: {}", request.getUsername());
        try {
            AuthResponse response = authService.login(request);
            log.info("Successfully logged in user: {}", request.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during login: {}", e.getMessage(), e);
            throw e;
        }
    }
} 