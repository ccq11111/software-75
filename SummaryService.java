package com.example.software.service;

import com.example.software.model.CategorySummary;
import com.example.software.model.SummaryResponse;
import com.example.software.model.Record;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SummaryService {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String BILLING_JSON_PATH = "data/billing/billingEntries.json";
    private List<Record> records;

    public SummaryService() {
        loadRecords();
    }

    private void loadRecords() {
        try {
            File jsonFile = new File(BILLING_JSON_PATH);
            if (!jsonFile.exists()) {
                throw new RuntimeException("Billing JSON file not found at: " + BILLING_JSON_PATH);
            }
            
            // 读取 JSON 文件并转换为 Record 对象列表
            List<Map<String, Object>> jsonRecords = objectMapper.readValue(jsonFile, List.class);
            records = jsonRecords.stream()
                .map(record -> new Record(
                    "default_user",// userId
                    (String) record.get("category"), // category
                    (String) record.get("product"), // product
                    ((Number) record.get("price")).doubleValue(), // amount
                    parseDate((String) record.get("date")), // date
                    (String) record.get("time"), // time
                    (String) record.get("remark") // remark
                ))
                .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load billing records", e);
        }
    }

    private LocalDate getStartDate(String periodInput, String customStartDate) {
        if (customStartDate != null && !customStartDate.isEmpty()) {
            return LocalDate.parse(customStartDate);
        }
        
        LocalDate now = LocalDate.now();
        return switch (periodInput.toLowerCase()) {
            case "day" -> now;
            case "week" -> now.minusDays(7);
            case "month" -> now.withDayOfMonth(1);
            case "year" -> now.with(TemporalAdjusters.firstDayOfYear());
            default -> throw new IllegalArgumentException("Invalid period: " + periodInput);
        };
    }

    private LocalDate getEndDate(String customEndDate) {
        if (customEndDate != null && !customEndDate.isEmpty()) {
            return LocalDate.parse(customEndDate);
        }
        return LocalDate.now();
    }

    private SummaryResponse generateSummary(List<Record> records, String period, String startDate, String endDate) {
        LocalDate start = getStartDate(period, startDate);
        LocalDate end = getEndDate(endDate);

        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        List<Record> filtered = records.stream()
                .filter(r -> !r.getDate().isBefore(start) && !r.getDate().isAfter(end))
                .toList();

        Map<String, Double> categoryTotals = new HashMap<>();
        for (Record r : filtered) {
            categoryTotals.merge(r.getCategory(), r.getAmount(), Double::sum);
        }

        double total = categoryTotals.values().stream().mapToDouble(Double::doubleValue).sum();

        List<CategorySummary> summaries = categoryTotals.entrySet().stream()
                .map(e -> new CategorySummary(
                        e.getKey(),
                        e.getValue(),
                        total > 0 ? Math.round(e.getValue() * 10000.0 / total) / 100.0 : 0
                ))
                .collect(Collectors.toList());

        String debugInfo = String.format("查询时间范围: %s 到 %s", start, end);
        System.out.println(debugInfo);

        return new SummaryResponse(true, capitalize(period), total, summaries, debugInfo);
    }

    public SummaryResponse getExpenditureSummary(String period, String startDate, String endDate) {
        loadRecords();
        // 支出类别集合
        List<String> expenditureCategories = List.of(
            "餐饮", "购物", "交通", "娱乐", "医疗", "通讯", "教育", "住宿", "其他", "支出"
        );
        List<Record> expenditureRecords = records.stream()
                .filter(r -> expenditureCategories.contains(r.getCategory()))
                .collect(Collectors.toList());
        return generateSummary(expenditureRecords, period, startDate, endDate);
    }

    public SummaryResponse getIncomeSummary(String period, String startDate, String endDate) {
        loadRecords();
        // 收入类别集合
        List<String> incomeCategories = List.of(
            "工资", "奖金", "报销", "转账", "理财收益", "其他收入", "收入"
        );
        List<Record> incomeRecords = records.stream()
                .filter(r -> incomeCategories.contains(r.getCategory()))
                .collect(Collectors.toList());
        return generateSummary(incomeRecords, period, startDate, endDate);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }

    private LocalDate parseDate(String dateStr) {
        LocalDate today = LocalDate.now();
        if (dateStr == null || dateStr.trim().isEmpty() || "今天".equals(dateStr.trim()) || "today".equalsIgnoreCase(dateStr.trim())) {
            return today;
        } else if ("昨天".equals(dateStr.trim()) || "yesterday".equalsIgnoreCase(dateStr.trim())) {
            return today.minusDays(1);
        } else if ("前天".equals(dateStr.trim())) {
            return today.minusDays(2);
        } else {
            try {
                return LocalDate.parse(dateStr.trim());
            } catch (Exception e) {
                return today;
            }
        }
    }
}
