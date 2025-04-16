package com.example.loginapp.api;

import java.util.List;

/**
 * Interface for AI assistant-related API operations
 */
public interface AIService {
    
    /**
     * Get financial advice from the AI assistant
     * 
     * @param message The user message
     * @param includeTransactions Whether to include transactions in context
     * @param includeSavings Whether to include savings in context
     * @param includeSummary Whether to include summary in context
     * @return AI advice response
     * @throws ApiException If retrieval fails
     */
    AIAdviceResponse getAdvice(String message, boolean includeTransactions, 
                              boolean includeSavings, boolean includeSummary) throws ApiException;
    
    /**
     * Get available quick actions for the AI assistant
     * 
     * @return List of quick actions
     * @throws ApiException If retrieval fails
     */
    List<QuickAction> getQuickActions() throws ApiException;
}
