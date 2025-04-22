package com.example.purseai.dto;

import lombok.Data;

@Data
public class UserSettingsRequest {
    private String currency;
    private NotificationSettings notifications;

    @Data
    public static class NotificationSettings {
        private Boolean email;
        private Boolean push;
    }
}