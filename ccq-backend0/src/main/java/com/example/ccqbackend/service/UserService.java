package com.example.ccqbackend.service;

import com.example.ccqbackend.model.LoginRequest;
import com.example.ccqbackend.model.RegisterRequest;
import com.example.ccqbackend.service.JwtSecretKeyService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.ccqbackend.model.User;
import java.io.IOException;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private List<User> users = new ArrayList<>();

    private final JwtSecretKeyService jwtSecretKeyService;

    @Autowired
    public UserService(JwtSecretKeyService jwtSecretKeyService) {
        this.jwtSecretKeyService = jwtSecretKeyService;
    }

    public User register(RegisterRequest request) throws IOException {
        if (users.stream().anyMatch(user -> user.getUsername().equals(request.getUsername()) ||
                user.getEmail().equals(request.getEmail()) || user.getPhone().equals(request.getPhone()))) {
            throw new RuntimeException("用户名、邮箱或电话已存在");
        }

        User newUser = new User();
        newUser.setId(UUID.randomUUID().toString());
        newUser.setUsername(request.getUsername());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setEmail(request.getEmail());
        newUser.setPhone(request.getPhone());

        users.add(newUser);
        return newUser;
    }

    public User login(LoginRequest request) throws IOException {
        User user = users.stream()
                .filter(u -> u.getUsername().equals(request.getUsername()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        return user;
    }

    public String generateJwtToken(User user) {
        String jwtSecretKey = jwtSecretKeyService.getJwtSecretKey();
        Key secretKey = Keys.hmacShaKeyFor(jwtSecretKey.getBytes());

        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userId", user.getId())
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }
}
