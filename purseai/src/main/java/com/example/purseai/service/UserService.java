package com.example.purseai.service;

import com.example.purseai.dto.UserSettingsRequest;
import com.example.purseai.model.User;
import com.example.purseai.model.UserSettings;
import com.example.purseai.repository.JsonFileUserRepository;
import java.util.NoSuchElementException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class UserService {

    private final JsonFileUserRepository userRepository;
    
    public UserService(JsonFileUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserById(String userId) {
        return userRepository.findByUsername(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public UserSettings updateUserSettings(String username, UserSettingsRequest request) {
        User user = getUserByUsername(username);

        UserSettings userSettings = user.getUserSettings();
        if (userSettings == null) {
            userSettings = new UserSettings();
        }
        
        if (request.getCurrency() != null) {
            userSettings.setPreferredCurrency(request.getCurrency().toString());
        }
        
        if (request.getNotifications() != null) {
            if (request.getNotifications().getEmail() != null) {
                userSettings.setEmailNotifications(request.getNotifications().getEmail());
            }
            if (request.getNotifications().getPush() != null) {
                userSettings.setPushNotifications(request.getNotifications().getPush());
            }
        }
        
        user.setUserSettings(userSettings);
        userRepository.save(user);
        
        return userSettings;
    }
} 