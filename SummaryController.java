package com.example.software.controller;

import com.example.software.model.SummaryResponse;
import com.example.software.security.JwtUtil;
import com.example.software.service.JwtSecretKeyService;
import com.example.software.service.SummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Logger;

@RestController
@RequestMapping("/v1/summary")
public class SummaryController {
    private static final Logger logger = Logger.getLogger(SummaryController.class.getName());
    private final SummaryService summaryService;
    private final JwtSecretKeyService jwtService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserDetailsService userDetailsService;
    public SummaryController(SummaryService summaryService, JwtSecretKeyService jwtService) {
        this.summaryService = summaryService;
        this.jwtService = jwtService;
    }

//    private String extractUserIdFromHeader(String authHeader) {
//        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//            return null;
//        }
//
//        String token = authHeader.substring(7);
//        if (!jwtService.validateToken(token)) {
//            return null;
//        }
//
//        String userId = jwtService.extractUserId(token);
//        if (userId == null || userId.isEmpty()) {
//            return null;
//        }
//
//        return userId;
//    }

    @GetMapping("/expenditure")
    public ResponseEntity<SummaryResponse> getExpenditureSummary(
            @RequestParam(value = "period", defaultValue = "Month") String period,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestHeader(name = "Authorization", required = true) String authorizationHeader
    ) {
        
//        String userId = extractUserIdFromHeader(authorizationHeader);
//        if (userId == null) {
//            return ResponseEntity.status(401).body(null);
//        }
        logger.info("Received expenditure request with token: " + authorizationHeader);
        authorizationHeader = authorizationHeader.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(authorizationHeader);
        boolean res = jwtUtil.validateToken(authorizationHeader,userDetailsService.loadUserByUsername(username));
        if (!res) {
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
        
//        String userId = extractUserIdFromHeader(authorizationHeader);
//        if (userId == null) {
//            return ResponseEntity.status(401).body(null);
//        }
        logger.info("Received income request with token: " + authorizationHeader);
        authorizationHeader = authorizationHeader.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(authorizationHeader);
        boolean res = jwtUtil.validateToken(authorizationHeader,userDetailsService.loadUserByUsername(username));
        if (!res) {
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
