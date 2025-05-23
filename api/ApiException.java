package com.example.software.api;

/**
 * Exception thrown when API calls fail
 */
public class ApiException extends Exception {
    private ApiError error;
    private int statusCode;
    
    public ApiException(String message) {
        super(message);
        this.error = new ApiError("INTERNAL_ERROR", message);
        this.statusCode = 500;
    }
    
    public ApiException(String message, Throwable cause) {
        super(message, cause);
        this.error = new ApiError("INTERNAL_ERROR", message);
        this.statusCode = 500;
    }
    
    public ApiException(ApiError error, int statusCode) {
        super(error.getMessage());
        this.error = error;
        this.statusCode = statusCode;
    }
    
    public ApiError getError() {
        return error;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
}
