package com.spendsmart.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashFlowSummary {
    private double totalInflow;
    private double totalOutflow;
    private double netCashFlow;
    private double averageMonthlyIncome;
    private double averageMonthlyExpense;
}