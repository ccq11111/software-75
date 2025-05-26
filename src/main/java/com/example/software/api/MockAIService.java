package com.example.software.api;

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

    @Override
    public AIAdviceResponse getHolidayAdvice(String csvPath) throws ApiException {
        return null;
    }

    @Override
    public AIAdviceResponse getTourismAdvice(String city) throws ApiException {
        String msg = "为您规划去" + city + "的旅游消费：\n" +
                "建议预算分配：住宿40%，餐饮30%，交通20%，购物10%。\n" +
                "推荐必买特产和必玩景点，祝您旅途愉快！";
        return new AIAdviceResponse(msg, new ArrayList<>());
    }

    @Override
    public String postConsumeRecord(String record) throws ApiException {
        return "已自动分类并入账: " + record;
    }

    @Override
    public String getPeriodicReminders() throws ApiException {
        return "根据您的历史交易记录，以下是预计未来30天内可能发生的周期性交易：\n\n" +
               "| 类别 | 产品 | 金额 | 周期(天) | 预计日期 | 剩余天数 |\n" +
               "|------|------|------|---------|----------|----------|\n" +
               "| 通讯 | 手机话费 | 50.00 | 30 | 2023-06-15 | 7 |\n" +
               "| 生活费用 | 水电费 | 120.00 | 90 | 2023-06-20 | 12 |\n" +
               "| 交通 | 公交卡充值 | 100.00 | 60 | 2023-06-25 | 17 |\n\n" +
               "请注意：以上预测基于历史交易规律，仅供参考。";
    }

    @Override
    public String get(String url) throws ApiException {
        if (url.contains("consume-analysis")) {
            return "你的消费主要集中在餐饮和娱乐，下月建议餐饮预算控制在1000元以内。";
        }
        return "模拟GET请求返回：" + url;
    }

}
