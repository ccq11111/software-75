package com.example.loginapp.api;

import java.util.List;

/**
 * Response for AI advice requests
 */
public class AIAdviceResponse extends ApiResponse {
    private String message;
    private List<AISuggestion> suggestions;
    
    public AIAdviceResponse() {
        super();
    }
    
    public AIAdviceResponse(boolean success, String errorMessage) {
        super(success, errorMessage);
    }
    
    public AIAdviceResponse(String message, List<AISuggestion> suggestions) {
        super(true, null);
        this.message = message;
        this.suggestions = suggestions;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public List<AISuggestion> getSuggestions() {
        return suggestions;
    }
    
    public void setSuggestions(List<AISuggestion> suggestions) {
        this.suggestions = suggestions;
    }
}
