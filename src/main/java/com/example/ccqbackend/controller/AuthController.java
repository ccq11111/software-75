package com.example.ccqbackend.controller;

import com.example.ccqbackend.model.LoginRequest;
import com.example.ccqbackend.model.RegisterRequest;
import com.example.ccqbackend.model.User;
import com.example.ccqbackend.service.UserService;
import com.example.ccqbackend.model.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api.purseai.com/v1/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {
        try {
            User newUser = userService.register(request);
            String token = userService.generateJwtToken(newUser); // 生成 JWT Token
            return "{\"success\": true, \"userId\": \"" + newUser.getId() + "\", \"token\": \"" + token + "\", \"message\": \"注册成功\"}";
        } catch (RuntimeException e) {
            // 打印堆栈信息，帮助调试
            e.printStackTrace();
            return "{\"success\": false, \"message\": \"错误: " + e.getMessage() + "\"}";
        } catch (IOException e) {
            // 打印堆栈信息，帮助调试
            e.printStackTrace();
            return "{\"success\": false, \"message\": \"IO 错误: " + e.getMessage() + "\"}";
        }
    }



    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        try {
            User user = userService.login(request);
            String token = userService.generateJwtToken(user);
            return "{\"success\": true, \"userId\": \"" + user.getId() + "\", \"username\": \"" + user.getUsername() + "\", \"token\": \"" + token + "\", \"expiresIn\": 86400}";
        } catch (RuntimeException | IOException e) {
            return "{\"success\": false, \"message\": \"" + e.getMessage() + "\"}";
        }
    }
}
