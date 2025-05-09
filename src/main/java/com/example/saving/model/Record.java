package com.example.saving.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class Record {
    private String userId;
    private final String category;
    private final String product;
    private final double amount;
    private final LocalDate date;
    private final String time;
    private final String remark;

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
