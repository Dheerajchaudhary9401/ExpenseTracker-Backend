package com.spendsmart.budgetrecurring.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "budgets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "budget_id")
    private int budgetId;

    @Column(name = "user_id", nullable = false)
    private int userId;

    @Column(name = "category_id", nullable = false)
    private int categoryId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "limit_amount", nullable = false)
    private double limitAmount;

    @Column(name = "currency", length = 10)
    private String currency = "INR";

    @Column(name = "period")
    @Enumerated(EnumType.STRING)
    private Period period = Period.MONTHLY;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "spent_amount")
    private double spentAmount = 0.0;

    @Column(name = "alert_threshold")
    private double alertThreshold = 80.0;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum Period {
        WEEKLY,
        MONTHLY,
        CUSTOM
    }
}