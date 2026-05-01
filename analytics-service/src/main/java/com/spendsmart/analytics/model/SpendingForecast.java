package com.spendsmart.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpendingForecast {
    private String forecastMonth;
    private double predictedExpense;
    private double predictedIncome;
    private double predictedSavings;
    private double confidenceLevel;
}