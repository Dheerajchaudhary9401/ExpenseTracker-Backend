package com.spendsmart.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private int userId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "currency", length = 10)
    private String currency = "INR";

    @Column(name = "timezone")
    private String timezone = "Asia/Kolkata";

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "bio", length = 500)
    private String bio;

    @Column(name = "provider", nullable = false)
    private String provider = "LOCAL";

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "monthly_budget")
    private double monthlyBudget = 0.0;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}