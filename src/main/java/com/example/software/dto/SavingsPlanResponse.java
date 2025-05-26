package com.example.software.dto;

import com.example.software.model.SavingsPlan;
import java.math.BigDecimal;
import java.time.Instant;

public class SavingsPlanResponse {
    private boolean success;
    private String planId;
    private String name;
    private Instant startDate;
    private Instant endDate;
    private SavingsPlan.CycleType cycle;
    private Integer cycleTimes;
    private BigDecimal amount;
    private BigDecimal totalAmount;
    private SavingsPlan.CurrencyType currency;
    private BigDecimal savedAmount;

    public SavingsPlanResponse() {
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
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

    public SavingsPlan.CycleType getCycle() {
        return cycle;
    }

    public void setCycle(SavingsPlan.CycleType cycle) {
        this.cycle = cycle;
    }

    public Integer getCycleTimes() {
        return cycleTimes;
    }

    public void setCycleTimes(Integer cycleTimes) {
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

    public SavingsPlan.CurrencyType getCurrency() {
        return currency;
    }

    public void setCurrency(SavingsPlan.CurrencyType currency) {
        this.currency = currency;
    }

    public BigDecimal getSavedAmount() {
        return savedAmount;
    }

    public void setSavedAmount(BigDecimal savedAmount) {
        this.savedAmount = savedAmount;
    }
} 