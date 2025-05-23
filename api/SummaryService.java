package com.example.software.api;

/**
 * Interface for summary-related API operations
 */
public interface SummaryService {
    
    /**
     * Get expenditure summary for the authenticated user
     * 
     * @param period The period (Week, Month, Year)
     * @return Expenditure summary
     * @throws ApiException If retrieval fails
     */
    ExpenditureSummary getExpenditureSummary(String period) throws ApiException;
    
    /**
     * Get income summary for the authenticated user
     * 
     * @param period The period (Week, Month, Year)
     * @return Income summary
     * @throws ApiException If retrieval fails
     */
    IncomeSummary getIncomeSummary(String period) throws ApiException;
}
