package com.example.loginapp.api;

import java.math.BigDecimal;
import java.util.List;

/**
 * Model class for expenditure summary
 */
public class ExpenditureSummary extends ApiResponse {
    private String period;
    private BigDecimal total;
    private List<CategorySummary> categories;
    
    public ExpenditureSummary() {
        super();
    }
    
    public ExpenditureSummary(boolean success, String message) {
        super(success, message);
    }
    
    public ExpenditureSummary(String period, BigDecimal total, List<CategorySummary> categories) {
        super(true, null);
        this.period = period;
        this.total = total;
        this.categories = categories;
    }
    
    public String getPeriod() {
        return period;
    }
    
    public void setPeriod(String period) {
        this.period = period;
    }
    
    public BigDecimal getTotal() {
        return total;
    }
    
    public void setTotal(BigDecimal total) {
        this.total = total;
    }
    
    public List<CategorySummary> getCategories() {
        return categories;
    }
    
    public void setCategories(List<CategorySummary> categories) {
        this.categories = categories;
    }
}
