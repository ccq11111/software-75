package com.example.software.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String username;  // 用户名
    private String password;  // 密码
    private String email;     // 邮箱
    private String phone;     // 电话
} 