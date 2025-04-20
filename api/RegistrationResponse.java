package com.example.loginapp.api;

/**
 * Response for registration requests
 */
public class RegistrationResponse extends ApiResponse {
    private String userId;
    private String token;
    
    public RegistrationResponse() {
        super();
    }
    
    public RegistrationResponse(boolean success, String message) {
        super(success, message);
    }
    
    public RegistrationResponse(String userId, String token) {
        super(true, "Registration successful");
        this.userId = userId;
        this.token = token;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
}
