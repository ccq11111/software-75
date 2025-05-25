package com.example.loginapp;

import java.time.LocalDate;

/**
 * Model class representing a Saving Plan
 */
public class SavingPlan {
    private String name;
    private LocalDate startDate;
    private String cycle;
    private int cycleTimes;
    private double amount;
    private String currency;

    /**
     * Constructor for SavingPlan
     */
    public SavingPlan(String name, LocalDate startDate, String cycle, int cycleTimes, double amount, String currency) {
        this.name = name;
        this.startDate = startDate;
        this.cycle = cycle;
        this.cycleTimes = cycleTimes;
        this.amount = amount;
        this.currency = currency;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public String getCycle() {
        return cycle;
    }

    public void setCycle(String cycle) {
        this.cycle = cycle;
    }

    public int getCycleTimes() {
        return cycleTimes;
    }

    public void setCycleTimes(int cycleTimes) {
        this.cycleTimes = cycleTimes;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * Calculate the total amount of the saving plan
     */
    public double calculateTotalAmount() {
        return amount * cycleTimes;
    }

    /**
     * Get the end date of the saving plan based on cycle and cycle times
     */
    public LocalDate calculateEndDate() {
        switch (cycle) {
            case "Daily":
                return startDate.plusDays(cycleTimes);
            case "Weekly":
                return startDate.plusWeeks(cycleTimes);
            case "Monthly":
                return startDate.plusMonths(cycleTimes);
            case "Quarterly":
                return startDate.plusMonths(cycleTimes * 3);
            case "Yearly":
                return startDate.plusYears(cycleTimes);
            default:
                return startDate;
        }
    }

    @Override
    public String toString() {
        return "SavingPlan{" +
                "name='" + name + '\'' +
                ", startDate=" + startDate +
                ", cycle='" + cycle + '\'' +
                ", cycleTimes=" + cycleTimes +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                '}';
    }
}
