package com.example.software.api;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Mock implementation of the summary service for the test account
 * This implementation works completely offline without any network connections
 */
public class MockSummaryService implements SummaryService {

    @Override
    public ExpenditureSummary getExpenditureSummary(String period) throws ApiException {
        // Validate the period
        if (!isValidPeriod(period)) {
            throw new ApiException(
                new ApiError("INVALID_PERIOD", "Invalid period. Must be Week, Month, or Year"),
                400
            );
        }

        // Create sample categories
        List<CategorySummary> categories = new ArrayList<>();
        categories.add(new CategorySummary("Groceries", new BigDecimal("450.75"), 35.0));
        categories.add(new CategorySummary("Dining", new BigDecimal("320.50"), 25.0));
        categories.add(new CategorySummary("Transportation", new BigDecimal("180.25"), 14.0));
        categories.add(new CategorySummary("Entertainment", new BigDecimal("150.00"), 12.0));
        categories.add(new CategorySummary("Shopping", new BigDecimal("180.50"), 14.0));

        // Calculate total
        BigDecimal total = categories.stream()
                .map(CategorySummary::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Return the summary
        return new ExpenditureSummary(period, total, categories);
    }

    @Override
    public IncomeSummary getIncomeSummary(String period) throws ApiException {
        // Validate the period
        if (!isValidPeriod(period)) {
            throw new ApiException(
                new ApiError("INVALID_PERIOD", "Invalid period. Must be Week, Month, or Year"),
                400
            );
        }

        // Create sample categories
        List<CategorySummary> categories = new ArrayList<>();
        categories.add(new CategorySummary("Salary", new BigDecimal("5000.00"), 80.0));
        categories.add(new CategorySummary("Freelance", new BigDecimal("800.00"), 12.8));
        categories.add(new CategorySummary("Investments", new BigDecimal("450.00"), 7.2));

        // Calculate total
        BigDecimal total = categories.stream()
                .map(CategorySummary::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Return the summary
        return new IncomeSummary(period, total, categories);
    }

    /**
     * Check if the period is valid
     */
    private boolean isValidPeriod(String period) {
        return period != null && (period.equals("Week") || period.equals("Month") || period.equals("Year"));
    }
}
