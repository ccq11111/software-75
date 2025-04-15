package com.example.ccqbackend.controller;

import com.example.ccqbackend.model.BillingEntry;
import com.example.ccqbackend.service.CSVService;
import com.example.ccqbackend.service.FileService;
import com.example.ccqbackend.service.JSONService;
import com.example.ccqbackend.service.XMLService;
import com.example.ccqbackend.model.BillingEntry;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api.purseai.com/v1/billing")
public class BillingController {

    @Autowired
    private FileService fileService;

    @Autowired
    private CSVService csvService;

    @Autowired
    private JSONService jsonService;

    @Autowired
    private XMLService xmlService;

    private static final String JWT_SECRET = "mySecretKey1234567890";  // 确保与生成token时的密钥一致

    @PostMapping("/entries")
    public BillingEntry createBillingEntry(@RequestHeader("Authorization") String token, @RequestBody BillingEntry entry) throws IOException, JAXBException {
        // 验证 token
        if (!isValidToken(token)) {
            throw new RuntimeException("无效的 token");
        }

        // 使用不同的格式保存数据
        fileService.writeDataToTXT(List.of(entry.toString()), "billingEntries.txt");

        // 创建CSV数据并写入文件
        String[] entryData = { entry.getCategory(), entry.getProduct(), String.valueOf(entry.getPrice()), entry.getRemark() };
        List<String[]> entryDataList = new ArrayList<>();
        entryDataList.add(entryData);
        csvService.writeDataToCSV(entryDataList, "billingEntries.csv");

        // 创建JSON格式
        jsonService.writeDataToJSON(List.of(entry), "billingEntries.json");

        // 创建XML格式
        xmlService.writeDataToXML(List.of(entry), "billingEntries.xml");

        return entry;
    }

    // 验证 token 是否有效
    private boolean isValidToken(String token) {
        try {
            // 从请求头中获取 token 并验证它
            String actualToken = token.replace("Bearer ", "");  // 删除 Bearer 前缀
            Key secretKey = Keys.hmacShaKeyFor(JWT_SECRET.getBytes()); // 使用正确的密钥
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(actualToken);
            return true;
        } catch (Exception e) {
            return false;  // 如果验证失败，返回 false
        }
    }
}
