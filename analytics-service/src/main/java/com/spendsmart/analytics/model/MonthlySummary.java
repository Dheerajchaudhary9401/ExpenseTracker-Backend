package com.spendsmart.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlySummary {
    private int year;
    private int month;
    private double totalIncome;
    private double totalExpense;
    private double netSavings;
    private double savingsRate;
    private double budgetUtilization;
    private int transactionCount;
}