package com.spendsmart.income.dto;

import com.spendsmart.income.entity.Income.IncomeSource;
import com.spendsmart.income.entity.Income.RecurrencePeriod;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class IncomeRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;

    @NotNull(message = "Category is required")
    private Integer categoryId;

    @NotNull(message = "Source is required")
    private IncomeSource source;

    @NotNull(message = "Date is required")
    private LocalDate date;

    private String notes;

    private String currency = "INR";

    private boolean isRecurring = false;

    private RecurrencePeriod recurrencePeriod;
}