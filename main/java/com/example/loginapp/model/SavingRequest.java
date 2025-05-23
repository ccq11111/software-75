package com.example.loginapp.model;

import lombok.Data;

@Data
public class SavingRequest {
    private Double amount;
    private String planName;
    private Integer durationDays;
}

