package com.example.software.repository;

import com.example.software.model.SavingsPlan;
import com.example.software.model.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
public class FileBasedSavingsPlanRepository implements SavingsPlanRepository {
    
    private static final Logger logger = Logger.getLogger(FileBasedSavingsPlanRepository.class.getName());
    private static final String DATA_DIR = "data";
    private static final String PLANS_FILE = "savings_plans.json";
    private static final String LOGS_FILE = "transactions.log";
    
    private final ObjectMapper objectMapper;
    private final Path dataDirectory;
    private final Path plansFilePath;
    private final Path logsFilePath;
    
    // 内存缓存
    private final Map<String, SavingsPlan> planCache = new ConcurrentHashMap<>();
    
    public FileBasedSavingsPlanRepository() {
        this.objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(SerializationFeature.INDENT_OUTPUT);
            
        this.dataDirectory = Paths.get(DATA_DIR);
        this.plansFilePath = dataDirectory.resolve(PLANS_FILE);
        this.logsFilePath = dataDirectory.resolve(LOGS_FILE);
        
        initialize();
    }
    
    private void initialize() {
        try {
            // 创建数据目录
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
            }
            
            // 如果计划文件存在，加载到内存
            if (Files.exists(plansFilePath)) {
                List<SavingsPlan> plans = objectMapper.readValue(
                    plansFilePath.toFile(),
                    new TypeReference<List<SavingsPlan>>() {}
                );
                
                for (SavingsPlan plan : plans) {
                    planCache.put(plan.getPlanId(), plan);
                }
                
                logger.info("已加载 " + plans.size() + " 个储蓄计划");
            } else {
                // 创建空文件
                saveToFile();
                logger.info("创建了新的储蓄计划存储文件");
            }
        } catch (IOException e) {
            logger.severe("初始化储蓄计划数据时出错: " + e.getMessage());
        }
    }
    
    private void saveToFile() {
        try {
            objectMapper.writeValue(plansFilePath.toFile(), planCache.values());
            logger.info("储蓄计划数据已保存到文件");
        } catch (IOException e) {
            logger.severe("保存储蓄计划数据时出错: " + e.getMessage());
        }
    }
    
    private void logTransaction(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logsFilePath.toFile(), true))) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            writer.write(String.format("[%s] %s%n", timestamp, message));
        } catch (IOException e) {
            logger.severe("记录交易日志时出错: " + e.getMessage());
        }
    }
    
    @Override
    public SavingsPlan save(SavingsPlan savingsPlan) {
        // 如果没有ID则生成一个
        if (savingsPlan.getPlanId() == null || savingsPlan.getPlanId().isEmpty()) {
            savingsPlan.setPlanId(UUID.randomUUID().toString());
            printPlanDetails(savingsPlan); // 仅在新建时打印详情
        }
        
        planCache.put(savingsPlan.getPlanId(), savingsPlan);
        saveToFile();
        
        return savingsPlan;
    }
    
    @Override
    public List<SavingsPlan> findByUser(User user) {
        return planCache.values().stream()
            .filter(plan -> 
                plan.getUser().getUserId() != null && plan.getUser().getUserId().equals(user.getUserId()))
            .collect(Collectors.toList());
    }
    
    @Override
    public Optional<SavingsPlan> findByPlanIdAndUser(String planId, User user) {
        SavingsPlan plan = planCache.get(planId);
        if (plan != null && 
            plan.getUser().getUserId() != null &&
            plan.getUser().getUserId().equals(user.getUserId())) {
            return Optional.of(plan);
        }
        return Optional.empty();
    }
    
    @Override
    public List<SavingsPlan> findAll() {
        return new ArrayList<>(planCache.values());
    }
    
    @Override
    public void deleteById(String planId) {
        SavingsPlan removed = planCache.remove(planId);
        if (removed != null) {
            saveToFile();
            logTransaction("删除了储蓄计划: " + removed.getName() + " (ID: " + planId + ")");
        }
    }
    
    @Override
    public Optional<SavingsPlan> findById(String planId) {
        return Optional.ofNullable(planCache.get(planId));
    }
    
    // 打印存储计划详情到控制台和日志
    private void printPlanDetails(SavingsPlan plan) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========== 储蓄计划详情 ==========\n");
        sb.append(String.format("计划 '%s' 创建成功!\n\n", plan.getName()));
        sb.append(String.format("计划ID: %s\n", plan.getPlanId()));
        sb.append(String.format("开始日期: %s\n", formatDate(plan.getStartDate())));
        sb.append(String.format("结束日期: %s\n", formatDate(plan.getEndDate())));
        sb.append(String.format("周期: %s\n", plan.getCycle()));
        sb.append(String.format("周期次数: %d\n", plan.getCycleTimes()));
        sb.append(String.format("每期金额: %.2f %s\n", plan.getAmount().doubleValue(), plan.getCurrency()));
        sb.append(String.format("总金额: %.2f %s\n", plan.getTotalAmount().doubleValue(), plan.getCurrency()));
        sb.append("====================================\n");
        
        System.out.println(sb.toString());
        logTransaction("创建新计划: " + sb.toString().replace("\n", " "));
    }
    
    private String formatDate(Instant date) {
        if (date == null) return "N/A";
        return DateTimeFormatter.ISO_LOCAL_DATE
            .format(date.atZone(ZoneId.systemDefault()).toLocalDate());
    }
}
