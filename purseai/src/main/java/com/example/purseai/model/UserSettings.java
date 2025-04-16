package com.example.purseai.model;

public class UserSettings {

    private String preferredCurrency = "CNY";
    private boolean emailNotifications = true;
    private boolean pushNotifications = false;
    
    // 构造函数
    public UserSettings() {
    }
    
    // Getters and Setters
    public String getPreferredCurrency() {
        return preferredCurrency;
    }
    
    public void setPreferredCurrency(String preferredCurrency) {
        this.preferredCurrency = preferredCurrency;
    }
    
    public boolean isEmailNotifications() {
        return emailNotifications;
    }
    
    public void setEmailNotifications(boolean emailNotifications) {
        this.emailNotifications = emailNotifications;
    }
    
    public boolean isPushNotifications() {
        return pushNotifications;
    }
    
    public void setPushNotifications(boolean pushNotifications) {
        this.pushNotifications = pushNotifications;
    }
}