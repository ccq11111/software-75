package com.example.software.api;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Mock implementation of the authentication service for the test account
 * This implementation works completely offline without any network connections
 */
public class MockAuthService implements AuthService {

    private static final String TEST_USERNAME = "test";
    private static final String TEST_PASSWORD = "test";
    private static final String TEST_USER_ID = "test-user-123";

    private static final Map<String, String> registeredUsers = new HashMap<>();

    static {
        // Add the test user
        registeredUsers.put(TEST_USERNAME, TEST_PASSWORD);
    }

    @Override
    public AuthResponse login(String username, String password) throws ApiException {
        // Check if the username exists
        if (!registeredUsers.containsKey(username)) {
            throw new ApiException(
                new ApiError("INVALID_CREDENTIALS", "Invalid username or password"),
                401
            );
        }

        // Check if the password is correct
        if (!registeredUsers.get(username).equals(password)) {
            throw new ApiException(
                new ApiError("INVALID_CREDENTIALS", "Invalid username or password"),
                401
            );
        }

        // Generate a mock token
        String token = "mock-token-" + UUID.randomUUID().toString();

        // Return the authentication response
        return new AuthResponse(TEST_USER_ID, username, token, 86400); // Token expires in 24 hours
    }

    @Override
    public RegistrationResponse register(String username, String password, String email, String phone) throws ApiException {
        // Check if the username already exists
        if (registeredUsers.containsKey(username)) {
            throw new ApiException(
                new ApiError("ACCOUNT_EXISTS", "Account already exists"),
                409
            );
        }

        // Register the new user
        registeredUsers.put(username, password);

        // Generate a mock user ID and token
        String userId = "user-" + UUID.randomUUID().toString();
        String token = "mock-token-" + UUID.randomUUID().toString();

        // Return the registration response
        return new RegistrationResponse(userId, token);
    }

    @Override
    public ApiResponse verifyCode(String userId, String code, String type) throws ApiException {
        // For the mock implementation, any code is valid
        return new ApiResponse(true, "Verification successful");
    }

    @Override
    public AuthResponse refreshToken(String currentToken) throws ApiException {
        // 为测试账户生成新的模拟 token
        String newToken = "mock-token-" + UUID.randomUUID().toString();
        return new AuthResponse(TEST_USER_ID, TEST_USERNAME, newToken, 86400); // Token expires in 24 hours
    }
}
