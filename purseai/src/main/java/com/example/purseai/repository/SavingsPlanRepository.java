package com.example.purseai.repository;

import com.example.purseai.model.SavingsPlan;
import com.example.purseai.model.User;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavingsPlanRepository {
    SavingsPlan save(SavingsPlan savingsPlan);
    List<SavingsPlan> findByUser(User user);
    Optional<SavingsPlan> findByPlanIdAndUser(String planId, User user);
    Optional<SavingsPlan> findById(String planId);
    List<SavingsPlan> findAll();
    void deleteById(String planId);
}