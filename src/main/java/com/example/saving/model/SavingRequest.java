package com.example.saving.model;

import lombok.Data;

@Data
public class SavingRequest {
    private Double amount;
    private String planName;
    private Integer durationDays;
}

