package com.example.loginapp.api;

import java.math.BigDecimal;

/**
 * Model class for a category summary
 */
public class CategorySummary {
    private String category;
    private BigDecimal amount;
    private double percentage;
    
    public CategorySummary() {
    }
    
    public CategorySummary(String category, BigDecimal amount, double percentage) {
        this.category = category;
        this.amount = amount;
        this.percentage = percentage;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public double getPercentage() {
        return percentage;
    }
    
    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
}
