package com.example.software.config;

import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class JwtConfig {
    // 使用固定密钥字符串，确保与AuthController使用相同的密钥
    // 密钥必须足够长，至少256位(32字节)
    private static final String SECRET_KEY_STRING = "mySecretKey1234567890mySecretKey1234567890mySecretKey1234567890";
    
    // 使用固定字符串创建密钥
    private static final Key SECRET_KEY = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes());
    
    // 直接使用秘钥生成方法（备选方案）
    // private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // 返回 Key 类型的密钥
    public Key getJwtSecretKey() {
        return SECRET_KEY; // 返回 Key 类型的密钥
    }
}
