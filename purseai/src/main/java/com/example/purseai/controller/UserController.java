package com.example.purseai.controller;

import com.example.purseai.dto.UserSettingsRequest;
import com.example.purseai.model.UserSettings;
import com.example.purseai.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/users")
public class UserController {

    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/settings")
    public ResponseEntity<Map<String, Object>> updateUserSettings(@RequestBody UserSettingsRequest request) {
        UserDetails userDetails = getCurrentUser();
        String userId = getUserIdFromToken(userDetails.getUsername());
        
        UserSettings updatedSettings = userService.updateUserSettings(userId, request);
        
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

    private String getUserIdFromToken(String username) {
        // In a real implementation, you would extract the userId from the token
        // or look up the user by username. This is a simplified version.
        return username;
    }
} 