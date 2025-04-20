package com.example.loginapp.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Mock implementation of the savings service for the test account
 * This implementation works completely offline without any network connections
 */
public class MockSavingsService implements SavingsService {

    private static final Map<String, SavingPlan> savingPlans = new HashMap<>();

    static {
        // Add some sample saving plans
        addSampleSavingPlans();
    }

    private static void addSampleSavingPlans() {
        // Sample plan 1: Vacation fund
        String planId1 = "plan-" + UUID.randomUUID().toString();
        LocalDate startDate1 = LocalDate.now().minusMonths(2);
        LocalDate endDate1 = startDate1.plusMonths(10);
        SavingPlan plan1 = new SavingPlan(
            planId1,
            "Vacation Fund",
            startDate1,
            endDate1,
            "Monthly",
            12,
            new BigDecimal("500.00"),
            new BigDecimal("6000.00"),
            "USD",
            new BigDecimal("1000.00")
        );
        savingPlans.put(planId1, plan1);

        // Sample plan 2: New laptop
        String planId2 = "plan-" + UUID.randomUUID().toString();
        LocalDate startDate2 = LocalDate.now().minusMonths(1);
        LocalDate endDate2 = startDate2.plusMonths(5);
        SavingPlan plan2 = new SavingPlan(
            planId2,
            "New Laptop",
            startDate2,
            endDate2,
            "Monthly",
            6,
            new BigDecimal("300.00"),
            new BigDecimal("1800.00"),
            "USD",
            new BigDecimal("300.00")
        );
        savingPlans.put(planId2, plan2);
    }

    @Override
    public SavingPlanResponse createPlan(String name, LocalDate startDate, String cycle,
                                        int cycleTimes, BigDecimal amount, String currency) throws ApiException {
        // Calculate end date based on cycle and cycle times
        LocalDate endDate = calculateEndDate(startDate, cycle, cycleTimes);

        // Calculate total amount
        BigDecimal totalAmount = amount.multiply(BigDecimal.valueOf(cycleTimes));

        // Create a new saving plan
        String planId = "plan-" + UUID.randomUUID().toString();
        SavingPlan plan = new SavingPlan(
            planId,
            name,
            startDate,
            endDate,
            cycle,
            cycleTimes,
            amount,
            totalAmount,
            currency,
            BigDecimal.ZERO
        );

        // Save the plan
        savingPlans.put(planId, plan);

        // Return the response
        return new SavingPlanResponse(plan);
    }

    @Override
    public List<SavingPlan> getAllPlans() throws ApiException {
        // Return a copy of all saving plans
        return new ArrayList<>(savingPlans.values());
    }

    @Override
    public SavingPlanResponse updatePlan(String planId, String name, LocalDate startDate,
                                        String cycle, int cycleTimes, BigDecimal amount,
                                        String currency, BigDecimal savedAmount) throws ApiException {
        // Check if the plan exists
        if (!savingPlans.containsKey(planId)) {
            throw new ApiException(
                new ApiError("NOT_FOUND", "Saving plan not found"),
                404
            );
        }

        // Get the existing plan
        SavingPlan existingPlan = savingPlans.get(planId);

        // Calculate end date based on cycle and cycle times
        LocalDate endDate = calculateEndDate(startDate, cycle, cycleTimes);

        // Calculate total amount
        BigDecimal totalAmount = amount.multiply(BigDecimal.valueOf(cycleTimes));

        // Update the plan
        existingPlan.setName(name);
        existingPlan.setStartDate(startDate);
        existingPlan.setEndDate(endDate);
        existingPlan.setCycle(cycle);
        existingPlan.setCycleTimes(cycleTimes);
        existingPlan.setAmount(amount);
        existingPlan.setTotalAmount(totalAmount);
        existingPlan.setCurrency(currency);
        existingPlan.setSavedAmount(savedAmount);

        // Return the response
        return new SavingPlanResponse(existingPlan);
    }

    @Override
    public ApiResponse deletePlan(String planId) throws ApiException {
        // Check if the plan exists
        if (!savingPlans.containsKey(planId)) {
            throw new ApiException(
                new ApiError("NOT_FOUND", "Saving plan not found"),
                404
            );
        }

        // Delete the plan
        savingPlans.remove(planId);

        // Return the response
        return new ApiResponse(true, "Plan deleted successfully");
    }

    /**
     * Calculate the end date based on the start date, cycle, and cycle times
     */
    private LocalDate calculateEndDate(LocalDate startDate, String cycle, int cycleTimes) {
        switch (cycle) {
            case "Daily":
                return startDate.plusDays(cycleTimes - 1);
            case "Weekly":
                return startDate.plusWeeks(cycleTimes - 1);
            case "Monthly":
                return startDate.plusMonths(cycleTimes - 1);
            case "Quarterly":
                return startDate.plusMonths((cycleTimes - 1) * 3);
            case "Yearly":
                return startDate.plusYears(cycleTimes - 1);
            default:
                return startDate.plusMonths(cycleTimes - 1);
        }
    }
}
