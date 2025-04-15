package com.example.ccqbackend.model;

import java.time.LocalDateTime;

public class BillingEntry {

    private String category;
    private String product;
    private double price;
    private String remark;

    // Getters and Setters
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {

    }

    public void setEntryDateTime(LocalDateTime now) {
    }
}
