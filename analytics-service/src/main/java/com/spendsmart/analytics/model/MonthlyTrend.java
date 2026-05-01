package com.spendsmart.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyTrend {
    private String month; // "2026-04"
    private double income;
    private double expense;
    private double savings;
}