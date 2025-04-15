package com.example.ccqbackend.service;

import io.jsonwebtoken.security.Keys;
import java.security.Key;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

@Component
public class JwtConfig {
    // 使用HS256算法生成密钥
    public static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256); // 生成密钥

    // 返回 Key 类型的密钥
    public Key getJwtSecretKey() {
        return SECRET_KEY; // 返回 Key 类型的密钥
    }
}
