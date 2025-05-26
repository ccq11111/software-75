package com.example.software.api;

/**
 * Mock implementation of the user service for the test account
 * This implementation works completely offline without any network connections
 */
public class MockUserService implements UserService {

    private static UserSettings userSettings;

    static {
        // Initialize with default settings
        UserSettings.Notifications notifications = new UserSettings.Notifications(true, false);
        userSettings = new UserSettings("test", "test@example.com", "123456789", "USD", "en", notifications);
    }

    @Override
    public UserSettings getUserSettings() throws ApiException {
        return userSettings;
    }

    @Override
    public UserSettings updateUserSettings(String email, String phone, String currency,
                                          String language, Boolean emailNotifications,
                                          Boolean pushNotifications) throws ApiException {
        // Update the settings
        if (email != null) {
            userSettings.setEmail(email);
        }
        if (phone != null) {
            userSettings.setPhone(phone);
        }
        if (currency != null) {
            userSettings.setCurrency(currency);
        }
        if (language != null) {
            userSettings.setLanguage(language);
        }

        // Update notifications
        UserSettings.Notifications notifications = userSettings.getNotifications();
        if (emailNotifications != null) {
            notifications.setEmail(emailNotifications);
        }
        if (pushNotifications != null) {
            notifications.setPush(pushNotifications);
        }
        userSettings.setNotifications(notifications);

        return userSettings;
    }
}
