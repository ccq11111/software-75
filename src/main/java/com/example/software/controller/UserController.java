package com.example.software.controller;

import com.example.software.dto.UserSettingsRequest;
import com.example.software.model.UserSettings;
import com.example.software.service.UserSettingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/purseai/v1/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    private final UserSettingService userSettingService;
    
    public UserController(UserSettingService userSettingService) {
        this.userSettingService = userSettingService;
    }

    @PutMapping("/settings")
    public ResponseEntity<Map<String, Object>> updateUserSettings(@RequestBody UserSettingsRequest request) {
        UserDetails userDetails = getCurrentUser();
        UserSettings updatedSettings = userSettingService.updateUserSettings(userDetails.getUsername(), request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("settings", Map.of(
                "currency", updatedSettings.getCurrency(),
                "notifications", Map.of(
                        "email", updatedSettings.isEmailNotifications(),
                        "push", updatedSettings.isPushNotifications()
                )
        ));
        
        return ResponseEntity.ok(response);
    }

    private UserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (UserDetails) authentication.getPrincipal();
    }
} 