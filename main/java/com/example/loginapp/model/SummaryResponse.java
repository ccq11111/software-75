package com.example.loginapp.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class SummaryResponse {
    private boolean success;
    private String period;
    private double total;
    private List<CategorySummary> categories;
    private String debugInfo;

    public SummaryResponse() {
    }

    public SummaryResponse(boolean success, String period, double total, List<CategorySummary> categories, String debugInfo) {
        this.success = success;
        this.period = period;
        this.total = total;
        this.categories = categories;
        this.debugInfo = debugInfo;
    }
}
