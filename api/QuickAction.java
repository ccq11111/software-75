package com.example.software.api;

/**
 * Model class for a quick action
 */
public class QuickAction {
    private String id;
    private String text;
    
    public QuickAction() {
    }
    
    public QuickAction(String id, String text) {
        this.id = id;
        this.text = text;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
}
