package com.example.purseai.controller;

import com.example.purseai.dto.SavingsPlanRequest;
import com.example.purseai.dto.SavingsPlanResponse;
import com.example.purseai.service.SavingsPlanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/savings/plans")
public class SavingsPlanController {

    private final SavingsPlanService savingsPlanService;
    
    public SavingsPlanController(SavingsPlanService savingsPlanService) {
        this.savingsPlanService = savingsPlanService;
    }

    @PostMapping
    public ResponseEntity<SavingsPlanResponse> createPlan(@Valid @RequestBody SavingsPlanRequest request) {
        UserDetails userDetails = getCurrentUser();
        String userId = getUserIdFromToken(userDetails.getUsername());
        
        SavingsPlanResponse response = savingsPlanService.createPlan(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllPlans() {
        UserDetails userDetails = getCurrentUser();
        String userId = getUserIdFromToken(userDetails.getUsername());
        
        List<SavingsPlanResponse> plans = savingsPlanService.getAllPlans(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("plans", plans);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{planId}")
    public ResponseEntity<SavingsPlanResponse> getPlan(@PathVariable String planId) {
        UserDetails userDetails = getCurrentUser();
        String userId = getUserIdFromToken(userDetails.getUsername());
        
        SavingsPlanResponse response = savingsPlanService.getPlan(planId, userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{planId}")
    public ResponseEntity<SavingsPlanResponse> updatePlan(
            @PathVariable String planId,
            @Valid @RequestBody SavingsPlanRequest request) {
        UserDetails userDetails = getCurrentUser();
        String userId = getUserIdFromToken(userDetails.getUsername());
        
        SavingsPlanResponse response = savingsPlanService.updatePlan(planId, request, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{planId}")
    public ResponseEntity<Map<String, Object>> deletePlan(@PathVariable String planId) {
        UserDetails userDetails = getCurrentUser();
        String userId = getUserIdFromToken(userDetails.getUsername());
        
        savingsPlanService.deletePlan(planId, userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "计划删除成功");
        
        return ResponseEntity.ok(response);
    }

    private UserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (UserDetails) authentication.getPrincipal();
    }

    private String getUserIdFromToken(String username) {
        // In a real implementation, you would extract the userId from the token
        // or look up the user by username. This is a simplified version.
        return username;
    }
} 