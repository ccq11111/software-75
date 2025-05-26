package com.example.software.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Embeddable
public class UserSettings {

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_currency")
    private SavingsPlan.CurrencyType currency = SavingsPlan.CurrencyType.CNY;

    @Column(name = "email_notifications")
    private boolean emailNotifications = true;

    @Column(name = "push_notifications")
    private boolean pushNotifications = false;

    // 构造函数
    public UserSettings() {
    }

    // Getters and Setters
    public SavingsPlan.CurrencyType getCurrency() {
        return currency;
    }

    public void setCurrency(SavingsPlan.CurrencyType currency) {
        this.currency = currency;
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