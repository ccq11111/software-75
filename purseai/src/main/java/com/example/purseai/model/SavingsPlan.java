package com.example.purseai.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.Instant;

public class SavingsPlan {

    private String planId;
    private String name;
    private Instant startDate;
    private Instant endDate;
    private String cycle;
    private int cycleTimes;
    private BigDecimal amount;
    private BigDecimal totalAmount;
    private BigDecimal savedAmount = BigDecimal.ZERO;
    private String currency;
    private String userId;
    
    // 枚举类型
    public enum CycleType {
        Daily, Weekly, Monthly, Quarterly, Yearly
    }
    
    public enum CurrencyType {
        CNY, USD, EUR
    }
    
    // 构造函数、getter和setter
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

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
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

    public BigDecimal getSavedAmount() {
        return savedAmount;
    }

    public void setSavedAmount(BigDecimal savedAmount) {
        this.savedAmount = savedAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}