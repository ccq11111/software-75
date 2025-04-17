package com.example.saving.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CategorySummary {
    private String category;
    private double amount;
    private double percentage;

    public CategorySummary(String category, double amount, double percentage) {
        this.category = category;
        this.amount = amount;
        this.percentage = percentage;
    }

}
