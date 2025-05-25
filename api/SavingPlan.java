package com.example.software.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Model class for a saving plan
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SavingPlan {
    private String planId;
    private String name;
    
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Instant startDate;
    
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Instant endDate;
    
    private String cycle;
    private int cycleTimes;
    private BigDecimal amount;
    private BigDecimal totalAmount;
    private String currency;
    private BigDecimal savedAmount;
    
    // Jackson需要这些字段来处理JSON中的日期数组
    private List<Integer> startDateAsLocalDate;
    private List<Integer> endDateAsLocalDate;
    
    public SavingPlan() {
    }
    
    public SavingPlan(String planId, String name, LocalDate startDate, LocalDate endDate,
                     String cycle, int cycleTimes, BigDecimal amount, BigDecimal totalAmount,
                     String currency, BigDecimal savedAmount) {
        this.planId = planId;
        this.name = name;
        this.startDate = startDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        this.endDate = endDate.atStartOfDay().toInstant(ZoneOffset.UTC);
        this.cycle = cycle;
        this.cycleTimes = cycleTimes;
        this.amount = amount;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.savedAmount = savedAmount;
    }
    
    // Getters and setters for date array fields
    public List<Integer> getStartDateAsLocalDate() {
        return startDateAsLocalDate;
    }
    
    public void setStartDateAsLocalDate(List<Integer> startDateAsLocalDate) {
        this.startDateAsLocalDate = startDateAsLocalDate;
        // 如果startDate为null但是有数组，就从数组创建startDate
        if (startDate == null && startDateAsLocalDate != null && startDateAsLocalDate.size() >= 3) {
            LocalDate date = LocalDate.of(
                startDateAsLocalDate.get(0), 
                startDateAsLocalDate.get(1), 
                startDateAsLocalDate.get(2)
            );
            this.startDate = date.atStartOfDay().toInstant(ZoneOffset.UTC);
        }
    }
    
    public List<Integer> getEndDateAsLocalDate() {
        return endDateAsLocalDate;
    }
    
    public void setEndDateAsLocalDate(List<Integer> endDateAsLocalDate) {
        this.endDateAsLocalDate = endDateAsLocalDate;
        // 如果endDate为null但是有数组，就从数组创建endDate
        if (endDate == null && endDateAsLocalDate != null && endDateAsLocalDate.size() >= 3) {
            LocalDate date = LocalDate.of(
                endDateAsLocalDate.get(0), 
                endDateAsLocalDate.get(1), 
                endDateAsLocalDate.get(2)
            );
            this.endDate = date.atStartOfDay().toInstant(ZoneOffset.UTC);
        }
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
    
    // 转换方法: 把Instant转为LocalDate
    public LocalDate getStartDateConverted() {
        if (startDate != null) {
            return startDate.atZone(ZoneOffset.UTC).toLocalDate();
        } else if (startDateAsLocalDate != null && startDateAsLocalDate.size() >= 3) {
            return LocalDate.of(
                startDateAsLocalDate.get(0), 
                startDateAsLocalDate.get(1), 
                startDateAsLocalDate.get(2)
            );
        }
        return LocalDate.now(); // 默认返回今天
    }
    
    public Instant getEndDate() {
        return endDate;
    }
    
    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }
    
    // 转换方法: 把Instant转为LocalDate
    public LocalDate getEndDateConverted() {
        if (endDate != null) {
            return endDate.atZone(ZoneOffset.UTC).toLocalDate();
        } else if (endDateAsLocalDate != null && endDateAsLocalDate.size() >= 3) {
            return LocalDate.of(
                endDateAsLocalDate.get(0), 
                endDateAsLocalDate.get(1), 
                endDateAsLocalDate.get(2)
            );
        }
        return LocalDate.now(); // 默认返回今天
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
