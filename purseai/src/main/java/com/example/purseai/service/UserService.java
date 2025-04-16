package com.example.purseai.service;

import com.example.purseai.dto.UserSettingsRequest;
import com.example.purseai.model.User;
import com.example.purseai.model.UserSettings;
import com.example.purseai.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Transactional
    public UserSettings updateUserSettings(String userId, UserSettingsRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        UserSettings userSettings = user.getUserSettings();
        
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