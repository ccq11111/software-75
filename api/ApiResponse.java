package com.example.software.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Base class for API responses
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse {
    private boolean success;
    private String message;
    private ApiError error;
    
    public ApiResponse() {
        this.success = true;
    }
    
    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public ApiResponse(ApiError error) {
        this.success = false;
        this.error = error;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public ApiError getError() {
        return error;
    }
    
    public void setError(ApiError error) {
        this.error = error;
    }
}
