package com.example.software.dto;

import com.example.software.model.SavingsPlan;
import lombok.Data;

@Data
public class UserSettingsRequest {
    private SavingsPlan.CurrencyType currency;
    private NotificationSettings notifications;

    @Data
    public static class NotificationSettings {
        private Boolean email;
        private Boolean push;
    }
}