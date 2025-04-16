package com.example.purseai.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class User {

    private String userId;
    private String username;
    private String password;
    private String email;
    private UserSettings userSettings = new UserSettings();
    
    // 构造函数
    public User() {
    }
    
    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

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

    public UserSettings getUserSettings() {
        return userSettings;
    }

    public void setUserSettings(UserSettings userSettings) {
        this.userSettings = userSettings;
    }
}