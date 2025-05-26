package com.example.software.controller;

import com.example.software.api.BillingEntry;
import com.example.software.security.JwtUtil;
import com.example.software.service.CSVService;
import com.example.software.service.FileService;
import com.example.software.service.JSONService;
import com.example.software.service.XMLService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/v1/billing")
public class BillingController {

    private static final Logger logger = Logger.getLogger(BillingController.class.getName());
    private static final String DATA_DIR = "data" + File.separator + "billing";
    private static final String JWT_SECRET = "8119647086214365923147805231478052314780523147805231478052314780523147805231478052314780";

    @Autowired
    private FileService fileService;

    @Autowired
    private CSVService csvService;

    @Autowired
    private JSONService jsonService;

    @Autowired
    private XMLService xmlService;

    public BillingController() {
        // 确保数据目录存在
        File directory = new File(DATA_DIR);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            logger.info("Creating directory " + DATA_DIR + ": " + (created ? "success" : "failed"));
        }
        logger.info("BillingController initialized with data directory: " + DATA_DIR);
        
        // 尝试创建测试文件
        try {
            File testFile = new File(DATA_DIR + File.separator + "test.txt");
            boolean created = testFile.createNewFile();
            logger.info("Test file creation: " + (created ? "success" : "file already exists"));
            if (created) {
                FileWriter writer = new FileWriter(testFile);
                writer.write("Test content: " + System.currentTimeMillis());
                writer.close();
                logger.info("Successfully wrote to test file");
            }
        } catch (Exception e) {
            logger.severe("Failed to create test file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 调试接口 - 用于记录原始请求数据
    @PostMapping("/debug")
    public ResponseEntity<?> debugRequest(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> rawData) {
        logger.info("Debug request received");
        logger.info("Token: " + token);
        logger.info("Raw data: " + rawData);

        // 返回接收到的数据
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Debug request received");
        response.put("receivedData", rawData);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/entries")
    public ResponseEntity<?> createBillingEntry(@RequestHeader("Authorization") String token, @RequestBody BillingEntry entry) {
        logger.info("Received createBillingEntry request");
        logger.info("Headers: Authorization=" + (token != null ? token : "null"));

        try {
            // 记录整个请求体
            try {
                logger.info("收到请求体: " + (entry != null ? entry.toString() : "null"));
            } catch (Exception e) {
                logger.severe("无法打印请求体: " + e.getMessage());
            }
            // 验证请求
            if (entry == null) {
                logger.severe("请求体为空");
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "请求体不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            logger.info("接收到的数据:");
            logger.info("  category: " + entry.getCategory());
            logger.info("  product: " + entry.getProduct());
            logger.info("  price: " + entry.getPrice());
            logger.info("  date: " + entry.getDate());
            logger.info("  time: " + entry.getTime());
            logger.info("  remark: " + entry.getRemark());

            // 验证 token
            if (!isValidToken(token)) {
                logger.warning("Invalid token: " + token);
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Invalid token");
                return ResponseEntity.status(401).body(response);
            }

            logger.info("Token validated successfully");

            // 验证和设置默认值
            if (entry.getCategory() == null || entry.getCategory().isEmpty()) {
                entry.setCategory("其他"); // 使用默认类别
                logger.info("使用默认类别: 其他");
            }

            if (entry.getProduct() == null || entry.getProduct().isEmpty()) {
                entry.setProduct("未指定"); // 使用默认产品名
                logger.info("使用默认产品名: 未指定");
            }

            if (entry.getPrice() == null) {
                entry.setPrice(BigDecimal.ZERO);
                logger.info("使用默认价格: 0");
            }
            if (entry.getRemark() == null) {
                entry.setRemark(entry.getProduct()); // 使用产品名作为备注
                logger.info("使用产品名作为默认备注: " + entry.getProduct());
            }

            if (entry.getDate() == null) {
                entry.setDate(LocalDate.now());
                logger.info("使用当前日期作为默认日期: " + entry.getDate());
            }

            if (entry.getTime() == null) {
                entry.setTime(LocalTime.now());
                logger.info("使用当前时间作为默认时间: " + entry.getTime());
            }
            // 如果没有设置entryDateTime，则从date和time字段构建
//            if (entry.getEntryDateTime() == null && entry.getDate() != null) {
//                LocalTime timeValue = entry.getTime() != null ? entry.getTime() : LocalTime.now();
//                entry.setEntryDateTime(LocalDateTime.of(entry.getDate(), timeValue));
//                logger.info("Set entryDateTime from date and time: " + entry.getEntryDateTime());
//            } else if (entry.getEntryDateTime() == null) {
//                // 设置创建时间
//                entry.setEntryDateTime(LocalDateTime.now());
//                logger.info("Set entryDateTime to now: " + entry.getEntryDateTime());
//            }
            
            // 确保date和time与entryDateTime同步
            if (entry.getDate() == null) {
                entry.setDate(LocalDate.now());
            }
            if (entry.getTime() == null) {
                entry.setTime(LocalDateTime.now().toLocalTime());
            }

            // 确保目录存在
            File directory = new File(DATA_DIR);
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                logger.info("Creating directory " + DATA_DIR + ": " + (created ? "success" : "failed"));
            }

            // 使用不同的格式保存数据
            String txtFile = DATA_DIR + File.separator + "billingEntries.txt";
            String csvFile = DATA_DIR + File.separator + "billingEntries.csv";
            String jsonFile = DATA_DIR + File.separator + "billingEntries.json";

            logger.info("Writing to TXT file: " + txtFile);
            fileService.writeDataToTXT(List.of(entry.toString()), txtFile);

            // 使用追加模式写入CSV
            BufferedWriter writer;
            if (!new File(csvFile).exists()) {
                // 文件不存在，创建并写入表头
                writer = new BufferedWriter(new FileWriter(csvFile));
                writer.write("类别,产品,价格,日期,时间,备注\n");
            } else {
                // 文件存在，使用追加模式
                writer = new BufferedWriter(new FileWriter(csvFile, true));
            }

            // 写入数据行
            writer.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    entry.getCategory(),
                    entry.getProduct(),
                    entry.getPrice(),
                    entry.getDate(),
                    entry.getTime(),
                    entry.getRemark()));
            writer.close();
            logger.info("成功写入CSV文件: " + csvFile);

            // 创建JSON格式
            logger.info("Writing to JSON file: " + jsonFile);
//            jsonService.writeDataToJSON(List.of(entry), jsonFile);
            // 同样处理JSON文件 (用于API读取)
            List<BillingEntry> allEntries = readExistingEntries(jsonFile);
            allEntries.add(entry); // 添加新记录
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(jsonFile), allEntries);
            logger.info("成功写入JSON文件: " + jsonFile);

            // 返回成功响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Billing entry created successfully");
            response.put("data", entry);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("Error creating billing entry" + e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to create billing entry: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/entries")
    public ResponseEntity<?> getBillingEntries(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String searchTerm) {
        logger.info("Received getBillingEntries request");
        logger.info("Headers: Authorization=" + (token != null ? token : "null"));
        // 验证 token
        if (!isValidToken(token)) {
            logger.warning("Invalid token: " + token);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Invalid token");
            return ResponseEntity.status(401).body(response);
        }
        try {

            String jsonFile = DATA_DIR + File.separator + "billingEntries.json";
            File jsonFileObj = new File(jsonFile);

            if (!jsonFileObj.exists() || jsonFileObj.length() == 0) {
                // 如果JSON文件不存在，尝试从CSV读取
                List<BillingEntry> entries = readEntriesFromCSV();
                return ResponseEntity.ok(entries);
            }

            // 从JSON文件读取
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            List<BillingEntry> entries = mapper.readValue(jsonFileObj,
                    new TypeReference<List<BillingEntry>>() {});
            logger.info("成功读取 " + entries.size() + " 条账单记录");
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            logger.severe("获取账单记录失败: " + e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "获取账单失败: " + e.getMessage()));
        }
    }
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserDetailsService userDetailsService;
    // 验证 token 是否有效
    private boolean isValidToken(String token) {
        try {
            logger.info("开始验证token: " + token);
            String actualToken = token.replace("Bearer ", "");
            logger.info("去除Bearer前缀后的token: " + actualToken);
            
            // 使用固定密钥，与签发令牌时相同
            Key secretKey = Keys.hmacShaKeyFor(JWT_SECRET.getBytes());
           // Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(actualToken);

            String username = jwtUtil.extractUsername(actualToken);
            boolean res = jwtUtil.validateToken(actualToken,userDetailsService.loadUserByUsername(username));
            logger.info("Token验证成功");
            return res;
        } catch (Exception e) {
            logger.warning("Token验证失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除指定ID的账单条目
     */
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteBillingEntry(@RequestParam(required = true) String entryId) {
        logger.info("收到删除请求，entryId: " + entryId);

        try {
            // 解析entryId，格式可能为: date_category_product
            // 最多分割成3部分，避免产品名中可能包含下划线导致过度分割
            String[] parts = entryId.split("_", 3);
            logger.info("分割后的entryId部分: " + String.join(", ", parts));

            if (parts.length < 2) {
                logger.warning("无效的entryId格式: " + entryId);
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "无效的entryId格式"));
            }

            // 确保数据目录存在
            File dataDir = new File(DATA_DIR);
            if (!dataDir.exists()) {
                dataDir.mkdirs();
                logger.info("创建数据目录: " + dataDir.getAbsolutePath());
            }

            // 检查相关文件
            File jsonFile = new File(DATA_DIR + "/billingEntries.json");
            File txtFile = new File(DATA_DIR + "/billingEntries.txt");
            File csvFile = new File(DATA_DIR + "/billingEntries.csv");

            logger.info("JSON文件存在: " + jsonFile.exists() + ", 路径: " + jsonFile.getAbsolutePath());
            logger.info("TXT文件存在: " + txtFile.exists() + ", 路径: " + txtFile.getAbsolutePath());
            logger.info("CSV文件存在: " + csvFile.exists() + ", 路径: " + csvFile.getAbsolutePath());

            List<BillingEntry> entries = new ArrayList<>();

            // 尝试从JSON文件读取现有数据
            if (jsonFile.exists()) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.registerModule(new JavaTimeModule());
                    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                    entries = mapper.readValue(jsonFile, new TypeReference<List<BillingEntry>>() {});
                    logger.info("从JSON文件读取了 " + entries.size() + " 条记录");
                } catch (Exception e) {
                    logger.warning("读取JSON文件失败: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // 如果JSON文件为空，尝试从CSV文件读取
            if (entries.isEmpty() && csvFile.exists()) {
                try {
                    List<BillingEntry> csvEntries = new ArrayList<>();
                    BufferedReader reader = new BufferedReader(new FileReader(csvFile));
                    String line;
                    // 跳过CSV头行
                    reader.readLine();

                    while ((line = reader.readLine()) != null) {
                        String[] values = line.split(",");
                        if (values.length >= 5) {
                            BillingEntry entry = new BillingEntry();
                            entry.setCategory(values[0].trim());
                            entry.setProduct(values[1].trim());

                            // 处理价格
                            try {
                                entry.setPrice(new BigDecimal(values[2].trim()));
                            } catch (NumberFormatException e) {
                                entry.setPrice(BigDecimal.ZERO);
                            }

                            // 处理日期时间
                            try {
                                String dateStr = values[3].trim();
                                if (!dateStr.isEmpty()) {
                                    entry.setDate(LocalDate.parse(dateStr));
                                }

                                if (values.length > 4 && !values[4].trim().isEmpty()) {
                                    entry.setTime(LocalTime.parse(values[4].trim()));
                                }

                                // 设置备注
                                if (values.length > 5) {
                                    entry.setRemark(values[5].trim());
                                }
                            } catch (Exception e) {
                                logger.warning("解析CSV行失败: " + line + ", 错误: " + e.getMessage());
                            }
                            csvEntries.add(entry);
                        }
                    }
                    reader.close();
                    entries = csvEntries;
                    logger.info("从CSV文件读取了 " + entries.size() + " 条记录");
                } catch (Exception e) {
                    logger.warning("读取CSV文件失败: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            if (entries.isEmpty()) {
                logger.warning("没有找到任何账单记录");
                return ResponseEntity.ok(Map.of("success", false, "message", "没有找到任何账单记录"));
            }

            // 打印所有记录的关键信息，用于调试
            logger.info("当前所有记录信息:");
            for (int i = 0; i < entries.size(); i++) {
                BillingEntry entry = entries.get(i);
                logger.info(String.format("记录[%d]: 日期=%s, 类别=%s, 产品=%s",
                        i,
                        entry.getDate() != null ? entry.getDate().toString() : "null",
                        entry.getCategory() != null ? entry.getCategory() : "null",
                        entry.getProduct() != null ? entry.getProduct() : "null"));
            }

            // 查找并删除匹配的记录
            int initialSize = entries.size();
            List<BillingEntry> entriesToRemove = new ArrayList<>();

            for (BillingEntry entry : entries) {
                String date = entry.getDate() != null ? entry.getDate().toString() : "";
                String category = entry.getCategory() != null ? entry.getCategory() : "";
                String product = entry.getProduct() != null ? entry.getProduct() : "";

                logger.info("检查记录: date=" + date + ", category=" + category + ", product=" + product);
                logger.info("比较条件: parts[0]=" + parts[0] +
                        (parts.length >= 2 ? ", parts[1]=" + parts[1] : "") +
                        (parts.length >= 3 ? ", parts[2]=" + parts[2] : ""));

                // 判断是否匹配
                boolean dateMatch = parts[0].equals(date);
                boolean categoryMatch = parts.length < 2 || parts[1].equals(category);
                boolean productMatch = parts.length < 3 || parts[2].equals(product);

                logger.info("匹配结果: dateMatch=" + dateMatch +
                        ", categoryMatch=" + categoryMatch +
                        ", productMatch=" + productMatch);

                if (dateMatch && categoryMatch && productMatch) {
                    logger.info("找到匹配的记录，将被删除: " + date + "_" + category + "_" + product);
                    entriesToRemove.add(entry);
                }
            }

            if (entriesToRemove.isEmpty()) {
                logger.warning("未找到匹配的记录: " + entryId);
                return ResponseEntity.ok(Map.of("success", false, "message", "未找到匹配的记录"));
            }

            logger.info("找到 " + entriesToRemove.size() + " 条要删除的记录");
            entries.removeAll(entriesToRemove);
            logger.info("删除后剩余记录数: " + entries.size());

            // 更新JSON文件
            boolean jsonSuccess = false;
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, entries);
                jsonSuccess = true;
                logger.info("成功更新JSON文件");
            } catch (Exception e) {
                logger.warning("更新JSON文件失败: " + e.getMessage());
                e.printStackTrace();
            }

            // 更新TXT文件
            boolean txtSuccess = false;
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(txtFile))) {
                for (BillingEntry entry : entries) {
                    writer.write(String.format("%s,%s,%s,%s,%s,%s\n",
                            entry.getCategory() != null ? entry.getCategory() : "",
                            entry.getProduct() != null ? entry.getProduct() : "",
                            entry.getPrice() != null ? entry.getPrice().toString() : "0",
                            entry.getDate() != null ? entry.getDate().toString() : "",
                            entry.getTime() != null ? entry.getTime().toString() : "",
                            entry.getRemark() != null ? entry.getRemark() : ""));
                }
                txtSuccess = true;
                logger.info("成功更新TXT文件");
            } catch (Exception e) {
                logger.warning("更新TXT文件失败: " + e.getMessage());
                e.printStackTrace();
            }

            // 更新CSV文件
            boolean csvSuccess = false;
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile))) {
                // 写入CSV头
                writer.write("类别,产品,价格,日期,时间,备注\n");

                // 写入数据行
                for (BillingEntry entry : entries) {
                    writer.write(String.format("%s,%s,%s,%s,%s,%s\n",
                            entry.getCategory() != null ? entry.getCategory() : "",
                            entry.getProduct() != null ? entry.getProduct() : "",
                            entry.getPrice() != null ? entry.getPrice().toString() : "0",
                            entry.getDate() != null ? entry.getDate().toString() : "",
                            entry.getTime() != null ? entry.getTime().toString() : "",
                            entry.getRemark() != null ? entry.getRemark() : ""));
                }
                csvSuccess = true;
                logger.info("成功更新CSV文件");
            } catch (Exception e) {
                logger.warning("更新CSV文件失败: " + e.getMessage());
                e.printStackTrace();
            }

            if (jsonSuccess && txtSuccess && csvSuccess) {
                logger.info("成功删除记录并更新所有文件");
                return ResponseEntity.ok(Map.of("success", true, "message", "成功删除记录"));
            } else {
                logger.warning("部分文件更新失败: JSON=" + jsonSuccess + ", TXT=" + txtSuccess + ", CSV=" + csvSuccess);
                return ResponseEntity.ok(Map.of("success", true, "message", "记录已删除，但部分文件可能未更新"));
            }
        } catch (Exception e) {
            logger.severe("删除记录时发生异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "删除记录时发生异常: " + e.getMessage()));
        }
    }

    @PostMapping("/import/csv")
    public ResponseEntity<?> importCsv(@RequestParam("file") MultipartFile file,
                                       @RequestHeader("Authorization") String authHeader) {
        // 验证授权
        if (!isValidToken(authHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("未授权访问");
        }

        try {
            if (file.isEmpty() || !file.getOriginalFilename().endsWith(".csv")) {
                return ResponseEntity.badRequest().body("请提供有效的CSV文件");
            }

            // 处理CSV文件
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)
            );

            String line;
            int imported = 0;
            int skipped = 0;

            // 跳过头行
            reader.readLine();

            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length < 5) {
                    skipped++;
                    continue;
                }

                try {
                    String category = values[0].trim();
                    String product = values[1].trim();
                    String dateStr = values[2].trim();
                    String timeStr = values[3].trim();
                    BigDecimal price = new BigDecimal(values[4].trim());
                    String remark = values.length > 5 ? values[5].trim() : product;

                    // 解析日期 - 支持多种格式
                    LocalDate date;
                    if (dateStr.contains("/")) {
                        String[] dateParts = dateStr.split("/");
                        int year = Integer.parseInt(dateParts[0]);
                        int month = Integer.parseInt(dateParts[1]);
                        int day = Integer.parseInt(dateParts[2]);
                        date = LocalDate.of(year, month, day);
                    } else {
                        date = LocalDate.parse(dateStr);
                    }

                    // 解析时间 - 支持多种格式
                    LocalTime time;
                    if (timeStr.contains(":")) {
                        time = LocalTime.parse(timeStr);
                    } else {
                        // 处理纯数字格式 (例如: "0915" -> "09:15")
                        String formattedTime = timeStr.length() == 4 ? 
                            timeStr.substring(0, 2) + ":" + timeStr.substring(2) : 
                            "00:00";
                        time = LocalTime.parse(formattedTime);
                    }

                    // 创建账单记录
                    BillingEntry entry = new BillingEntry();
                    entry.setCategory(category);
                    entry.setProduct(product);
                    entry.setPrice(price);
                    entry.setDate(date);
                    entry.setTime(time);
                    entry.setRemark(remark);

                    // 直接调用createBillingEntry方法保存记录
                    ResponseEntity<?> result = createBillingEntry(authHeader, entry);
                    if (result.getStatusCode().is2xxSuccessful()) {
                        imported++;
                        logger.info("成功导入记录: " + entry.getCategory() + "-" + entry.getProduct());
                    } else {
                        skipped++;
                        logger.warning("导入记录失败: " + line);
                    }
                } catch (Exception e) {
                    logger.warning("处理CSV行时出错: " + e.getMessage());
                    skipped++;
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("entriesImported", imported);
            response.put("entriesSkipped", skipped);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("处理CSV文件时出错: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("处理CSV文件时出错: " + e.getMessage());
        }
    }

    @GetMapping("/getAllBillingEntries")
    public ResponseEntity<?> getAllBillingEntries() {
        try {
            String jsonFile = DATA_DIR + File.separator + "billingEntries.json";
            File jsonFileObj = new File(jsonFile);

            if (!jsonFileObj.exists() || jsonFileObj.length() == 0) {
                // 如果JSON文件不存在，尝试从CSV读取
                List<BillingEntry> entries = readEntriesFromCSV();
                return ResponseEntity.ok(entries);
            }

            // 从JSON文件读取
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            List<BillingEntry> entries = mapper.readValue(jsonFileObj,
                    new TypeReference<List<BillingEntry>>() {});

            logger.info("成功读取 " + entries.size() + " 条账单记录");
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            logger.severe("获取账单记录失败: " + e.getMessage());
            return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", "获取账单失败: " + e.getMessage()));
        }
    }

    @PostMapping("/update")
    public ResponseEntity<?> update(@RequestBody Map<String, String> body) {
        String entryId = body.get("entryId");
        entryId = URLDecoder.decode(entryId, StandardCharsets.UTF_8);
        logger.info("收到更新请求，entryId: " + body.get("entryId"));

        try {
            // 解析entryId，格式可能为: date_category_product
            // 最多分割成3部分，避免产品名中可能包含下划线导致过度分割
            String[] parts = entryId.split("_", 3);
            logger.info("分割后的entryId部分: " + String.join(", ", parts));

            if (parts.length < 2) {
                logger.warning("无效的entryId格式: " + entryId);
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "无效的entryId格式"));
            }

            // 确保数据目录存在
            File dataDir = new File(DATA_DIR);
            if (!dataDir.exists()) {
                dataDir.mkdirs();
                logger.info("创建数据目录: " + dataDir.getAbsolutePath());
            }

            // 检查相关文件
            File jsonFile = new File(DATA_DIR + "/billingEntries.json");
            File txtFile = new File(DATA_DIR + "/billingEntries.txt");
            File csvFile = new File(DATA_DIR + "/billingEntries.csv");

            logger.info("JSON文件存在: " + jsonFile.exists() + ", 路径: " + jsonFile.getAbsolutePath());
            logger.info("TXT文件存在: " + txtFile.exists() + ", 路径: " + txtFile.getAbsolutePath());
            logger.info("CSV文件存在: " + csvFile.exists() + ", 路径: " + csvFile.getAbsolutePath());

            List<BillingEntry> entries = new ArrayList<>();

            // 尝试从JSON文件读取现有数据
            if (jsonFile.exists()) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.registerModule(new JavaTimeModule());
                    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                    entries = mapper.readValue(jsonFile, new TypeReference<List<BillingEntry>>() {});
                    logger.info("从JSON文件读取了 " + entries.size() + " 条记录");
                } catch (Exception e) {
                    logger.warning("读取JSON文件失败: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // 如果JSON文件为空，尝试从CSV文件读取
            if (entries.isEmpty() && csvFile.exists()) {
                try {
                    List<BillingEntry> csvEntries = new ArrayList<>();
                    BufferedReader reader = new BufferedReader(new FileReader(csvFile));
                    String line;
                    // 跳过CSV头行
                    reader.readLine();

                    while ((line = reader.readLine()) != null) {
                        String[] values = line.split(",");
                        if (values.length >= 5) {
                            BillingEntry entry = new BillingEntry();
                            entry.setCategory(values[0].trim());
                            entry.setProduct(values[1].trim());

                            // 处理价格
                            try {
                                entry.setPrice(new BigDecimal(values[2].trim()));
                            } catch (NumberFormatException e) {
                                entry.setPrice(BigDecimal.ZERO);
                            }

                            // 处理日期时间
                            try {
                                String dateStr = values[3].trim();
                                if (!dateStr.isEmpty()) {
                                    entry.setDate(LocalDate.parse(dateStr));
                                }

                                if (values.length > 4 && !values[4].trim().isEmpty()) {
                                    entry.setTime(LocalTime.parse(values[4].trim()));
                                }

                                // 设置备注
                                if (values.length > 5) {
                                    entry.setRemark(values[5].trim());
                                }
                            } catch (Exception e) {
                                logger.warning("解析CSV行失败: " + line + ", 错误: " + e.getMessage());
                            }
                            csvEntries.add(entry);
                        }
                    }
                    reader.close();
                    entries = csvEntries;
                    logger.info("从CSV文件读取了 " + entries.size() + " 条记录");
                } catch (Exception e) {
                    logger.warning("读取CSV文件失败: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            if (entries.isEmpty()) {
                logger.warning("没有找到任何账单记录");
                return ResponseEntity.ok(Map.of("success", false, "message", "没有找到任何账单记录"));
            }

            // 打印所有记录的关键信息，用于调试
            logger.info("当前所有记录信息:");
            for (int i = 0; i < entries.size(); i++) {
                BillingEntry entry = entries.get(i);
                logger.info(String.format("记录[%d]: 日期=%s, 类别=%s, 产品=%s",
                        i,
                        entry.getDate() != null ? entry.getDate().toString() : "null",
                        entry.getCategory() != null ? entry.getCategory() : "null",
                        entry.getProduct() != null ? entry.getProduct() : "null"));
            }

            // 查找并删除匹配的记录
            int initialSize = entries.size();
            List<BillingEntry> entriesToUpdate = new ArrayList<>();

            for (BillingEntry entry : entries) {
                String date = entry.getDate() != null ? entry.getDate().toString() : "";
                String category = entry.getCategory() != null ? entry.getCategory() : "";
                String product = entry.getProduct() != null ? entry.getProduct() : "";

                logger.info("检查记录: date=" + date + ", category=" + category + ", product=" + product);
                logger.info("比较条件: parts[0]=" + parts[0] +
                        (parts.length >= 2 ? ", parts[1]=" + parts[1] : "") +
                        (parts.length >= 3 ? ", parts[2]=" + parts[2] : ""));

                // 判断是否匹配
                boolean dateMatch = parts[0].equals(date);
                boolean categoryMatch = parts.length < 2 || parts[1].equals(category);
                boolean productMatch = parts.length < 3 || parts[2].equals(product);

                logger.info("匹配结果: dateMatch=" + dateMatch +
                        ", categoryMatch=" + categoryMatch +
                        ", productMatch=" + productMatch);

                if (dateMatch && categoryMatch && productMatch) {
                    logger.info("找到匹配的记录，将被更改: " + date + "_" + category + "_" + product);
                    entriesToUpdate.add(entry);
                }
            }

            if (entriesToUpdate.isEmpty()) {
                logger.warning("未找到匹配的记录: " + entryId);
                return ResponseEntity.ok(Map.of("success", false, "message", "未找到匹配的记录"));
            }

            logger.info("找到 " + entriesToUpdate.size() + " 条要更改的记录");
            String categoryUpdate = body.get("category");
            String productUpdate = body.get("product");
            String priceUpdate = body.get("price");
            String dateUpdate = body.get("date");
            String timeUpdate = body.get("time");
            String remarkUpdate = body.get("remark");
            for (BillingEntry entry : entriesToUpdate) {
                entry.setCategory(categoryUpdate);
                entry.setProduct(productUpdate);
                BigDecimal decimal = new BigDecimal(priceUpdate);
                entry.setPrice(decimal);
                entry.setDate(LocalDate.parse(dateUpdate));
                entry.setTime(LocalTime.parse(timeUpdate));
                entry.setRemark(remarkUpdate);
            }
            //entries.removeAll(entriesToUpdate);


            // 更新JSON文件
            boolean jsonSuccess = false;
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, entries);
                jsonSuccess = true;
                logger.info("成功更新JSON文件");
            } catch (Exception e) {
                logger.warning("更新JSON文件失败: " + e.getMessage());
                e.printStackTrace();
            }

            // 更新TXT文件
            boolean txtSuccess = false;
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(txtFile))) {
                for (BillingEntry entry : entries) {
                    writer.write(String.format("%s,%s,%s,%s,%s,%s\n",
                            entry.getCategory() != null ? entry.getCategory() : "",
                            entry.getProduct() != null ? entry.getProduct() : "",
                            entry.getPrice() != null ? entry.getPrice().toString() : "0",
                            entry.getDate() != null ? entry.getDate().toString() : "",
                            entry.getTime() != null ? entry.getTime().toString() : "",
                            entry.getRemark() != null ? entry.getRemark() : ""));
                }
                txtSuccess = true;
                logger.info("成功更新TXT文件");
            } catch (Exception e) {
                logger.warning("更新TXT文件失败: " + e.getMessage());
                e.printStackTrace();
            }

            // 更新CSV文件
            boolean csvSuccess = false;
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile))) {
                // 写入CSV头
                writer.write("类别,产品,价格,日期,时间,备注\n");

                // 写入数据行
                for (BillingEntry entry : entries) {
                    writer.write(String.format("%s,%s,%s,%s,%s,%s\n",
                            entry.getCategory() != null ? entry.getCategory() : "",
                            entry.getProduct() != null ? entry.getProduct() : "",
                            entry.getPrice() != null ? entry.getPrice().toString() : "0",
                            entry.getDate() != null ? entry.getDate().toString() : "",
                            entry.getTime() != null ? entry.getTime().toString() : "",
                            entry.getRemark() != null ? entry.getRemark() : ""));
                }
                csvSuccess = true;
                logger.info("成功更新CSV文件");
            } catch (Exception e) {
                logger.warning("更新CSV文件失败: " + e.getMessage());
                e.printStackTrace();
            }

            if (jsonSuccess && txtSuccess && csvSuccess) {
                logger.info("成功更新记录并更新所有文件");
                return ResponseEntity.ok(Map.of("success", true, "message", "成功更新记录"));
            } else {
                logger.warning("部分文件更新失败: JSON=" + jsonSuccess + ", TXT=" + txtSuccess + ", CSV=" + csvSuccess);
                return ResponseEntity.ok(Map.of("success", true, "message", "记录已更新，但部分文件可能未更新"));
            }
        } catch (Exception e) {
            logger.severe("更新记录时发生异常: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "更新记录时发生异常: " + e.getMessage()));
        }
    }
    // 辅助方法：读取现有JSON数据
    private List<BillingEntry> readExistingEntries(String jsonFilePath) {
        List<BillingEntry> entries = new ArrayList<>();
        File jsonFile = new File(jsonFilePath);

        if (jsonFile.exists() && jsonFile.length() > 0) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                entries = mapper.readValue(jsonFile, new TypeReference<List<BillingEntry>>() {});
                logger.info("读取到 " + entries.size() + " 条现有记录");
            } catch (Exception e) {
                logger.warning("读取JSON文件失败: " + e.getMessage());
            }
        }

        return entries;
    }

    // 从CSV读取账单记录
    private List<BillingEntry> readEntriesFromCSV() {
        List<BillingEntry> entries = new ArrayList<>();
        String csvFile = DATA_DIR + File.separator + "billingEntries.csv";
        File csvFileObj = new File(csvFile);

        if (!csvFileObj.exists()) {
            return entries;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFileObj))) {
            // 跳过头行
            reader.readLine();
            String line;

            while ((line = reader.readLine()) != null) {
                try {
                    // 解析CSV行
                    String[] values = line.split(",");
                    if (values.length >= 5) {
                        BillingEntry entry = new BillingEntry();

                        // 清理引号
                        entry.setCategory(values[0].replace("\"", "").trim());
                        entry.setProduct(values[1].replace("\"", "").trim());

                        // 解析价格
                        try {
                            entry.setPrice(new BigDecimal(values[2].replace("\"", "").trim()));
                        } catch (Exception e) {
                            entry.setPrice(BigDecimal.ZERO);
                        }

                        // 解析日期
                        try {
                            entry.setDate(LocalDate.parse(values[3].replace("\"", "").trim()));
                        } catch (Exception e) {
                            entry.setDate(LocalDate.now());
                        }

                        // 解析时间
                        try {
                            entry.setTime(LocalTime.parse(values[4].replace("\"", "").trim()));
                        } catch (Exception e) {
                            entry.setTime(LocalTime.now());
                        }

                        // 备注
                        if (values.length > 5) {
                            entry.setRemark(values[5].replace("\"", "").trim());
                        } else {
                            entry.setRemark(entry.getProduct());
                        }

                        entries.add(entry);
                    }
                } catch (Exception e) {
                    logger.warning("解析CSV行失败: " + line);
                }
            }
        } catch (Exception e) {
            logger.warning("读取CSV文件失败: " + e.getMessage());
        }

        return entries;
    }
}
