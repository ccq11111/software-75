package com.example.loginapp.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.ZonedDateTime;

/**
 * Class representing API errors
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiError {
    private String code;
    private String message;
    private Object details;
    private ZonedDateTime timestamp;
    
    public ApiError() {
    }
    
    public ApiError(String code, String message) {
        this.code = code;
        this.message = message;
        this.timestamp = ZonedDateTime.now();
    }
    
    public ApiError(String code, String message, Object details) {
        this.code = code;
        this.message = message;
        this.details = details;
        this.timestamp = ZonedDateTime.now();
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Object getDetails() {
        return details;
    }
    
    public void setDetails(Object details) {
        this.details = details;
    }
    
    public ZonedDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
