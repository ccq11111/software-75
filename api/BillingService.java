package com.example.loginapp.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.io.File;

/**
 * Interface for billing-related API operations
 */
public interface BillingService {
    
    /**
     * Create a new billing entry
     * 
     * @param category The category
     * @param product The product name
     * @param price The price
     * @param date The date
     * @param time The time
     * @param remark Additional remarks
     * @return Billing entry response
     * @throws ApiException If creation fails
     */
    BillingEntryResponse createEntry(String category, String product, BigDecimal price, 
                                    LocalDate date, LocalTime time, String remark) throws ApiException;
    
    /**
     * Get all billing entries for the authenticated user
     * 
     * @param startDate Optional start date filter
     * @param endDate Optional end date filter
     * @param category Optional category filter
     * @param searchTerm Optional search term
     * @return List of billing entries
     * @throws ApiException If retrieval fails
     */
    List<BillingEntry> getEntries(LocalDate startDate, LocalDate endDate, 
                                 String category, String searchTerm) throws ApiException;
    
    /**
     * Update an existing billing entry
     * 
     * @param entryId The entry ID
     * @param category The category
     * @param product The product name
     * @param price The price
     * @param date The date
     * @param time The time
     * @param remark Additional remarks
     * @return Updated billing entry
     * @throws ApiException If update fails
     */
    BillingEntryResponse updateEntry(String entryId, String category, String product, 
                                    BigDecimal price, LocalDate date, LocalTime time, 
                                    String remark) throws ApiException;
    
    /**
     * Delete a billing entry
     * 
     * @param entryId The entry ID
     * @return API response
     * @throws ApiException If deletion fails
     */
    ApiResponse deleteEntry(String entryId) throws ApiException;
    
    /**
     * Import billing entries from a CSV file
     * 
     * @param file The CSV file
     * @return Import response
     * @throws ApiException If import fails
     */
    ImportResponse importFromCsv(File file) throws ApiException;
}
