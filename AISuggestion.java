package com.example.software.api;

/**
 * Model class for an AI suggestion
 */
public class AISuggestion {
    private String type;
    private String text;
    
    public AISuggestion() {
    }
    
    public AISuggestion(String type, String text) {
        this.type = type;
        this.text = text;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
}
