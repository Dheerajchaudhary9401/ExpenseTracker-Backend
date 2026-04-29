package com.spendsmart.budgetrecurring.dto;

import com.spendsmart.budgetrecurring.entity.Budget.Period;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class BudgetRequest {

    @NotBlank(message = "Budget name is required")
    private String name;

    @NotNull(message = "Category is required")
    private Integer categoryId;

    @NotNull(message = "Limit amount is required")
    @Positive(message = "Limit must be positive")
    private Double limitAmount;

    @NotNull(message = "Period is required")
    private Period period;

    private LocalDate startDate;

    private LocalDate endDate;

    private Double alertThreshold = 80.0;

    private String currency = "INR";
}