package com.example.software.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

/**
 * Mock implementation of the savings service for the test account
 * This implementation works completely offline without any network connections
 */
public class MockSavingsService implements SavingsService {

    private static final Map<String, SavingPlan> savingPlans = new HashMap<>();
    // 使用绝对路径和相对路径结合的方式
    private static final String SAVE_FILE_PATH = System.getProperty("user.dir") + "/purseai/data/savings_plans.json";
    private static final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
        .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
        .configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

    static {
        // 尝试从文件加载保存的计划
        loadSavedPlans();
    }
    
    /**
     * 从文件加载已保存的计划
     */
    private static void loadSavedPlans() {
        File file = new File(SAVE_FILE_PATH);
        System.out.println("尝试从路径加载计划: " + file.getAbsolutePath());
        
        if (file.exists() && file.length() > 0) {
            try {
                System.out.println("文件存在，大小: " + file.length() + " 字节");
                
                // 先尝试直接读取文件内容用于调试
                try {
                    String fileContent = new String(Files.readAllBytes(file.toPath()));
                    System.out.println("文件内容: " + fileContent);
                } catch (Exception e) {
                    System.err.println("读取文件内容出错: " + e.getMessage());
                }
                
                SavingPlan[] plans = objectMapper.readValue(file, SavingPlan[].class);
                for (SavingPlan plan : plans) {
                    savingPlans.put(plan.getPlanId(), plan);
                    System.out.println("已加载计划: ID=" + plan.getPlanId() + ", 名称=" + plan.getName());
                }
                System.out.println("成功从文件加载 " + plans.length + " 个保存计划");
            } catch (IOException e) {
                System.err.println("加载保存计划时出错: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("文件不存在或为空: " + file.getAbsolutePath());
            // 文件不存在时创建一个空文件
            try {
                Path directoryPath = Paths.get(SAVE_FILE_PATH).getParent();
                if (directoryPath != null && !Files.exists(directoryPath)) {
                    Files.createDirectories(directoryPath);
                }
                if (!file.exists()) {
                    file.createNewFile();
                    // 写入一个空数组以初始化文件
                    objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValue(file, new ArrayList<>());
                    System.out.println("已创建新的空计划文件");
                }
            } catch (IOException e) {
                System.err.println("创建新文件时出错: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * 将所有计划保存到文件
     */
    private void savePlansToFile() {
        try {
            File file = new File(SAVE_FILE_PATH);
            System.out.println("保存计划到文件: " + file.getAbsolutePath());
            
            // 确保目录存在
            Path directoryPath = Paths.get(SAVE_FILE_PATH).getParent();
            if (directoryPath != null && !Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }
            
            // 将计划列表写入文件
            objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(file, new ArrayList<>(savingPlans.values()));
            System.out.println("成功保存 " + savingPlans.size() + " 个计划到文件");
        } catch (IOException e) {
            System.err.println("保存计划到文件时出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void addSampleSavingPlans() {
        // 方法保留但不调用，以防未来需要
        // Sample plan 1: Vacation fund
        String planId1 = "plan-" + UUID.randomUUID().toString();
        LocalDate startDate1 = LocalDate.now().minusMonths(2);
        LocalDate endDate1 = startDate1.plusMonths(10);
        SavingPlan plan1 = new SavingPlan(
            planId1,
            "Vacation Fund",
            startDate1,
            endDate1,
            "Monthly",
            12,
            new BigDecimal("500.00"),
            new BigDecimal("6000.00"),
            "USD",
            new BigDecimal("1000.00")
        );
        savingPlans.put(planId1, plan1);

        // Sample plan 2: New laptop
        String planId2 = "plan-" + UUID.randomUUID().toString();
        LocalDate startDate2 = LocalDate.now().minusMonths(1);
        LocalDate endDate2 = startDate2.plusMonths(5);
        SavingPlan plan2 = new SavingPlan(
            planId2,
            "New Laptop",
            startDate2,
            endDate2,
            "Monthly",
            6,
            new BigDecimal("300.00"),
            new BigDecimal("1800.00"),
            "USD",
            new BigDecimal("300.00")
        );
        savingPlans.put(planId2, plan2);
    }

    @Override
    public SavingPlanResponse createPlan(String name, LocalDate startDate, String cycle,
                                        int cycleTimes, BigDecimal amount, String currency) throws ApiException {
        // Calculate end date based on cycle and cycle times
        LocalDate endDate = calculateEndDate(startDate, cycle, cycleTimes);

        // Calculate total amount
        BigDecimal totalAmount = amount.multiply(BigDecimal.valueOf(cycleTimes));

        // Create a new saving plan
        String planId = "plan-" + UUID.randomUUID().toString();
        SavingPlan plan = new SavingPlan(
            planId,
            name,
            startDate,
            endDate,
            cycle,
            cycleTimes,
            amount,
            totalAmount,
            currency,
            BigDecimal.ZERO
        );

        // Save the plan
        savingPlans.put(planId, plan);
        
        // 保存到文件
        savePlansToFile();

        // Return the response
        return new SavingPlanResponse(plan);
    }

    @Override
    public List<SavingPlan> getAllPlans() throws ApiException {
        // Return a copy of all saving plans
        return new ArrayList<>(savingPlans.values());
    }

    @Override
    public SavingPlanResponse updatePlan(String planId, String name, LocalDate startDate,
                                        String cycle, int cycleTimes, BigDecimal amount,
                                        String currency, BigDecimal savedAmount) throws ApiException {
        // Check if the plan exists
        if (!savingPlans.containsKey(planId)) {
            throw new ApiException(
                new ApiError("NOT_FOUND", "Saving plan not found"),
                404
            );
        }

        // Get the existing plan
        SavingPlan existingPlan = savingPlans.get(planId);

        // Calculate end date based on cycle and cycle times
        LocalDate endDate = calculateEndDate(startDate, cycle, cycleTimes);

        // Calculate total amount
        BigDecimal totalAmount = amount.multiply(BigDecimal.valueOf(cycleTimes));

        // Update the plan
        existingPlan.setName(name);
        existingPlan.setStartDate(startDate.atStartOfDay().toInstant(ZoneOffset.UTC));
        existingPlan.setEndDate(endDate.atStartOfDay().toInstant(ZoneOffset.UTC));
        existingPlan.setCycle(cycle);
        existingPlan.setCycleTimes(cycleTimes);
        existingPlan.setAmount(amount);
        existingPlan.setTotalAmount(totalAmount);
        existingPlan.setCurrency(currency);
        existingPlan.setSavedAmount(savedAmount);
        
        // 保存到文件
        savePlansToFile();

        // Return the response
        return new SavingPlanResponse(existingPlan);
    }

    @Override
    public ApiResponse deletePlan(String planId) throws ApiException {
        // Check if the plan exists
        if (!savingPlans.containsKey(planId)) {
            throw new ApiException(
                new ApiError("NOT_FOUND", "Saving plan not found"),
                404
            );
        }

        // Delete the plan
        savingPlans.remove(planId);
        
        // 保存到文件
        savePlansToFile();

        // Return the response
        return new ApiResponse(true, "Plan deleted successfully");
    }

    /**
     * Calculate the end date based on the start date, cycle, and cycle times
     */
    private LocalDate calculateEndDate(LocalDate startDate, String cycle, int cycleTimes) {
        switch (cycle) {
            case "Daily":
                return startDate.plusDays(cycleTimes - 1);
            case "Weekly":
                return startDate.plusWeeks(cycleTimes - 1);
            case "Monthly":
                return startDate.plusMonths(cycleTimes - 1);
            case "Quarterly":
                return startDate.plusMonths((cycleTimes - 1) * 3);
            case "Yearly":
                return startDate.plusYears(cycleTimes - 1);
            default:
                return startDate.plusMonths(cycleTimes - 1);
        }
    }
}
