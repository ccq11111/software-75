package com.example.ccqbackend.controller;

import com.example.ccqbackend.model.LoginRequest;
import com.example.ccqbackend.model.RegisterRequest;
import com.example.ccqbackend.model.User;
import com.example.ccqbackend.service.UserService;
import com.example.ccqbackend.service.JwtConfig;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Key;

@RestController
@RequestMapping("/api.purseai.com/v1/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtConfig jwtConfig;  // 注入 JwtConfig

    // 使用构造方法注入密钥
    private final Key secretKey;

    @Autowired
    public AuthController(JwtConfig jwtConfig) {
        this.secretKey = jwtConfig.getJwtSecretKey();  // 在构造函数中初始化
    }

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {
        try {
            User newUser = userService.register(request);
            String token = generateJwtToken(newUser); // 使用密钥生成 JWT Token
            return "{\"success\": true, \"userId\": \"" + newUser.getId() + "\", \"token\": \"" + token + "\", \"message\": \"注册成功\"}";
        } catch (RuntimeException e) {
            e.printStackTrace();
            return "{\"success\": false, \"message\": \"错误: " + e.getMessage() + "\"}";
        } catch (IOException e) {
            e.printStackTrace();
            return "{\"success\": false, \"message\": \"IO 错误: " + e.getMessage() + "\"}";
        }
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        try {
            User user = userService.login(request);
            String token = generateJwtToken(user); // 使用密钥生成 JWT Token
            return "{\"success\": true, \"userId\": \"" + user.getId() + "\", \"username\": \"" + user.getUsername() + "\", \"token\": \"" + token + "\", \"expiresIn\": 86400}";
        } catch (RuntimeException | IOException e) {
            return "{\"success\": false, \"message\": \"" + e.getMessage() + "\"}";
        }
    }

    // 使用 HS256 算法和密钥生成 JWT Token
    private String generateJwtToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userId", user.getId())
                .signWith(secretKey, SignatureAlgorithm.HS256)  // 使用构造函数中的密钥
                .compact();
    }
}
