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
    public ResponseEntity<SummaryResponse> getExpenditureSummary(
            @RequestParam(value = "period", defaultValue = "Month") String period,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestHeader(name = "Authorization", required = true) String authorizationHeader) {
        
        String userId = extractUserIdFromHeader(authorizationHeader);
        if (userId == null) {
            return ResponseEntity.status(401).body(null);
        }

        try {
            SummaryResponse response = summaryService.getExpenditureSummary(period, startDate, endDate);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/income")
    public ResponseEntity<SummaryResponse> getIncomeSummary(
            @RequestParam(value = "period", defaultValue = "Month") String period,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestHeader(name = "Authorization", required = true) String authorizationHeader) {
        
        String userId = extractUserIdFromHeader(authorizationHeader);
        if (userId == null) {
            return ResponseEntity.status(401).body(null);
        }

        try {
            SummaryResponse response = summaryService.getIncomeSummary(period, startDate, endDate);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
}
