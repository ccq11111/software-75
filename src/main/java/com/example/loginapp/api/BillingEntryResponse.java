package com.example.loginapp.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response for billing entry operations
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BillingEntryResponse extends ApiResponse {
    private String entryId;
    private String category;
    private String product;
    private BigDecimal price;
    private LocalDate date;
    private LocalTime time;
    private String formattedTime;
    private String remark;
    
    @JsonProperty("data")
    private BillingEntry data;
    
    public BillingEntryResponse() {
        super();
    }
    
    public BillingEntryResponse(boolean success, String message) {
        super(success, message);
    }
    
    public BillingEntryResponse(BillingEntry entry) {
        super(true, null);
        this.entryId = entry.getEntryId();
        this.category = entry.getCategory();
        this.product = entry.getProduct();
        this.price = entry.getPrice();
        this.date = entry.getDate();
        this.time = entry.getTime();
        this.formattedTime = entry.getFormattedTime();
        this.remark = entry.getRemark();
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
    
    @JsonProperty("data")
    public void setData(BillingEntry data) {
        this.data = data;
        if (data != null) {
            // 输出debug信息
            System.out.println("从data字段获取到数据: " + data);
            System.out.println("  category: " + data.getCategory());
            System.out.println("  product: " + data.getProduct());
            System.out.println("  price: " + data.getPrice());
            System.out.println("  date: " + data.getDate());
            System.out.println("  time: " + data.getTime());
            System.out.println("  remark: " + data.getRemark());
            
            this.category = data.getCategory();
            this.product = data.getProduct();
            this.price = data.getPrice();
            this.date = data.getDate();
            this.time = data.getTime();
            this.formattedTime = data.getFormattedTime();
            this.remark = data.getRemark();
        }
    }
    
    public BillingEntry getData() {
        return data;
    }
}
