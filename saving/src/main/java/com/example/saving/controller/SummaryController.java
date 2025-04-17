package com.example.saving.controller;

import com.example.saving.model.SummaryResponse;
import com.example.saving.service.JwtSecretKeyService;
import com.example.saving.service.SummaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/summary")
public class SummaryController {

    private final SummaryService summaryService;
    private final JwtSecretKeyService jwtService;

    public SummaryController(SummaryService summaryService, JwtSecretKeyService jwtService) {
        this.summaryService = summaryService;
        this.jwtService = jwtService;
    }

    // ✅ 提取验证逻辑：仅返回 userId，不再返回 ResponseEntity，错误在主函数中统一处理
    private String extractUserIdFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7);
        if (!jwtService.validateToken(token)) {
            return null;
        }

        String userId = jwtService.extractUserId(token);
        if (userId == null || userId.isEmpty()) {
            return null;
        }

        return userId;
    }

    @GetMapping("/expenditure")
    public ResponseEntity<?> getExpenditureSummary(
            @RequestParam(defaultValue = "Month") String period,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        String userId = extractUserIdFromHeader(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).body("Unauthorized - Invalid or missing token");
        }

        try {
            SummaryResponse response = summaryService.getExpenditureSummary(period);//, userId)
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Internal server error"));
        }
    }

    @GetMapping("/income")
    public ResponseEntity<?> getIncomeSummary(
            @RequestParam(defaultValue = "Month") String period,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        String userId = extractUserIdFromHeader(authHeader);
        if (userId == null) {
            return ResponseEntity.status(401).body("Unauthorized - Invalid or missing token");
        }

        try {
            SummaryResponse response = summaryService.getIncomeSummary(period);//, userId)
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "Internal server error"));
        }
    }
}
