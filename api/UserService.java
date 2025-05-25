package com.example.software.api;

/**
 * Interface for user settings-related API operations
 */
public interface UserService {
    
    /**
     * Get user settings
     * 
     * @return User settings
     * @throws ApiException If retrieval fails
     */
    UserSettings getUserSettings() throws ApiException;
    
    /**
     * Update user settings
     * 
     * @param email The email address
     * @param phone The phone number
     * @param currency The currency code
     * @param language The language code
     * @param emailNotifications Whether to enable email notifications
     * @param pushNotifications Whether to enable push notifications
     * @return Updated user settings
     * @throws ApiException If update fails
     */
    UserSettings updateUserSettings(String email, String phone, String currency, 
                                   String language, Boolean emailNotifications, 
                                   Boolean pushNotifications) throws ApiException;
}
