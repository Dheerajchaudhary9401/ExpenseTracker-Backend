package com.spendsmart.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryExpense {
    private int categoryId;
    private String categoryName;
    private double amount;
    private double percentage;
    private int transactionCount;
}