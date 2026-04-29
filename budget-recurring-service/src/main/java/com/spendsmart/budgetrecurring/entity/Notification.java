package com.spendsmart.budgetrecurring.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private int notificationId;

    @Column(name = "recipient_id", nullable = false)
    private int recipientId;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column(name = "severity")
    @Enumerated(EnumType.STRING)
    private Severity severity = Severity.INFO;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    @Column(name = "related_id")
    private Integer relatedId;

    @Column(name = "related_type")
    private String relatedType;

    @Column(name = "is_read")
    private boolean isRead = false;

    @Column(name = "is_acknowledged")
    private boolean isAcknowledged = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum NotificationType {
        BUDGET_ALERT,
        RECURRING_DUE,
        MONTHLY_SUMMARY,
        BUDGET_EXCEEDED,
        SYSTEM
    }

    public enum Severity {
        INFO,
        WARNING,
        CRITICAL
    }
}