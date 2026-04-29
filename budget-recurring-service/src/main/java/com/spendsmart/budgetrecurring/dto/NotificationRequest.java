package com.spendsmart.budgetrecurring.dto;

import com.spendsmart.budgetrecurring.entity.Notification.NotificationType;
import com.spendsmart.budgetrecurring.entity.Notification.Severity;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class NotificationRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    @NotNull(message = "Type is required")
    private NotificationType type;

    private Severity severity = Severity.INFO;

    private Integer relatedId;

    private String relatedType;
}