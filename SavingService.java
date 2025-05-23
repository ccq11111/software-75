package com.example.software.service;

import com.example.software.model.SavingsPlan;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SavingService {
    private final List<SavingsPlan> savingPlans = new ArrayList<>();

    public Map<String, Object> getSavingPlans() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("plans", savingPlans);
        return response;
    }

    public SavingsPlan addPlan(SavingsPlan plan) {
        savingPlans.add(plan);
        return plan;
    }
} 