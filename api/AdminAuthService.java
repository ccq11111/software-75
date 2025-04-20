package com.example.loginapp.api;

import java.util.UUID;

/**
 * Special implementation of the authentication service for the admin account
 */
public class AdminAuthService implements AuthService {
    
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin";
    private static final String ADMIN_USER_ID = "admin-user-123";
    
    @Override
    public AuthResponse login(String username, String password) throws ApiException {
        // Check if the username is admin
        if (!ADMIN_USERNAME.equals(username)) {
            throw new ApiException(
                new ApiError("INVALID_CREDENTIALS", "Invalid username or password"),
                401
            );
        }
        
        // Check if the password is correct
        if (!ADMIN_PASSWORD.equals(password)) {
            throw new ApiException(
                new ApiError("INVALID_CREDENTIALS", "Invalid username or password"),
                401
            );
        }
        
        // Generate a mock token
        String token = "admin-token-" + UUID.randomUUID().toString();
        
        // Return the authentication response
        return new AuthResponse(ADMIN_USER_ID, username, token, 86400); // Token expires in 24 hours
    }
    
    @Override
    public RegistrationResponse register(String username, String password, String email, String phone) throws ApiException {
        // Admin account cannot be registered
        throw new ApiException(
            new ApiError("OPERATION_NOT_ALLOWED", "Admin account cannot be registered"),
            403
        );
    }
    
    @Override
    public ApiResponse verifyCode(String userId, String code, String type) throws ApiException {
        // Admin account doesn't need verification
        return new ApiResponse(true, "Verification successful");
    }
}
