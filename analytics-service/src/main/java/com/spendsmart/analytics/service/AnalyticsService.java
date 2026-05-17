package com.spendsmart.analytics.service;

import com.spendsmart.analytics.model.*;
import java.time.LocalDate;
import java.util.List;

public interface AnalyticsService {

    MonthlySummary getMonthlySummary(int userId, int year, int month);

    List<CategoryExpense> getCategoryExpenses(int userId, int year, int month);

    List<MonthlyTrend> getMonthlyTrend(int userId, int months);

    List<CategoryExpense> getTopSpendingCategories(int userId, int limit);

    FinancialHealthScore getFinancialHealthScore(int userId);

    CashFlowSummary getCashFlowSummary(int userId, LocalDate startDate, LocalDate endDate);

    List<SpendingForecast> getSpendingForecast(int userId, int monthsAhead);

    double getSavingsRate(int userId, int year, int month);

    double getBudgetAdherence(int userId, int year, int month);

    String exportTransactionsCSV(int userId, LocalDate startDate, LocalDate endDate);

    byte[] generateMonthlyReportPDF(int userId, int year, int month);

    void sendMonthlySummaryEmail(int userId, String email, int year, int month);
}