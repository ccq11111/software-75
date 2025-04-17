package com.example.ccqbackend.controller;

import com.example.ccqbackend.model.BillingEntry;
import com.example.ccqbackend.service.CSVService;
import com.example.ccqbackend.service.FileService;
import com.example.ccqbackend.service.JSONService;
import com.example.ccqbackend.service.XMLService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/billing")
public class BillingController {

    private static final Logger logger = Logger.getLogger(BillingController.class.getName());
    private static final String DATA_DIR = "data" + File.separator + "billing";
    private static final String JWT_SECRET = "mySecretKey1234567890";

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

    @PostMapping("/entries")
    public ResponseEntity<?> createBillingEntry(@RequestHeader("Authorization") String token, @RequestBody BillingEntry entry) {
        logger.info("Received createBillingEntry request with token: " + token);
        logger.info("Received entry data: category=" + entry.getCategory() 
            + ", product=" + entry.getProduct() 
            + ", price=" + entry.getPrice()
            + ", date=" + entry.getDate()
            + ", time=" + entry.getTime()
            + ", remark=" + entry.getRemark());
            
        try {
            // 验证 token
            if (!isValidToken(token)) {
                logger.warning("Invalid token: " + token);
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Invalid token");
                return ResponseEntity.status(401).body(response);
            }

            logger.info("Token validated successfully");
            
            // 如果没有设置entryDateTime，则从date和time字段构建
            if (entry.getEntryDateTime() == null && entry.getDate() != null) {
                LocalTime timeValue = entry.getTime() != null ? entry.getTime() : LocalTime.now();
                entry.setEntryDateTime(LocalDateTime.of(entry.getDate(), timeValue));
                logger.info("Set entryDateTime from date and time: " + entry.getEntryDateTime());
            } else if (entry.getEntryDateTime() == null) {
                // 设置创建时间
                entry.setEntryDateTime(LocalDateTime.now());
                logger.info("Set entryDateTime to now: " + entry.getEntryDateTime());
            }
            
            // 确保date和time与entryDateTime同步
            if (entry.getDate() == null && entry.getEntryDateTime() != null) {
                entry.setDate(entry.getEntryDateTime().toLocalDate());
            }
            if (entry.getTime() == null && entry.getEntryDateTime() != null) {
                entry.setTime(entry.getEntryDateTime().toLocalTime());
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

            // 创建CSV数据并写入文件
            String[] entryData = { 
                entry.getCategory(), 
                entry.getProduct(), 
                entry.getPrice().toString(), 
                entry.getDate().toString(), 
                entry.getRemark() 
            };
            List<String[]> entryDataList = new ArrayList<>();
            entryDataList.add(entryData);
            
            logger.info("Writing to CSV file: " + csvFile);
            csvService.writeDataToCSV(entryDataList, csvFile);

            // 创建JSON格式
            logger.info("Writing to JSON file: " + jsonFile);
            jsonService.writeDataToJSON(List.of(entry), jsonFile);

            // 返回成功响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Billing entry created successfully");
            response.put("data", entry);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error creating billing entry", e);
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
        
        logger.info("Received getBillingEntries request with token: " + token);
        try {
            // 验证 token
            if (!isValidToken(token)) {
                logger.warning("Invalid token: " + token);
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Invalid token");
                return ResponseEntity.status(401).body(response);
            }
            
            logger.info("Token validated successfully");
            // 这里应该是从文件读取数据的逻辑
            // 为测试，我们返回一个空列表
            List<BillingEntry> entries = new ArrayList<>();
            
            // 返回成功响应
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Billing entries retrieved successfully");
            response.put("entries", entries);
            logger.info("Retrieved " + entries.size() + " billing entries");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.severe("Error retrieving billing entries: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to retrieve billing entries: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // 验证 token 是否有效
    private boolean isValidToken(String token) {
        try {
            logger.info("开始验证token: " + token);
            String actualToken = token.replace("Bearer ", "");
            logger.info("去除Bearer前缀后的token: " + actualToken);
            
            // 使用固定密钥，与签发令牌时相同
            Key secretKey = Keys.hmacShaKeyFor(JWT_SECRET.getBytes());
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(actualToken);
            logger.info("Token验证成功");
            return true;
        } catch (Exception e) {
            logger.warning("Token验证失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
