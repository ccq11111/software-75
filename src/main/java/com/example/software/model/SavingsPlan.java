package com.example.software.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "savings_plans")
public class SavingsPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String planId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Instant startDate;

    @Column(nullable = false)
    private Instant endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CycleType cycle;

    @Column(nullable = false)
    private Integer cycleTimes;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal savedAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CurrencyType currency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 全参数构造方法
    public SavingsPlan(String planId, String name, LocalDate startDate, LocalDate endDate,
                      String cycle, int cycleTimes, double amount, double totalAmount,
                      String currency, double savedAmount) {
        this.planId = planId;
        this.name = name;
        this.startDate = Instant.from(startDate);
        this.endDate = Instant.from(endDate);
        this.cycle = CycleType.valueOf(cycle);
        this.cycleTimes = cycleTimes;
        this.amount = BigDecimal.valueOf(amount);
        this.totalAmount = BigDecimal.valueOf(totalAmount);
        this.currency = CurrencyType.valueOf(currency);
        this.savedAmount = BigDecimal.valueOf(savedAmount);
    }
    // 构造函数
    public SavingsPlan() {
        this.savedAmount = BigDecimal.ZERO;
    }

    public enum CycleType {
        Daily, Weekly, Monthly, Quarterly, Yearly
    }

    public enum CurrencyType {
        USD, CNY, EUR
    }
}