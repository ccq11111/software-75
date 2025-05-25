package com.example.software.controller;

import com.example.software.model.SavingsPlan;
import com.example.software.model.SavingRequest;
import com.example.software.security.JwtUtil;
import com.example.software.service.JwtSecretKeyService;
import com.example.software.service.SavingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/savings")
public class SavingController {

    @Autowired
    private SavingService savingService;
//    private final JwtSecretKeyService jwtService;

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserDetailsService userDetailsService;

//    public SavingController(SavingService savingService, JwtSecretKeyService jwtService) {
//        this.savingService = savingService;
//        this.jwtService = jwtService;
//    }

//    @Value("${user.default}")
//    private String defaultUser;

//    @PostMapping("/token")
//    public ResponseEntity<Map<String, String>> generateToken() {
//        String token = jwtService.generateToken(defaultUser);
//        return ResponseEntity.ok(Map.of("token", token));
//    }


//    @GetMapping("/plans")
    public ResponseEntity<?> getSavingPlans(
            @RequestHeader(name = "Authorization", required = true) String authorizationHeader) {

        // 1. 验证Token
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Unauthorized - Invalid or missing token");
        }

        String token = authorizationHeader.substring(7);
//        if (!jwtService.validateToken(token)) {
//            return ResponseEntity.status(401).body("Unauthorized - Invalid token");
//        }
        String username = jwtUtil.extractUsername(token);
        boolean res = jwtUtil.validateToken(token,userDetailsService.loadUserByUsername(username));
        if (!res) {
            return ResponseEntity.status(401).body("Unauthorized - Invalid token");
        }

        // 2. 从Token中提取用户ID（假设JWT包含userId）
//        String userId = jwtService.extractUserId(token);

        // 3. 获取动态数据
        Map<String, Object> response = savingService.getSavingPlans();

        if (!(Boolean) response.get("success")) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/plans")
    public ResponseEntity<?> createPlan(
            @RequestBody(required = true) SavingRequest savingRequest,
            @RequestHeader(name = "Authorization", required = true) String authorizationHeader
    ) {
        // Token 校验（略）

        // 逻辑构造 SavingPlan
        SavingsPlan plan = new SavingsPlan(
                UUID.randomUUID().toString(),
                savingRequest.getPlanName(),
                LocalDate.now(),
                LocalDate.now().plusDays(savingRequest.getDurationDays()),
                "DAILY",                  // 假设固定 DAILY
                savingRequest.getDurationDays(),
                savingRequest.getAmount(),
                savingRequest.getAmount() * savingRequest.getDurationDays(),
                "CNY",                    // 假设固定币种
                0.0
        );

        SavingsPlan added = savingService.addPlan(plan);

        return ResponseEntity.ok(Map.of("success", true, "plan", added));
    }

}