package com.example.purseai.dto;

import com.example.purseai.model.SavingsPlan;

public class UserSettingsRequest {
    private SavingsPlan.CurrencyType currency;
    private NotificationSettings notifications;
    
    public UserSettingsRequest() {
    }
    
    public SavingsPlan.CurrencyType getCurrency() {
        return currency;
    }
    
    public void setCurrency(SavingsPlan.CurrencyType currency) {
        this.currency = currency;
    }
    
    public NotificationSettings getNotifications() {
        return notifications;
    }
    
    public void setNotifications(NotificationSettings notifications) {
        this.notifications = notifications;
    }
    
    public static class NotificationSettings {
        private Boolean email;
        private Boolean push;
        
        public NotificationSettings() {
        }
        
        public Boolean getEmail() {
            return email;
        }
        
        public void setEmail(Boolean email) {
            this.email = email;
        }
        
        public Boolean getPush() {
            return push;
        }
        
        public void setPush(Boolean push) {
            this.push = push;
        }
    }
} 