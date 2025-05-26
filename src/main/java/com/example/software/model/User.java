package com.example.software.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private String userId;  // 用户ID
    private String username;
    private String password;
    private String email;
    private String phone;     // 电话
    private UserSettings userSettings;
} 