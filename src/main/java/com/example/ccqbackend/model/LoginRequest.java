package com.example.ccqbackend.model;

public class LoginRequest {
    private String username;  // 用户名gradlew.bat bootRun

    private String password;  // 密码

    // 构造函数
    public LoginRequest() {}

    // Getter 和 Setter 方法
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
