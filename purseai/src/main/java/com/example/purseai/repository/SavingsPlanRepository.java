package com.example.purseai.repository;

import com.example.purseai.model.SavingsPlan;
import com.example.purseai.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavingsPlanRepository extends JpaRepository<SavingsPlan, String> {
    List<SavingsPlan> findByUser(User user);
    Optional<SavingsPlan> findByPlanIdAndUser(String planId, User user);
} 