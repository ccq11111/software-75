package com.example.software.controller;

import com.example.software.config.JwtConfig;
import com.example.software.dto.AuthResponse;
import com.example.software.dto.LoginRequest;
import com.example.software.dto.RegisterRequest;
import com.example.software.model.User;
import com.example.software.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.io.IOException;
import java.security.Key;

@Slf4j
@RestController
@RequestMapping("/v1/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    @Autowired
    private AuthService authService;

//    @Autowired
//    private UserService userService;

    @Autowired
    private JwtConfig jwtConfig;  // 注入 JwtConfig

    // 使用构造方法注入密钥
    private final Key secretKey;

    @Autowired
    public AuthController(JwtConfig jwtConfig) {
        this.secretKey = jwtConfig.getJwtSecretKey();  // 在构造函数中初始化
    }


//    @Autowired
//    public AuthController(AuthService authService) {
//        this.authService = authService;
//    }

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