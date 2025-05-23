package com.example.software.controller;

import com.example.software.dto.SavingsPlanRequest;
import com.example.software.dto.SavingsPlanResponse;
import com.example.software.model.User;
import com.example.software.security.UserDetailsImpl;
import com.example.software.service.SavingsPlanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/savings/plans")
public class SavingsPlanController {

    @Autowired
    private SavingsPlanService savingsPlanService;
    
//    public SavingsPlanController(SavingsPlanService savingsPlanService) {
//        this.savingsPlanService = savingsPlanService;
//    }
    @PostMapping("/createPlan")
    public ResponseEntity<SavingsPlanResponse> createPlan(@Valid @RequestBody SavingsPlanRequest request) {
        User user = getCurrentUser();
        String userId = user.getUserId();

        SavingsPlanResponse response = savingsPlanService.createPlan(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/getAllPlans")
    public ResponseEntity<Map<String, Object>> getAllPlans() {
        User user = getCurrentUser();
        String userId = user.getUserId();

        List<SavingsPlanResponse> plans = savingsPlanService.getAllPlans(userId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("plans", plans);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/getPlan")
    public ResponseEntity<SavingsPlanResponse> getPlan(@RequestParam(value = "planId") String planId) {
        User user = getCurrentUser();
        String userId = user.getUserId();
        
        SavingsPlanResponse response = savingsPlanService.getPlan(planId, userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/updatePlan")
    public ResponseEntity<SavingsPlanResponse> updatePlan(
            @RequestParam(value = "planId") String planId,
            @Valid @RequestBody SavingsPlanRequest request) {
        User user = getCurrentUser();
        String userId = user.getUserId();
        
        SavingsPlanResponse response = savingsPlanService.updatePlan(planId, request, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/deletePlan")
    public ResponseEntity<Map<String, Object>> deletePlan(@RequestParam(value = "planId") String planId) {
        User user = getCurrentUser();

        savingsPlanService.deletePlan(planId, user.getUserId());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "计划删除成功");
        
        return ResponseEntity.ok(response);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        return principal.getUser();
    }

    private String getUserIdFromToken(String username) {
        // In a real implementation, you would extract the userId from the token
        // or look up the user by username. This is a simplified version.
        return username;
    }
} 