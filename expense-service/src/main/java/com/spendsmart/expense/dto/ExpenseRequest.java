package com.spendsmart.expense.dto;

import com.spendsmart.expense.entity.Expense.ExpenseType;
import com.spendsmart.expense.entity.Expense.PaymentMethod;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ExpenseRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;

    @NotNull(message = "Category is required")
    private Integer categoryId;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private String notes;

    private String receiptUrl;

    private ExpenseType type = ExpenseType.EXPENSE;

    private String currency = "INR";

    private boolean isRecurring = false;
}