package com.example.software.service;

import com.example.software.dto.SavingsPlanRequest;
import com.example.software.dto.SavingsPlanResponse;
import com.example.software.model.SavingsPlan;
import com.example.software.model.User;
import com.example.software.repository.SavingsPlanRepository;
import com.example.software.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SavingsPlanService {

    private final SavingsPlanRepository savingsPlanRepository;
    private final UserRepository userRepository;
    
    @Autowired
    public SavingsPlanService(SavingsPlanRepository savingsPlanRepository, UserRepository userRepository) {
        this.savingsPlanRepository = savingsPlanRepository;
        this.userRepository = userRepository;
    }
    
    public SavingsPlanResponse createPlan(SavingsPlanRequest request, String userId) {
        // 获取用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        // 创建新的储蓄计划
        SavingsPlan savingsPlan = new SavingsPlan();
        savingsPlan.setPlanId(UUID.randomUUID().toString());
        savingsPlan.setName(request.getName());
        savingsPlan.setStartDate(request.getStartDate());
        savingsPlan.setCycle(request.getCycle());
        savingsPlan.setCycleTimes(request.getCycleTimes());
        savingsPlan.setAmount(request.getAmount());
        savingsPlan.setCurrency(request.getCurrency());
        savingsPlan.setUser(user);
        savingsPlan.setSavedAmount(BigDecimal.ZERO);
        
        // 计算总金额和结束日期
        savingsPlan.setTotalAmount(request.getAmount().multiply(BigDecimal.valueOf(request.getCycleTimes())));
        savingsPlan.setEndDate(calculateEndDate(savingsPlan.getStartDate(), savingsPlan.getCycle(), savingsPlan.getCycleTimes()));
        
        // 保存并返回响应
        SavingsPlan savedPlan = savingsPlanRepository.save(savingsPlan);
        return createResponse(savedPlan, true);
    }
    
    public List<SavingsPlanResponse> getAllPlans(String userId) {
        // 获取用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        // 获取用户的所有计划
        List<SavingsPlan> plans = savingsPlanRepository.findByUser(user);
        
        // 转换为响应对象
        return plans.stream()
                .map(plan -> createResponse(plan, false))
                .collect(Collectors.toList());
    }
    
    public SavingsPlanResponse getPlan(String planId, String userId) {
        // 获取用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        // 获取特定计划
        Optional<SavingsPlan> planOpt = savingsPlanRepository.findByPlanIdAndUser(planId, user);
        
        if (!planOpt.isPresent()) {
            throw new RuntimeException("Plan not found or not owned by user");
        }
        
        return createResponse(planOpt.get(), false);
    }
    
    public SavingsPlanResponse updatePlan(String planId, SavingsPlanRequest request, String userId) {
        // 获取用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        // 获取特定计划
        Optional<SavingsPlan> planOpt = savingsPlanRepository.findByPlanIdAndUser(planId, user);
        
        if (!planOpt.isPresent()) {
            throw new RuntimeException("Plan not found or not owned by user");
        }
        
        SavingsPlan plan = planOpt.get();
        
        // 更新计划属性
        plan.setName(request.getName());
        plan.setCycle(request.getCycle());
        plan.setCycleTimes(request.getCycleTimes());
        plan.setAmount(request.getAmount());
        plan.setCurrency(request.getCurrency());
        
        // 重新计算总金额和结束日期
        plan.setTotalAmount(request.getAmount().multiply(BigDecimal.valueOf(request.getCycleTimes())));
        plan.setEndDate(calculateEndDate(plan.getStartDate(), plan.getCycle(), plan.getCycleTimes()));
        
        // 保存并返回响应
        SavingsPlan updatedPlan = savingsPlanRepository.save(plan);
        return createResponse(updatedPlan, true);
    }
    
    public void deletePlan(String planId, String userId) {
        // 获取用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        // 获取特定计划
        Optional<SavingsPlan> planOpt = savingsPlanRepository.findByPlanIdAndUser(planId, user);
        
        if (!planOpt.isPresent()) {
            throw new RuntimeException("Plan not found or not owned by user");
        }
        
        // 删除计划
        savingsPlanRepository.deleteById(planId);
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