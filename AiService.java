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
import java.time.LocalTime;
import java.util.*;
import com.example.software.util.TourismDataUtil;
import java.time.format.DateTimeFormatter;
import java.util.regex.*;
import java.util.stream.Collectors;


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
    public static String filterThinkTags(String input) {
        return input.replaceAll("(?s)<think>.*?</think>", "");
    }
    public static String filterThinkTags2(String input) {
        String prev;
        do {
            prev = input;
            input = input.replaceAll("(?is)<think[^>]*>.*?</think>", "");
            input = input.replaceAll("(?is)<think[^>]*>", "");
            input = input.replaceAll("(?is)</think>", "");
        } while (!input.equals(prev));
        return input;
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
            if (!csvFile.exists()) return "The bill data file was not found.";
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
            // 2. 筛选今天往前7天（含今天）的账单数据
            LocalDate today = LocalDate.now();
            LocalDate sevenDaysAgo = today.minusDays(6); // 包含今天共7天
            List<BillingEntry> recentEntries = new ArrayList<>();
            for (BillingEntry e : entries) {
                if (e.getDate() != null && !e.getDate().isBefore(sevenDaysAgo) && !e.getDate().isAfter(today)) {
                    recentEntries.add(e);
                }
            }
            BigDecimal total = recentEntries.stream().map(BillingEntry::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
            Map<String, BigDecimal> categoryMap = new HashMap<>();
            for (BillingEntry e : recentEntries) {
                categoryMap.merge(e.getCategory(), e.getPrice(), BigDecimal::add);
            }
            // 3. 构造AI分析请求
            StringBuilder prompt = new StringBuilder();
            prompt.append("Please analyze the following bill data and provide suggestions for the upcoming festival.\n");
            prompt.append("Festival: ").append("Recent 7-day Consumption").append("\n");
            prompt.append("Analysis period: ").append(sevenDaysAgo).append(" to ").append(today).append("\n");
            prompt.append("Total expenditure in the recent 7 days: ").append(total).append(" yuan\n");
            prompt.append("Expenditure by category:\n");
            for (Map.Entry<String, BigDecimal> entry : categoryMap.entrySet()) {
                prompt.append(entry.getKey()).append(": ").append(entry.getValue()).append(" yuan\n");
            }
            prompt.append("Based on the recent 7-day consumption, please provide reasonable budget suggestions for dining, gifts, travel, etc. for the upcoming festival.\n");
            prompt.append("Please answer in English.\n");
            // 4. 调用AI模型
            return filterMarkdown(filterThinkTags2(chat(prompt.toString())));
        } catch (Exception e) {
            return "Failure of festival consumption analysis:" + e.getMessage();
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
            return "The tourism information of this city has not been included yet. Please try another destination.";
        }
        StringBuilder prompt = new StringBuilder();
        prompt.append("Please provide a detailed travel spending plan and suggestions for a trip to ").append(info.name).append(".\n");
        prompt.append("Local average monthly income: ").append(info.avgIncome).append(" yuan\n");
        prompt.append("Local specialties: ").append(info.specialties).append("\n");
        prompt.append("Main attractions: ").append(info.attractions).append("\n");
        prompt.append("Travel tips: ").append(info.tips).append("\n");
        prompt.append("Based on the above information, please give a reasonable budget allocation (such as accommodation, food, transportation, shopping, etc.), and recommend must-buy specialties and must-visit attractions.\n");
        prompt.append("Please answer in English.\n");
        String aiResult = chat(prompt.toString());
        aiResult = filterThinkTags2(aiResult);
        return filterMarkdown(aiResult);
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
            if (!csvFile.exists()) return "The bill data file was not found.";
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
            // 2. 筛选近三个月的数据
            LocalDate today = LocalDate.now();
            LocalDate threeMonthsAgo = today.minusMonths(3);
            List<BillingEntry> filteredEntries = entries.stream()
                .filter(e -> !e.getDate().isBefore(threeMonthsAgo) && !e.getDate().isAfter(today))
                .collect(Collectors.toList());
            // 3. 构造AI分析请求（只用filteredEntries）
            String prompt = """
Please analyze the following bill data and provide a consumption pattern analysis and budget suggestions for next month.
Requirements:
1. Output only the analysis results and suggestions. Do not include any thinking process, format check, data cleaning, or requirement clarification.
2. Do not output any <think> tags or thought content.
3. The analysis should include:
a) Expenditure structure model: Analyze the expenditure proportion by category (such as catering, transportation, entertainment, etc.), and indicate which categories have a higher proportion than the average level.
b) Trend change pattern: Analyze the fluctuation trend and growth trajectory of income and expenditure in recent months, and point out the characteristics of changes in expenditure or income.
c) Consumption frequency: Analyze small and high-frequency consumption, abnormal expenditures during holidays or specific time periods, and indicate whether there are phenomena such as emotional consumption.
4. Finally, provide two versions of the budget suggestions for next month:
- Savings plan version: Generate personalized budget suggestions for the next month based on the user's historical consumption records, including approximate expenditure allocation, specific amount range, and estimated savings amount.
- No savings plan version: Provide a normal budget suggestion for next month.
5. The output should be well-organized and described point by point, facilitating users' understanding and reference.

The bill data is as follows (each line: category, product, amount, date):
""";
            for (BillingEntry e : filteredEntries) {
                prompt += e.getCategory() + "," + e.getProduct() + "," + e.getPrice() + "," + e.getDate() + "\n";
            }
            prompt += "Please answer in English.\n";
            System.out.println("[AI分析Prompt] " + prompt);
            return filterMarkdown(filterThinkTags2(chat(prompt)));
        } catch (Exception e) {
            return "Failure of consumption pattern analysis" + e.getMessage();
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
            String prompt = """
请将以下消费描述结构化为账单条目，只输出JSON对象，字段包括：category, product, price, date（yyyy-MM-dd），time（HH:mm），remark。
要求：
1. 先判断该描述是"收入"还是"支出"。
2. 如果是"支出"，category字段请严格从以下类别中选择：餐饮、购物、交通、娱乐、医疗、通讯、教育、住宿、其他。
3. 如果是"收入"，category字段请严格从以下类别中选择：工资、奖金、报销、转账、理财收益、其他收入。
4. product字段请尽量提取具体的商品、服务或收入来源。
5. 不要使用"item""thing"等泛泛的词。
6. price字段只输出数字，不要带"元"或其他单位。
7. date字段请用yyyy-MM-dd格式，如果描述中是"今天"请自动替换为当天日期。如果没有日期则请自动替换为当天日期。
8. time字段如果没有具体时间请留空或用当前时间。
9. 字段顺序和名称必须严格为：category, product, price, date, time, remark。
10. 请根据语义判断，不要仅凭关键词。例如"工资到账""收到转账""报销到账""理财收益"都属于收入，"买咖啡""看电影""买衣服""打车"都属于支出。
消费描述：""" + record;
            prompt += "Please answer in English.\n";
            String aiResult = chat(prompt);
            // 2. 解析AI返回的JSON
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            
            // 1. 提取第一个合法JSON对象
            Pattern pattern = Pattern.compile("\\{[\\s\\S]*?\\}");
            Matcher matcher = pattern.matcher(aiResult);
            if (matcher.find()) {
                aiResult = matcher.group();
            } else {
                throw new RuntimeException("No legal JSON object was found");
            }
            
            // 2. 解析为Map
            Map<String, Object> map = mapper.readValue(aiResult, Map.class);
            
            // 3. 金额字段清洗
            String priceStr = map.getOrDefault("price", "0").toString().replaceAll("[^\\d.]", "");
            BigDecimal price = new BigDecimal(priceStr.isEmpty() ? "0" : priceStr);
            
            // 4. 日期、时间兜底
            String dateStr = (String) map.get("date");
            LocalDate date;
            if (dateStr == null || dateStr.trim().isEmpty() || "今天".equals(dateStr.trim()) || "today".equalsIgnoreCase(dateStr.trim())) {
                date = LocalDate.now();
            } else {
                try {
                    date = LocalDate.parse(dateStr.trim());
                } catch (Exception e) {
                    date = LocalDate.now();
                }
            }
            
            String timeStr = (String) map.getOrDefault("time", "");
            LocalTime time = timeStr.isEmpty() ? LocalTime.now() : LocalTime.parse(timeStr);
            
            BillingEntry entry = new BillingEntry();
            entry.setEntryId(UUID.randomUUID().toString().substring(0, 8));
            entry.setCategory((String) map.getOrDefault("category", "其他"));
            String product = (String) map.get("product");
            if (product == null || product.trim().isEmpty() || product.matches("^[\\d.]+元?$")) {
                product = (String) map.getOrDefault("category", record);
            }
            entry.setProduct(product);
            entry.setPrice(price);
            entry.setDate(date);
            entry.setTime(time);
            entry.setFormattedTime(time.toString());
            entry.setRemark((String) map.getOrDefault("remark", record));
            
            // 3. 写入三种格式的账单文件
            saveToCSV(entry);
            saveToJSON(entry);
            saveToTXT(entry);
            
            return String.format("Automatically classified and recorded: Category:%s，Product: %s, Amount: %s yuan, Date: %s. ",
                    entry.getCategory(), entry.getProduct(), entry.getPrice(), entry.getDate());
        } catch (Exception e) {
            e.printStackTrace();
            return "Consumption record entry failed" + e.getMessage();
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
                return "No upcoming cyclical transactions were detected.";
            }

            // 按距离时间排序
            periodicTransactions.sort(Comparator.comparing(PeriodicTransaction::getDaysUntilNext));

            // 构造表格输出
            StringBuilder result = new StringBuilder();
            result.append("Based on your historical transaction records, the following are the periodic transactions expected to occur within the next 30 days：\n\n");
            result.append("| Category | Product | Amount | Cycle (days) | Estimated date | Remaining days | \n");
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

            result.append("\nNote: Predictions are derived from historical transaction trends and should be considered indicative only.");
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Errors when analyzing cyclical transactions:" + e.getMessage();
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