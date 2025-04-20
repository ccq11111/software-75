package com.example.loginapp.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Model class for a billing entry
 */
public class BillingEntry {
    private String entryId;
    private String category;
    private String product;
    private BigDecimal price;
    private LocalDate date;
    private LocalTime time;
    private String formattedTime;
    private String remark;
    
    public BillingEntry() {
    }
    
    public BillingEntry(String entryId, String category, String product, BigDecimal price,
                       LocalDate date, LocalTime time, String remark) {
        this.entryId = entryId;
        this.category = category;
        this.product = product;
        this.price = price;
        this.date = date;
        this.time = time;
        this.formattedTime = time.format(DateTimeFormatter.ofPattern("HH:mm"));
        this.remark = remark;
    }
    
    public String getEntryId() {
        return entryId;
    }
    
    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getProduct() {
        return product;
    }
    
    public void setProduct(String product) {
        this.product = product;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public LocalTime getTime() {
        return time;
    }
    
    public void setTime(LocalTime time) {
        this.time = time;
        this.formattedTime = time.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
    
    public String getFormattedTime() {
        return formattedTime;
    }
    
    public void setFormattedTime(String formattedTime) {
        this.formattedTime = formattedTime;
    }
    
    public String getRemark() {
        return remark;
    }
    
    public void setRemark(String remark) {
        this.remark = remark;
    }
}
