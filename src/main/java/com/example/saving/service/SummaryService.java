package com.example.saving.service;

import com.example.saving.model.CategorySummary;
import com.example.saving.model.SummaryResponse;
import com.example.saving.model.Record;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
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
                    LocalDate.parse((String) record.get("date")), // date
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
        List<Record> expenditureRecords = records.stream()
                .filter(r -> !r.getCategory().equals("工资") && !r.getCategory().equals("奖金"))
                .collect(Collectors.toList());
        return generateSummary(expenditureRecords, period, startDate, endDate);
    }

    public SummaryResponse getIncomeSummary(String period, String startDate, String endDate) {
        List<Record> incomeRecords = records.stream()
                .filter(r -> r.getCategory().equals("工资") || r.getCategory().equals("奖金"))
                .collect(Collectors.toList());
        return generateSummary(incomeRecords, period, startDate, endDate);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
