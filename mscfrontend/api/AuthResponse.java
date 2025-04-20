package com.example.loginapp.api;

/**
 * Response for authentication requests
 */
public class AuthResponse extends ApiResponse {
    private String userId;
    private String username;
    private String token;
    private long expiresIn;
    
    public AuthResponse() {
        super();
    }
    
    public AuthResponse(boolean success, String message) {
        super(success, message);
    }
    
    public AuthResponse(String userId, String username, String token, long expiresIn) {
        super(true, "Login successful");
        this.userId = userId;
        this.username = username;
        this.token = token;
        this.expiresIn = expiresIn;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public long getExpiresIn() {
        return expiresIn;
    }
    
    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
