package com.example.software.service;

import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.stereotype.Service;
import com.example.software.api.BillingEntry;
import com.example.software.util.HolidayUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import com.example.software.util.TourismDataUtil;
import java.time.format.DateTimeFormatter;

@Service
public class AiService {
    private final OllamaChatClient chatClient;

    public AiService(OllamaChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String chat(String message) {
        ChatResponse response = chatClient.call(new Prompt(message));
        return response.getResult().getOutput().getContent();
    }

    public static String filterMarkdown(String text) {
        return text
            .replaceAll("(?m)^#+\\s*", "") // 去除标题
            .replaceAll("\\*\\*([^*]+)\\*\\*", "$1") // 去除加粗
            .replaceAll("\\*([^*]+)\\*", "$1") // 去除斜体
            .replaceAll("^-\\s*", "") // 去除无序列表
            .replaceAll("`([^`]+)`", "$1") // 去除行内代码
            .replaceAll(">\\s*", "") // 去除引用
            .replaceAll("\\n{2,}", "\n") // 多余空行合并
            .replaceAll("expensive", "高价") // 过滤英文单词
            .trim();
    }

    /**
     * 节日消费分析与建议
     * @param csvPath 账单CSV文件路径
     * @return AI生成的节日支出建议
     */
    public String holidaySpendingAdvice(String csvPath) {
        try {
            // 1. 读取账单数据
            List<BillingEntry> entries = new ArrayList<>();
            File csvFile = new File(csvPath);
            if (!csvFile.exists()) return "未找到账单数据文件。";
            try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
                String line = reader.readLine(); // 跳过头行
                while ((line = reader.readLine()) != null) {
                    String[] values = line.split(",");
                    if (values.length >= 5) {
                        BillingEntry entry = new BillingEntry();
                        entry.setCategory(values[0].replace("\"", "").trim());
                        entry.setProduct(values[1].replace("\"", "").trim());
                        try { entry.setPrice(new BigDecimal(values[2].replace("\"", "").trim())); } catch (Exception e) { entry.setPrice(BigDecimal.ZERO); }
                        try { entry.setDate(LocalDate.parse(values[3].replace("\"", "").trim())); } catch (Exception e) { entry.setDate(LocalDate.now()); }
                        entry.setRemark(values.length > 5 ? values[5].replace("\"", "").trim() : entry.getProduct());
                        entries.add(entry);
                    }
                }
            }
            // 2. 查找最近节日
            LocalDate today = LocalDate.now();
            Optional<HolidayUtil.HolidayInfo> holidayOpt = HolidayUtil.getUpcomingHoliday(today, 30);
            if (holidayOpt.isEmpty()) return "未来30天内无常见节日。";
            HolidayUtil.HolidayInfo holiday = holidayOpt.get();
            // 3. 统计节日前后7天的消费
            LocalDate start = holiday.date.minusDays(3);
            LocalDate end = holiday.date.plusDays(3);
            List<BillingEntry> holidayEntries = new ArrayList<>();
            for (BillingEntry e : entries) {
                if (e.getDate() != null && !e.getDate().isBefore(start) && !e.getDate().isAfter(end)) {
                    holidayEntries.add(e);
                }
            }
            BigDecimal total = holidayEntries.stream().map(BillingEntry::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
            Map<String, BigDecimal> categoryMap = new HashMap<>();
            for (BillingEntry e : holidayEntries) {
                categoryMap.merge(e.getCategory(), e.getPrice(), BigDecimal::add);
            }
            // 4. 构造AI分析请求
            StringBuilder prompt = new StringBuilder();
            prompt.append("请根据以下账单数据，分析即将到来的节日\"")
                  .append(holiday.name)
                  .append("\"的消费情况，并给出合理的支出建议。\n");
            prompt.append("节日时间：").append(holiday.date).append("\n");
            prompt.append("节日前后7天总支出：").append(total).append("元\n");
            prompt.append("各类别支出：\n");
            for (Map.Entry<String, BigDecimal> entry : categoryMap.entrySet()) {
                prompt.append(entry.getKey()).append(": ").append(entry.getValue()).append("元\n");
            }
            prompt.append("请结合节日特点，给出餐饮、礼品、出行等方面的预算建议。");
            // 5. 调用AI模型
            return filterMarkdown(chat(prompt.toString()));
        } catch (Exception e) {
            return "节日消费分析失败：" + e.getMessage();
        }
    }

    /**
     * 旅游消费规划与建议
     * @param city 目的地城市
     * @return AI生成的旅游支出建议
     */
    public String tourismAdvice(String city) {
        TourismDataUtil.DestinationInfo info = TourismDataUtil.getDestinationInfo(city);
        if (info == null) {
            return "暂未收录该城市的旅游信息，请换一个目的地试试。";
        }
        StringBuilder prompt = new StringBuilder();
        prompt.append("请为用户规划一次去").append(info.name).append("的旅游消费：\n");
        prompt.append("当地人均月收入：").append(info.avgIncome).append("元\n");
        prompt.append("当地特色美食/特产：").append(info.specialties).append("\n");
        prompt.append("主要景点：").append(info.attractions).append("\n");
        prompt.append("旅游建议：").append(info.tips).append("\n");
        prompt.append("请结合以上信息，给出合理的预算分配（如住宿、餐饮、交通、购物等），并推荐必买特产和必玩景点。");
        return filterMarkdown(chat(prompt.toString()));
    }

    /**
     * 消费模式分析与预算建议
     * @return AI生成的消费模式分析和预算建议
     */
    public String consumeAnalysis() {
        try {
            // 1. 读取账单数据
            List<BillingEntry> entries = new ArrayList<>();
            String csvPath = "data/billing/billingEntries.csv";
            File csvFile = new File(csvPath);
            if (!csvFile.exists()) return "未找到账单数据文件。";
            try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
                String line = reader.readLine(); // 跳过头行
                while ((line = reader.readLine()) != null) {
                    String[] values = line.split(",");
                    if (values.length >= 5) {
                        BillingEntry entry = new BillingEntry();
                        entry.setCategory(values[0].replace("\"", "").trim());
                        entry.setProduct(values[1].replace("\"", "").trim());
                        try { entry.setPrice(new BigDecimal(values[2].replace("\"", "").trim())); } catch (Exception e) { entry.setPrice(BigDecimal.ZERO); }
                        try { entry.setDate(LocalDate.parse(values[3].replace("\"", "").trim())); } catch (Exception e) { entry.setDate(LocalDate.now()); }
                        entry.setRemark(values.length > 5 ? values[5].replace("\"", "").trim() : entry.getProduct());
                        entries.add(entry);
                    }
                }
            }
            // 2. 构造AI分析请求
            StringBuilder prompt = new StringBuilder();
            prompt.append("请根据以下账单数据，分析用户的消费模式，并给出下月预算建议：\n");
            for (BillingEntry e : entries) {
                prompt.append(e.getCategory()).append(",")
                      .append(e.getProduct()).append(",")
                      .append(e.getPrice()).append(",")
                      .append(e.getDate()).append("\n");
            }
            return filterMarkdown(chat(prompt.toString()));
        } catch (Exception e) {
            return "消费模式分析失败：" + e.getMessage();
        }
    }

    /**
     * 消费记录自动分类并入账
     * @param record 消费描述
     * @return 结果字符串
     */
    public String consumeRecordAndAdd(String record) {
        try {
            // 1. 调用AI模型结构化消费记录
            String prompt = "请将以下消费描述结构化为账单条目，输出JSON，字段包括：category, product, price, date（yyyy-MM-dd），time（HH:mm），remark。\n消费描述：" + record;
            String aiResult = chat(prompt);
            // 2. 解析AI返回的JSON
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            
            // 处理可能的格式问题，提取JSON部分
            if (aiResult.contains("{") && aiResult.contains("}")) {
                int start = aiResult.indexOf("{");
                int end = aiResult.lastIndexOf("}") + 1;
                aiResult = aiResult.substring(start, end);
            }
            
            Map<String, Object> map = mapper.readValue(aiResult, Map.class);
            BillingEntry entry = new BillingEntry();
            entry.setEntryId(UUID.randomUUID().toString().substring(0, 8));
            entry.setCategory((String) map.getOrDefault("category", "其他"));
            entry.setProduct((String) map.getOrDefault("product", "未指定"));
            entry.setPrice(new BigDecimal(map.getOrDefault("price", "0").toString()));
            
            // 日期处理
            String dateStr = (String) map.getOrDefault("date", LocalDate.now().toString());
            entry.setDate(LocalDate.parse(dateStr));
            
            // 时间处理
            String timeStr = (String) map.getOrDefault("time", java.time.LocalTime.now().toString());
            entry.setTime(java.time.LocalTime.parse(timeStr));
            entry.setFormattedTime(entry.getTime().toString());
            
            entry.setRemark((String) map.getOrDefault("remark", record));
            
            // 3. 写入三种格式的账单文件
            saveToCSV(entry);
            saveToJSON(entry);
            saveToTXT(entry);
            
            return String.format("已自动分类并入账：类别：%s，产品：%s，金额：%s元，日期：%s。",
                    entry.getCategory(), entry.getProduct(), entry.getPrice(), entry.getDate());
        } catch (Exception e) {
            e.printStackTrace();
            return "消费记录入账失败：" + e.getMessage();
        }
    }

    /**
     * 保存到CSV文件
     */
    private void saveToCSV(BillingEntry entry) throws Exception {
        String csvPath = "data/billing/billingEntries.csv";
        File csvFile = new File(csvPath);
        boolean fileExists = csvFile.exists();
        try (java.io.FileWriter fw = new java.io.FileWriter(csvFile, true)) {
            if (!fileExists) {
                fw.write("类别,产品,价格,日期,时间,备注\n");
            }
            fw.write(String.format("\"%s\",\"%s\",%.2f,\"%s\",\"%s\",\"%s\"\n",
                    entry.getCategory(), entry.getProduct(), entry.getPrice(), entry.getDate(), 
                    entry.getFormattedTime(), entry.getRemark()));
        }
    }

    /**
     * 保存到JSON文件
     */
    private void saveToJSON(BillingEntry entry) throws Exception {
        String jsonPath = "data/billing/billingEntries.json";
        File jsonFile = new File(jsonPath);
        
        // 读取现有数据
        List<BillingEntry> entries = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        
        if (jsonFile.exists() && jsonFile.length() > 0) {
            try {
                entries = mapper.readValue(jsonFile, 
                        mapper.getTypeFactory().constructCollectionType(List.class, BillingEntry.class));
            } catch (Exception e) {
                System.err.println("JSON文件读取失败，将创建新文件：" + e.getMessage());
            }
        }
        
        // 添加新条目
        entries.add(entry);
        
        // 写回文件
        mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, entries);
    }

    /**
     * 保存到TXT文件
     */
    private void saveToTXT(BillingEntry entry) throws Exception {
        String txtPath = "data/billing/billingEntries.txt";
        File txtFile = new File(txtPath);
        try (java.io.FileWriter fw = new java.io.FileWriter(txtFile, true)) {
            fw.write(String.format("%s, %s,%.2f,%s,%s,%s\n",
                    entry.getCategory(), entry.getProduct(), entry.getPrice(), entry.getDate(), 
                    entry.getFormattedTime(), entry.getRemark()));
        }
    }

    /**
     * 分析周期性交易模式并生成提醒
     * @return 周期性交易提醒信息
     */
    public String periodicReminders() {
        try {
            // 1. 读取账单数据
            List<BillingEntry> entries = new ArrayList<>();
            String csvPath = "data/billing/billingEntries.csv";
            File csvFile = new File(csvPath);
            if (!csvFile.exists()) return "未找到账单数据文件。";

            try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
                String line = reader.readLine(); // 跳过头行
                while ((line = reader.readLine()) != null) {
                    String[] values = line.split(",");
                    if (values.length >= 5) {
                        BillingEntry entry = new BillingEntry();
                        entry.setCategory(values[0].replace("\"", "").trim());
                        entry.setProduct(values[1].replace("\"", "").trim());
                        try { entry.setPrice(new BigDecimal(values[2].replace("\"", "").trim())); } catch (Exception e) { entry.setPrice(BigDecimal.ZERO); }
                        try { entry.setDate(LocalDate.parse(values[3].replace("\"", "").trim())); } catch (Exception e) { entry.setDate(LocalDate.now()); }
                        entry.setRemark(values.length > 5 ? values[5].replace("\"", "").trim() : entry.getProduct());
                        entries.add(entry);
                    }
                }
            }

            // 2. 对交易进行分组并分析周期性
            Map<String, List<BillingEntry>> productGroups = new HashMap<>();
            for (BillingEntry entry : entries) {
                String key = entry.getCategory() + ":" + entry.getProduct();
                productGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(entry);
            }

            // 3. 分析每组交易的周期性
            List<PeriodicTransaction> periodicTransactions = new ArrayList<>();

            for (Map.Entry<String, List<BillingEntry>> group : productGroups.entrySet()) {
                List<BillingEntry> transactions = group.getValue();
                if (transactions.size() < 2) continue;
                transactions.sort(Comparator.comparing(BillingEntry::getDate));

                // 计算日期间隔
                List<Integer> intervals = new ArrayList<>();
                for (int i = 1; i < transactions.size(); i++) {
                    LocalDate prevDate = transactions.get(i-1).getDate();
                    LocalDate currDate = transactions.get(i).getDate();
                    int daysBetween = (int) java.time.temporal.ChronoUnit.DAYS.between(prevDate, currDate);
                    intervals.add(daysBetween);
                }

                // 统计"相近间隔"（±2天）的出现次数
                Map<Integer, Integer> intervalCounts = new HashMap<>();
                for (int interval : intervals) {
                    boolean found = false;
                    for (Integer key : intervalCounts.keySet()) {
                        if (Math.abs(key - interval) <= 2) { // 容忍2天误差
                            intervalCounts.put(key, intervalCounts.get(key) + 1);
                            found = true;
                            break;
                        }
                    }
                    if (!found) intervalCounts.put(interval, 1);
                }

                // 找出出现次数最多的"相近间隔"
                int maxCount = 0;
                int mostCommonInterval = 0;
                for (Map.Entry<Integer, Integer> entry : intervalCounts.entrySet()) {
                    if (entry.getValue() > maxCount) {
                        maxCount = entry.getValue();
                        mostCommonInterval = entry.getKey();
                    }
                }

                // 只要有2次及以上的"相近间隔"，就认为是周期性
                if (maxCount >= 2 && mostCommonInterval > 0 && mostCommonInterval <= 365) {
                    BillingEntry lastTransaction = transactions.get(transactions.size() - 1);
                    LocalDate nextDate = lastTransaction.getDate().plusDays(mostCommonInterval);
                    int daysUntilNext = (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), nextDate);

                    if (daysUntilNext > 0 && daysUntilNext <= 30) {  // 只提醒未来30天内的交易
                        String[] parts = group.getKey().split(":");
                        PeriodicTransaction pt = new PeriodicTransaction(
                            parts[0], // 类别
                            parts[1], // 产品
                            lastTransaction.getPrice(), // 金额
                            mostCommonInterval, // 周期天数
                            nextDate, // 下次预计日期
                            daysUntilNext // 距今天数
                        );
                        periodicTransactions.add(pt);
                    }
                }
            }

            // 4. 生成输出
            if (periodicTransactions.isEmpty()) {
                return "未发现即将到来的周期性交易。";
            }

            // 按距离时间排序
            periodicTransactions.sort(Comparator.comparing(PeriodicTransaction::getDaysUntilNext));

            // 构造表格输出
            StringBuilder result = new StringBuilder();
            result.append("根据您的历史交易记录，以下是预计未来30天内可能发生的周期性交易：\n\n");
            result.append("| 类别 | 产品 | 金额 | 周期(天) | 预计日期 | 剩余天数 |\n");
            result.append("|------|------|------|---------|----------|----------|\n");

            for (PeriodicTransaction pt : periodicTransactions) {
                result.append(String.format("| %s | %s | %.2f | %d | %s | %d |\n",
                    pt.getCategory(),
                    pt.getProduct(),
                    pt.getAmount(),
                    pt.getIntervalDays(),
                    pt.getNextDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    pt.getDaysUntilNext()
                ));
            }

            result.append("\n请注意：以上预测基于历史交易规律，仅供参考。");

            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "分析周期性交易时出错：" + e.getMessage();
        }
    }

    /**
     * 周期性交易类，用于存储分析结果
     */
    private static class PeriodicTransaction {
        private final String category;
        private final String product;
        private final BigDecimal amount;
        private final int intervalDays;
        private final LocalDate nextDate;
        private final int daysUntilNext;
        
        public PeriodicTransaction(String category, String product, BigDecimal amount, 
                                  int intervalDays, LocalDate nextDate, int daysUntilNext) {
            this.category = category;
            this.product = product;
            this.amount = amount;
            this.intervalDays = intervalDays;
            this.nextDate = nextDate;
            this.daysUntilNext = daysUntilNext;
        }
        
        public String getCategory() { return category; }
        public String getProduct() { return product; }
        public BigDecimal getAmount() { return amount; }
        public int getIntervalDays() { return intervalDays; }
        public LocalDate getNextDate() { return nextDate; }
        public int getDaysUntilNext() { return daysUntilNext; }
    }
} 