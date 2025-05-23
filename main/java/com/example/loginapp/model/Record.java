package com.example.loginapp.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class Record {
    private String userId;
    private String category;
    private String product;
    private double amount;
    private LocalDate date;
    private String time;
    private String remark;

    public Record(String userId, String category, String product, double amount, LocalDate date, String time, String remark) {
        this.userId = userId;
        this.category = category;
        this.product = product;
        this.amount = amount;
        this.date = date;
        this.time = time;
        this.remark = remark;
    }
}
