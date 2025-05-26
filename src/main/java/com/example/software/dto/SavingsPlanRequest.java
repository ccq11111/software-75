package com.example.software.dto;

import com.example.software.model.SavingsPlan;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public class SavingsPlanRequest {
    
    @NotBlank(message = "Plan name is required")
    private String name;
    
    @NotNull(message = "Start date is required")
    private Instant startDate;
    
    @NotNull(message = "Cycle type is required")
    private SavingsPlan.CycleType cycle;
    
    @NotNull(message = "Cycle times is required")
    @Min(value = 1, message = "Cycle times must be at least 1")
    private Integer cycleTimes;
    
    @NotNull(message = "Amount is required")
    @Min(value = 0, message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotNull(message = "Currency is required")
    private SavingsPlan.CurrencyType currency;
    
    public SavingsPlanRequest() {
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
    
    public SavingsPlan.CurrencyType getCurrency() {
        return currency;
    }
    
    public void setCurrency(SavingsPlan.CurrencyType currency) {
        this.currency = currency;
    }
} 