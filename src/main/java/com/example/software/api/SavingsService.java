package com.example.software.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Interface for savings plan-related API operations
 */
public interface SavingsService {
    
    /**
     * Create a new saving plan
     * 
     * @param name The plan name
     * @param startDate The start date
     * @param cycle The cycle type (Daily, Weekly, Monthly, Quarterly, Yearly)
     * @param cycleTimes The number of cycles
     * @param amount The amount per cycle
     * @param currency The currency code
     * @return Saving plan response
     * @throws ApiException If creation fails
     */
    SavingPlanResponse createPlan(String name, LocalDate startDate, String cycle, 
                                 int cycleTimes, BigDecimal amount, String currency) throws ApiException;
    
    /**
     * Get all saving plans for the authenticated user
     * 
     * @return List of saving plans
     * @throws ApiException If retrieval fails
     */
    List<SavingPlan> getAllPlans() throws ApiException;
    
    /**
     * Update an existing saving plan
     * 
     * @param planId The plan ID
     * @param name The plan name
     * @param startDate The start date
     * @param cycle The cycle type
     * @param cycleTimes The number of cycles
     * @param amount The amount per cycle
     * @param currency The currency code
     * @param savedAmount The amount already saved
     * @return Updated saving plan
     * @throws ApiException If update fails
     */
    SavingPlanResponse updatePlan(String planId, String name, LocalDate startDate, 
                                 String cycle, int cycleTimes, BigDecimal amount, 
                                 String currency, BigDecimal savedAmount) throws ApiException;
    
    /**
     * Delete a saving plan
     * 
     * @param planId The plan ID
     * @return API response
     * @throws ApiException If deletion fails
     */
    ApiResponse deletePlan(String planId) throws ApiException;
}
