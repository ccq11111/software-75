package com.example.loginapp.service;

import com.example.loginapp.entity.SavingPlan;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SavingService {
    private final List<SavingPlan> savingPlans = new ArrayList<>();

    public Map<String, Object> getSavingPlans() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("plans", savingPlans);
        return response;
    }

    public SavingPlan addPlan(SavingPlan plan) {
        savingPlans.add(plan);
        return plan;
    }
} 