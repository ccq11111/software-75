package com.example.purseai.repository;

import com.example.purseai.model.SavingsPlan;
import com.example.purseai.model.User;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavingsPlanRepository {
    // 保存储蓄计划
    SavingsPlan save(SavingsPlan savingsPlan);
    
    // 查找用户的所有储蓄计划
    List<SavingsPlan> findByUser(User user);
    
    // 查找特定用户的特定计划
    Optional<SavingsPlan> findByPlanIdAndUser(String planId, User user);
    
    // 查找所有储蓄计划
    List<SavingsPlan> findAll();
    
    // 删除储蓄计划
    void deleteById(String planId);
}