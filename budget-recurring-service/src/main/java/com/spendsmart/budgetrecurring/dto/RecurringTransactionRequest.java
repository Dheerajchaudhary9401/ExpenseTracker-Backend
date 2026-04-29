package com.spendsmart.budgetrecurring.dto;

import com.spendsmart.budgetrecurring.entity.RecurringTransaction.TransactionType;
import com.spendsmart.budgetrecurring.entity.RecurringTransaction.Frequency;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class RecurringTransactionRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;

    @NotNull(message = "Category is required")
    private Integer categoryId;

    @NotNull(message = "Type is required")
    private TransactionType type;

    @NotNull(message = "Frequency is required")
    private Frequency frequency;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    private String description;

    private String paymentMethod;
}