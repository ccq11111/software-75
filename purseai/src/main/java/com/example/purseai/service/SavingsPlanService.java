package com.example.purseai.service;

import com.example.purseai.dto.SavingsPlanRequest;
import com.example.purseai.dto.SavingsPlanResponse;
import com.example.purseai.model.SavingsPlan;
import com.example.purseai.model.User;
import com.example.purseai.repository.SavingsPlanRepository;
import com.example.purseai.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SavingsPlanService {

    private final SavingsPlanRepository savingsPlanRepository;
    private final UserRepository userRepository;
    
    public SavingsPlanService(SavingsPlanRepository savingsPlanRepository, UserRepository userRepository) {
        this.savingsPlanRepository = savingsPlanRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public SavingsPlanResponse createPlan(SavingsPlanRequest request, String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Instant endDate = calculateEndDate(request.getStartDate(), request.getCycle(), request.getCycleTimes());
        BigDecimal totalAmount = request.getAmount().multiply(BigDecimal.valueOf(request.getCycleTimes()));

        SavingsPlan savingsPlan = new SavingsPlan();
        savingsPlan.setName(request.getName());
        savingsPlan.setStartDate(request.getStartDate());
        savingsPlan.setEndDate(endDate);
        savingsPlan.setCycle(request.getCycle());
        savingsPlan.setCycleTimes(request.getCycleTimes());
        savingsPlan.setAmount(request.getAmount());
        savingsPlan.setTotalAmount(totalAmount);
        savingsPlan.setSavedAmount(BigDecimal.ZERO);
        savingsPlan.setCurrency(request.getCurrency());
        savingsPlan.setUser(user);

        SavingsPlan savedPlan = savingsPlanRepository.save(savingsPlan);
        
        return createResponse(savedPlan, true);
    }

    @Transactional(readOnly = true)
    public List<SavingsPlanResponse> getAllPlans(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return savingsPlanRepository.findByUser(user).stream()
                .map(plan -> createResponse(plan, true))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SavingsPlanResponse getPlan(String planId, String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        SavingsPlan plan = savingsPlanRepository.findByPlanIdAndUser(planId, user)
                .orElseThrow(() -> new EntityNotFoundException("Savings plan not found"));
        
        return createResponse(plan, true);
    }

    @Transactional
    public SavingsPlanResponse updatePlan(String planId, SavingsPlanRequest request, String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        SavingsPlan plan = savingsPlanRepository.findByPlanIdAndUser(planId, user)
                .orElseThrow(() -> new EntityNotFoundException("Savings plan not found"));
        
        // Update allowed fields
        plan.setName(request.getName());
        plan.setAmount(request.getAmount());
        plan.setCycle(request.getCycle());
        
        // Recalculate derived fields
        Instant endDate = calculateEndDate(plan.getStartDate(), plan.getCycle(), plan.getCycleTimes());
        plan.setEndDate(endDate);
        plan.setTotalAmount(plan.getAmount().multiply(BigDecimal.valueOf(plan.getCycleTimes())));
        
        SavingsPlan updatedPlan = savingsPlanRepository.save(plan);
        return createResponse(updatedPlan, true);
    }

    @Transactional
    public void deletePlan(String planId, String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        SavingsPlan plan = savingsPlanRepository.findByPlanIdAndUser(planId, user)
                .orElseThrow(() -> new EntityNotFoundException("Savings plan not found"));
        
        savingsPlanRepository.delete(plan);
    }

    private Instant calculateEndDate(Instant startDate, SavingsPlan.CycleType cycle, int cycleTimes) {
        switch (cycle) {
            case Daily:
                return startDate.plus(cycleTimes, ChronoUnit.DAYS);
            case Weekly:
                return startDate.plus(cycleTimes * 7, ChronoUnit.DAYS);
            case Monthly:
                return startDate.plus(cycleTimes, ChronoUnit.MONTHS);
            case Quarterly:
                return startDate.plus(cycleTimes * 3, ChronoUnit.MONTHS);
            case Yearly:
                return startDate.plus(cycleTimes, ChronoUnit.YEARS);
            default:
                throw new IllegalArgumentException("Unsupported cycle type");
        }
    }

    private SavingsPlanResponse createResponse(SavingsPlan plan, boolean success) {
        SavingsPlanResponse response = new SavingsPlanResponse();
        response.setSuccess(success);
        response.setPlanId(plan.getPlanId());
        response.setName(plan.getName());
        response.setStartDate(plan.getStartDate());
        response.setEndDate(plan.getEndDate());
        response.setCycle(plan.getCycle());
        response.setCycleTimes(plan.getCycleTimes());
        response.setAmount(plan.getAmount());
        response.setTotalAmount(plan.getTotalAmount());
        response.setCurrency(plan.getCurrency());
        response.setSavedAmount(plan.getSavedAmount());
        return response;
    }
} 