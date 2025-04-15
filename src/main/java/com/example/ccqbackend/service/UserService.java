package com.example.ccqbackend.service;

import com.example.ccqbackend.model.LoginRequest;
import com.example.ccqbackend.model.RegisterRequest;
import com.example.ccqbackend.model.User;
import com.example.ccqbackend.model.RegisterRequest;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 使用 ArrayList 替代不可变集合，确保能够添加用户
    private List<User> users = new ArrayList<>();  // 这里可以存储用户

    // 从 application.properties 读取 JWT 密钥
    @Value("${jwt.secret}")
    private String jwtSecret;  // 读取的密钥

    // 加载用户信息（可以从文件或数据库中读取）
    public List<User> loadUsers() throws IOException {
        // 返回当前内存中的用户列表
        return users;
    }

    // 保存用户数据（可以将数据保存到文件或数据库中）
    private void saveUsers(List<User> users) throws IOException {
        // 在这里可以实现保存数据的逻辑，例如保存到文件或数据库
        this.users = users;
    }

    // 注册新用户
    public User register(RegisterRequest request) throws IOException {
        // 检查用户名、邮箱、电话是否已存在
        if (users.stream().anyMatch(user -> user.getUsername().equals(request.getUsername()) ||
                user.getEmail().equals(request.getEmail()) || user.getPhone().equals(request.getPhone()))) {
            throw new RuntimeException("用户名、邮箱或电话已存在");
        }

        // 创建新用户对象并设置属性
        User newUser = new User();
        newUser.setId(UUID.randomUUID().toString());
        newUser.setUsername(request.getUsername());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));  // 密码加密
        newUser.setEmail(request.getEmail());
        newUser.setPhone(request.getPhone());

        // 将新用户添加到内存中的列表
        users.add(newUser);
        saveUsers(users);  // 保存用户信息到内存中

        return newUser;
    }

    // 用户登录
    public User login(LoginRequest request) throws IOException {
        // 从内存中加载用户列表
        User user = users.stream()
                .filter(u -> u.getUsername().equals(request.getUsername()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        // 验证密码是否匹配
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        return user;
    }

    // 生成 JWT Token
    public String generateJwtToken(User user) {
        // 使用 io.jsonwebtoken.security.Keys 提供的密钥生成方法
        Key secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);  // 自动生成符合要求的密钥

        // 生成 JWT Token
        return Jwts.builder()
                .setSubject(user.getUsername())  // 设置用户名为 subject
                .claim("userId", user.getId())  // 将 userId 作为 claim 存储
                .signWith(secretKey, SignatureAlgorithm.HS256)  // 使用 HS256 算法和密钥进行签名
                .compact();  // 返回生成的 JWT token
    }

}
