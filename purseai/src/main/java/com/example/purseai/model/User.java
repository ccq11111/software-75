package com.example.purseai.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String userId;
    private String username;
    private String password;
    private String email;
    private UserSettings userSettings;
} 