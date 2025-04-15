package com.example.ccqbackend.controller;

import com.example.ccqbackend.model.BillingEntry;
import com.example.ccqbackend.service.CSVService;
import com.example.ccqbackend.service.FileService;
import com.example.ccqbackend.service.JSONService;
import com.example.ccqbackend.service.JwtConfig;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api.purseai.com/v1/billing")
public class BillingController {

    private final FileService fileService;
    private final CSVService csvService;
    private final JSONService jsonService;
    private final JwtConfig jwtConfig;  // 注入 JwtConfig

    @Autowired
    public BillingController(FileService fileService, CSVService csvService, JSONService jsonService, JwtConfig jwtConfig) {
        this.fileService = fileService;
        this.csvService = csvService;
        this.jsonService = jsonService;
        this.jwtConfig = jwtConfig;
    }

    @PostMapping("/entries")
    public BillingEntry createBillingEntry(@RequestHeader("Authorization") String token, @RequestBody BillingEntry entry) throws IOException {
        // 验证 token
        if (!isValidToken(token)) {
            throw new RuntimeException("无效的 token");
        }

        // 使用不同的格式保存数据
        fileService.writeDataToTXT(List.of(entry.toString()), "billingEntries.txt");

        // 创建CSV数据并写入文件
        String[] entryData = { entry.getCategory(), entry.getProduct(), String.valueOf(entry.getPrice()),entry.getDate().toString(), entry.getRemark() };
        List<String[]> entryDataList = new ArrayList<>();
        entryDataList.add(entryData);
        csvService.writeDataToCSV(entryDataList, "billingEntries.csv");

        // 创建JSON格式
        jsonService.writeDataToJSON(List.of(entry), "billingEntries.json");

        return entry;
    }

    // 验证 token 是否有效
    private boolean isValidToken(String token) {
        try {
            String actualToken = token.replace("Bearer ", "");  // 删除 Bearer 前缀
            Key secretKey = jwtConfig.getJwtSecretKey();  // 直接使用 Key 类型的密钥
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(actualToken); // 验证 token
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
