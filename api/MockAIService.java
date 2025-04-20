package com.example.loginapp.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Mock implementation of the AI service for the test account
 * This implementation works completely offline without any network connections
 */
public class MockAIService implements AIService {

    @Override
    public AIAdviceResponse getAdvice(String message, boolean includeTransactions,
                                     boolean includeSavings, boolean includeSummary) throws ApiException {
        // Create sample suggestions
        List<AISuggestion> suggestions = new ArrayList<>();

        // Add suggestions based on the message
        if (message.toLowerCase().contains("save") || message.toLowerCase().contains("saving")) {
            suggestions.add(new AISuggestion("tip", "Consider setting up a monthly automatic transfer to your savings account."));
            suggestions.add(new AISuggestion("tip", "Try the 50/30/20 rule: 50% for needs, 30% for wants, and 20% for savings."));
        } else if (message.toLowerCase().contains("budget") || message.toLowerCase().contains("spend")) {
            suggestions.add(new AISuggestion("tip", "Track your daily expenses for a month to identify spending patterns."));
            suggestions.add(new AISuggestion("tip", "Consider using the envelope budgeting method for discretionary spending."));
        } else if (message.toLowerCase().contains("invest") || message.toLowerCase().contains("investment")) {
            suggestions.add(new AISuggestion("tip", "Start with low-cost index funds if you're new to investing."));
            suggestions.add(new AISuggestion("tip", "Remember to diversify your investment portfolio to reduce risk."));
        } else {
            // Default suggestions
            suggestions.add(new AISuggestion("tip", "Review your recent transactions to identify areas where you can cut back."));
            suggestions.add(new AISuggestion("tip", "Set specific, measurable financial goals for the next 3, 6, and 12 months."));
        }

        // Add a suggestion based on the context
        if (includeTransactions) {
            suggestions.add(new AISuggestion("insight", "Based on your recent transactions, you spend most on dining and groceries."));
        }
        if (includeSavings) {
            suggestions.add(new AISuggestion("insight", "You're making good progress on your vacation savings goal."));
        }
        if (includeSummary) {
            suggestions.add(new AISuggestion("insight", "Your spending this month is 15% higher than last month."));
        }

        // Generate a response message
        String responseMessage = "Here are some financial tips based on your question: \"" + message + "\"";

        // Return the advice response
        return new AIAdviceResponse(responseMessage, suggestions);
    }

    @Override
    public List<QuickAction> getQuickActions() throws ApiException {
        // Create sample quick actions
        List<QuickAction> quickActions = Arrays.asList(
            new QuickAction("budget", "How can I budget better?"),
            new QuickAction("save", "Tips for saving money"),
            new QuickAction("invest", "Investment advice for beginners"),
            new QuickAction("debt", "How to reduce my debt?"),
            new QuickAction("emergency", "Building an emergency fund")
        );

        return quickActions;
    }
}
