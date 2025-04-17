package com.example.ccqbackend.controller;

import com.example.ccqbackend.model.BillingEntry;
import com.example.ccqbackend.service.CSVService;
import com.example.ccqbackend.service.FileService;
import com.example.ccqbackend.service.JSONService;
import com.example.ccqbackend.service.JwtConfig;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/billing")
public class BillingController {

    private final FileService fileService;
    private final CSVService csvService;
    private final JSONService jsonService;
    private final JwtConfig jwtConfig;  // 注入 JwtConfig
    private static final String DATA_DIR = "data/billing";
    private static final Logger logger = Logger.getLogger(BillingController.class.getName());

    @Autowired
    public BillingController(FileService fileService, CSVService csvService, JSONService jsonService, JwtConfig jwtConfig) {
        this.fileService = fileService;
        this.csvService = csvService;
        this.jsonService = jsonService;
        this.jwtConfig = jwtConfig;
        
        // 确保数据目录存在
        File directory = new File(DATA_DIR);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            logger.info("Creating directory " + DATA_DIR + ": " + (created ? "success" : "failed"));
            // 尝试写入测试文件
            try {
                File testFile = new File(DATA_DIR, "test.txt");
                if (testFile.createNewFile()) {
                    logger.info("Successfully created test file at " + testFile.getAbsolutePath());
                }
            } catch (IOException e) {
                logger.severe("Failed to create test file: " + e.getMessage());
            }
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
    public ResponseEntity<?> createBillingEntry(@RequestHeader(value="Authorization", required=false) String token, 
                                               @RequestBody(required=false) BillingEntry entry) {
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
            if (token == null || token.isEmpty()) {
                logger.severe("Token为空");
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "认证令牌不能为空");
                return ResponseEntity.status(401).body(response);
            }
            
        if (!isValidToken(token)) {
                logger.severe("Token无效: " + token);
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "无效的认证令牌");
                return ResponseEntity.status(401).body(response);
            }

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

            // 文件写入部分使用追加模式
            try {
                // 确保目录存在
                File directory = new File(DATA_DIR);
                if (!directory.exists()) {
                    boolean created = directory.mkdirs();
                    logger.info("创建目录: " + (created ? "成功" : "失败"));
                }
                
                // CSV文件路径
                String csvFile = DATA_DIR + File.separator + "billingEntries.csv";
                
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
                
                // 同样处理JSON文件 (用于API读取)
                String jsonFile = DATA_DIR + File.separator + "billingEntries.json";
                List<BillingEntry> allEntries = readExistingEntries(jsonFile);
                allEntries.add(entry); // 添加新记录
                
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                mapper.writerWithDefaultPrettyPrinter().writeValue(new File(jsonFile), allEntries);
                logger.info("成功写入JSON文件: " + jsonFile);
                
            } catch (Exception e) {
                logger.severe("文件写入失败: " + e.getMessage());
                e.printStackTrace();
            }
            
            // 返回成功响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "账单记录创建成功");
            response.put("data", entry);
            logger.info("账单记录创建成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.severe("创建账单记录时发生错误: " + e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建账单记录失败: " + e.getMessage());
            response.put("errorType", e.getClass().getName());
            response.put("stackTrace", e.getStackTrace()[0].toString());
            return ResponseEntity.status(500).body(response);
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

    // 验证 token 是否有效
    private boolean isValidToken(String token) {
        try {
            if (token == null || token.isEmpty()) {
                logger.severe("Token is null or empty");
                return false;
            }
            
            logger.info("原始token: [" + token + "]");
            
            // 检查token是否以Bearer开头
            if (!token.startsWith("Bearer ")) {
                logger.severe("Token does not start with 'Bearer ' prefix: " + token);
                // 尝试不带前缀直接验证
                try {
                    Key secretKey = jwtConfig.getJwtSecretKey();
                    Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
                    logger.info("Token验证成功（不带Bearer前缀）");
                    return true;
                } catch (Exception e) {
                    logger.severe("Token验证失败（不带Bearer前缀）: " + e.getMessage());
                    return false;
                }
            }
            
            String actualToken = token.replace("Bearer ", "");  // 删除 Bearer 前缀
            logger.info("去除Bearer前缀后的token: [" + actualToken + "]");
            
            Key secretKey = jwtConfig.getJwtSecretKey();
            logger.info("使用的密钥: " + secretKey.getAlgorithm());
            
            // 验证token
            io.jsonwebtoken.Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(actualToken)
                .getBody();
                
            logger.info("Token验证成功，包含的声明: " + claims);
            return true;
        } catch (Exception e) {
            logger.severe("Token验证失败，详细原因: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 删除指定ID的账单条目
     */
    @DeleteMapping("/{entryId}")
    public ResponseEntity<?> deleteBillingEntry(@PathVariable String entryId) {
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
                    BigDecimal price = new BigDecimal(values[2].trim());
                    LocalDate date = LocalDate.parse(values[3].trim());
                    String remark = values.length > 4 ? values[4].trim() : product;
                    
                    // 创建账单记录
                    BillingEntry entry = new BillingEntry();
                    entry.setCategory(category);
                    entry.setProduct(product); 
                    entry.setPrice(price);
                    entry.setDate(date);
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
        } catch (IOException e) {
            logger.severe("处理CSV文件时出错: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("处理CSV文件时出错: " + e.getMessage());
        }
    }

    @GetMapping("/entries")
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
