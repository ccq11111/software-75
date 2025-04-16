package com.example.loginapp.api;

/**
 * Model class for user settings
 */
public class UserSettings extends ApiResponse {
    private String username;
    private String email;
    private String phone;
    private String currency;
    private String language;
    private Notifications notifications;
    
    public UserSettings() {
        super();
    }
    
    public UserSettings(boolean success, String message) {
        super(success, message);
    }
    
    public UserSettings(String username, String email, String phone, 
                       String currency, String language, Notifications notifications) {
        super(true, null);
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.currency = currency;
        this.language = language;
        this.notifications = notifications;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
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
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public Notifications getNotifications() {
        return notifications;
    }
    
    public void setNotifications(Notifications notifications) {
        this.notifications = notifications;
    }
    
    /**
     * Inner class for notification settings
     */
    public static class Notifications {
        private boolean email;
        private boolean push;
        
        public Notifications() {
        }
        
        public Notifications(boolean email, boolean push) {
            this.email = email;
            this.push = push;
        }
        
        public boolean isEmail() {
            return email;
        }
        
        public void setEmail(boolean email) {
            this.email = email;
        }
        
        public boolean isPush() {
            return push;
        }
        
        public void setPush(boolean push) {
            this.push = push;
        }
    }
}
