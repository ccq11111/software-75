package com.example.software.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class JwtSecretKeyService {

    private String jwtSecretKey;

    public String getJwtSecretKey() {
        return jwtSecretKey;
    }

    @PostConstruct
    private void init() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] secretKeyBytes = new byte[32]; // 32字节密钥
        secureRandom.nextBytes(secretKeyBytes);
        jwtSecretKey = Base64.getEncoder().encodeToString(secretKeyBytes);
    }
}
