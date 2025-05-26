package com.example.software.api;

/**
 * Interface for authentication-related API operations
 */
public interface AuthService {
    
    /**
     * Authenticate a user and get access token
     * 
     * @param username The username
     * @param password The password
     * @return Authentication response containing token and user info
     * @throws ApiException If authentication fails
     */
    AuthResponse login(String username, String password) throws ApiException;
    
    /**
     * Register a new user account
     * 
     * @param username The username
     * @param password The password
     * @param email The email address (optional)
     * @param phone The phone number (optional)
     * @return Registration response
     * @throws ApiException If registration fails
     */
    RegistrationResponse register(String username, String password, String email, String phone) throws ApiException;
    
    /**
     * Verify a user's email or phone using a verification code
     * 
     * @param userId The user ID
     * @param code The verification code
     * @param type The verification type (email or phone)
     * @return Verification response
     * @throws ApiException If verification fails
     */
    ApiResponse verifyCode(String userId, String code, String type) throws ApiException;

    /**
     * 刷新访问令牌
     * 
     * @param currentToken 当前的访问令牌
     * @return 包含新令牌的认证响应
     * @throws ApiException 如果刷新失败
     */
    AuthResponse refreshToken(String currentToken) throws ApiException;
}
