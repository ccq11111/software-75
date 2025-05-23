package com.example.loginapp.api;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response for saving plan operations
 */
public class SavingPlanResponse extends ApiResponse {
    private String planId;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String cycle;
    private int cycleTimes;
    private BigDecimal amount;
    private BigDecimal totalAmount;
    private String currency;
    private BigDecimal savedAmount;
    
    public SavingPlanResponse() {
        super();
    }
    
    public SavingPlanResponse(boolean success, String message) {
        super(success, message);
    }
    
    public SavingPlanResponse(SavingPlan plan) {
        super(true, null);
        this.planId = plan.getPlanId();
        this.name = plan.getName();
        this.startDate = plan.getStartDate();
        this.endDate = plan.getEndDate();
        this.cycle = plan.getCycle();
        this.cycleTimes = plan.getCycleTimes();
        this.amount = plan.getAmount();
        this.totalAmount = plan.getTotalAmount();
        this.currency = plan.getCurrency();
        this.savedAmount = plan.getSavedAmount();
    }
    
    public String getPlanId() {
        return planId;
    }
    
    public void setPlanId(String planId) {
        this.planId = planId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public String getCycle() {
        return cycle;
    }
    
    public void setCycle(String cycle) {
        this.cycle = cycle;
    }
    
    public int getCycleTimes() {
        return cycleTimes;
    }
    
    public void setCycleTimes(int cycleTimes) {
        this.cycleTimes = cycleTimes;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public BigDecimal getSavedAmount() {
        return savedAmount;
    }
    
    public void setSavedAmount(BigDecimal savedAmount) {
        this.savedAmount = savedAmount;
    }
}
