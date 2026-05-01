package com.spendsmart.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialHealthScore {
    private double overallScore;
    private double savingsRateScore;
    private double budgetAdherenceScore;
    private double expenseToIncomeScore;
    private String healthStatus; // EXCELLENT, GOOD, FAIR, POOR
    private String recommendation;
}