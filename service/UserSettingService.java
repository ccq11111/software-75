package com.example.software.service;

import com.example.software.dto.UserSettingsRequest;
import com.example.software.model.User;
import com.example.software.model.UserSettings;
import com.example.software.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

@Service
public class UserSettingService {
    private final UserRepository userRepository;

    public UserSettingService(UserRepository userRepository) {
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
            userSettings.setCurrency(request.getCurrency());
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
