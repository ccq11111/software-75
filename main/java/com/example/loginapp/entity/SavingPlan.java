package com.example.loginapp.entity;

import java.time.LocalDate;

public class SavingPlan {
    private String planId;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String cycle; // "DAILY"|"WEEKLY"|"MONTHLY"
    private int cycleTimes;
    private double amount;
    private double totalAmount;
    private String currency;
    private double savedAmount;

    // 全参数构造方法
    public SavingPlan(String planId, String name, LocalDate startDate, LocalDate endDate,
                      String cycle, int cycleTimes, double amount, double totalAmount,
                      String currency, double savedAmount) {
        this.planId = planId;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.cycle = cycle;
        this.cycleTimes = cycleTimes;
        this.amount = amount;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.savedAmount = savedAmount;
    }

    // Getters and Setters
    // ...
}