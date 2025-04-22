package com.example.purseai.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSettings {
    private String preferredCurrency;
    private boolean emailNotifications;
    private boolean pushNotifications;
}