package com.example.loginapp.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Model class for saving plans
 * This is a singleton class to share data between controllers
 */
public class SavingPlanModel {
    private static SavingPlanModel instance;
    private ObservableList<SavingPlan> plans;
    
    private SavingPlanModel() {
        plans = FXCollections.observableArrayList();
    }
    
    public static SavingPlanModel getInstance() {
        if (instance == null) {
            instance = new SavingPlanModel();
        }
        return instance;
    }
    
    public ObservableList<SavingPlan> getPlans() {
        return plans;
    }
    
    public void addPlan(SavingPlan plan) {
        plans.add(plan);
    }
    
    /**
     * Inner class to represent a saving plan
     */
    public static class SavingPlan {
        private String name;
        private LocalDate startDate;
        private String cycle;
        private int cycleTimes;
        private double amount;
        private String currency;
        private double savedAmount;
        
        public SavingPlan(String name, LocalDate startDate, String cycle, int cycleTimes, double amount, String currency) {
            this.name = name;
            this.startDate = startDate;
            this.cycle = cycle;
            this.cycleTimes = cycleTimes;
            this.amount = amount;
            this.currency = currency;
            this.savedAmount = 0.0; // Initially no money saved
        }
        
        public String getName() {
            return name;
        }
        
        public LocalDate getStartDate() {
            return startDate;
        }
        
        public String getFormattedStartDate() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");
            return startDate.atTime(14, 29).format(formatter); // Adding a fixed time for display
        }
        
        public String getCycle() {
            return cycle;
        }
        
        public int getCycleTimes() {
            return cycleTimes;
        }
        
        public double getAmount() {
            return amount;
        }
        
        public String getCurrency() {
            return currency;
        }
        
        public double getSavedAmount() {
            return savedAmount;
        }
        
        public void setSavedAmount(double savedAmount) {
            this.savedAmount = savedAmount;
        }
        
        public String getCycleTimeDisplay() {
            return cycleTimes + " " + getCycleDaysText();
        }
        
        private String getCycleDaysText() {
            switch (cycle) {
                case "Daily":
                    return "days";
                case "Weekly":
                    return "weeks";
                case "Monthly":
                    return "months";
                case "Quarterly":
                    return "quarters";
                case "Yearly":
                    return "years";
                default:
                    return "days";
            }
        }
        
        public String getAimMoneyDisplay() {
            String currencySymbol = getCurrencySymbol();
            return currencySymbol + " " + String.format("%.0f", amount * cycleTimes);
        }
        
        public String getSavedDisplay() {
            String currencySymbol = getCurrencySymbol();
            return currencySymbol + String.format("%.0f", savedAmount);
        }
        
        public String getTargetDisplay() {
            String currencySymbol = getCurrencySymbol();
            return currencySymbol + String.format("%.0f", amount * cycleTimes);
        }
        
        private String getCurrencySymbol() {
            if (currency.startsWith("CNY")) {
                return "¥";
            } else if (currency.startsWith("USD")) {
                return "USD";
            } else if (currency.startsWith("EUR")) {
                return "EUR";
            } else if (currency.startsWith("GBP")) {
                return "GBP";
            } else if (currency.startsWith("JPY")) {
                return "JPY";
            } else {
                return "";
            }
        }
        
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
                    return startDate.plusDays(cycleTimes);
            }
        }
        
        public double calculateTotalAmount() {
            return amount * cycleTimes;
        }
    }
}
