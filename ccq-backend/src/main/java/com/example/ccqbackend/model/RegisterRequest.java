package com.example.ccqbackend.model;

public class RegisterRequest {
    private String username;  // 用户名
    private String password;  // 密码
    private String email;     // 邮箱
    private String phone;     // 电话

    // 构造函数
    public RegisterRequest() {}

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
