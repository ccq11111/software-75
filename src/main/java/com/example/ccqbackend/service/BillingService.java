package com.example.ccqbackend.service;

import com.example.ccqbackend.service.JwtConfig;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Key;


public class BillingService {


    private JwtConfig jwtSecretKeyService; // 注入 JwtSecretKeyService

    @PostMapping("/entries")
    public String createBillingEntry(@RequestHeader("Authorization") String token) throws IOException {
        // 验证 token
        if (!isValidToken(token)) {
            throw new RuntimeException("无效的 token");
        }

        // 使用不同的格式保存数据...
        return "Billing entry created";
    }

    // 验证 token 是否有效
    private boolean isValidToken(String token) {
        try {
            String actualToken = token.replace("Bearer ", "");  // 去除 Bearer 前缀
            Key secretKey = jwtSecretKeyService.getJwtSecretKey();  // 获取密钥
            // 使用密钥解析 token
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(actualToken);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;  // 如果验证失败，返回 false
        }
    }
}
