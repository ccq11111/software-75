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

    private static final String JWT_SECRET = "mySecretKey1234567890";  // 可替换为更加安全的密钥，或者使用自动生成的密钥

    @PostMapping("/entries")
    public BillingEntry createBillingEntry(@RequestHeader("Authorization") String token, @RequestBody BillingEntry entry) throws IOException, JAXBException, IOException, JAXBException {
        // 验证 token
        if (!isValidToken(token)) {
            throw new RuntimeException("无效的 token: The token is either expired, malformed, or missing.");
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
            // 使用生成的密钥来解析 token
            Key secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256); // 使用自动生成的密钥，确保其符合标准
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(actualToken); // 解析并验证 token
            return true;
        } catch (Exception e) {
            // 打印详细错误信息，帮助定位问题
            e.printStackTrace();
            return false;  // 如果验证失败，返回 false
        }
    }
}
