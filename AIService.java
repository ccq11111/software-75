package com.example.software.api;

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

    /**
     * 获取节日消费分析建议
     * @param csvPath 账单CSV文件路径
     * @return AI建议响应
     * @throws ApiException
     */
    AIAdviceResponse getHolidayAdvice(String csvPath) throws ApiException;

    /**
     * 获取旅游消费建议
     * @param city 目的地
     * @return AI建议响应
     * @throws ApiException
     */
    AIAdviceResponse getTourismAdvice(String city) throws ApiException;

    /**
     * 通用GET请求接口
     */
    String get(String url) throws ApiException;

    /**
     * 发送消费记录，AI自动分类并入账
     */
    String postConsumeRecord(String record) throws ApiException;

    /**
     * 获取周期性交易提醒
     */
    String getPeriodicReminders() throws ApiException;
}
