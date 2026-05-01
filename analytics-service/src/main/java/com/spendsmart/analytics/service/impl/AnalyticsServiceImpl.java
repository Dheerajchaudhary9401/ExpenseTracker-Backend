package com.spendsmart.analytics.service.impl;

import com.spendsmart.analytics.model.*;
import com.spendsmart.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final JavaMailSender mailSender;

    @Value("${service.expense.url}")
    private String expenseServiceUrl;

    @Value("${service.income.url}")
    private String incomeServiceUrl;

    @Value("${service.budget.url}")
    private String budgetServiceUrl;

    @Override
    public MonthlySummary getMonthlySummary(int userId, int year, int month) {
        // In production, these would be REST API calls to other services
        // For now, using mock data

        double totalIncome = getMockIncome(userId, year, month);
        double totalExpense = getMockExpense(userId, year, month);
        double netSavings = totalIncome - totalExpense;
        double savingsRate = totalIncome > 0 ? (netSavings / totalIncome) * 100 : 0;
        double budgetUtilization = calculateBudgetUtilization(userId, year, month);

        return MonthlySummary.builder()
                .year(year)
                .month(month)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netSavings(netSavings)
                .savingsRate(savingsRate)
                .budgetUtilization(budgetUtilization)
                .transactionCount(45)
                .build();
    }

    @Override
    public List<CategoryExpense> getCategoryExpenses(int userId, int year, int month) {
        // Mock data - in production, fetch from expense-service grouped by category
        List<CategoryExpense> expenses = new ArrayList<>();

        expenses.add(CategoryExpense.builder()
                .categoryId(1).categoryName("Food")
                .amount(5000).percentage(35.7).transactionCount(12).build());

        expenses.add(CategoryExpense.builder()
                .categoryId(2).categoryName("Transport")
                .amount(3000).percentage(21.4).transactionCount(8).build());

        expenses.add(CategoryExpense.builder()
                .categoryId(3).categoryName("Shopping")
                .amount(2500).percentage(17.9).transactionCount(6).build());

        expenses.add(CategoryExpense.builder()
                .categoryId(4).categoryName("Bills")
                .amount(2000).percentage(14.3).transactionCount(4).build());

        expenses.add(CategoryExpense.builder()
                .categoryId(5).categoryName("Entertainment")
                .amount(1500).percentage(10.7).transactionCount(5).build());

        return expenses;
    }

    @Override
    public List<MonthlyTrend> getMonthlyTrend(int userId, int months) {
        List<MonthlyTrend> trends = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = months - 1; i >= 0; i--) {
            LocalDate date = today.minusMonths(i);
            String monthKey = date.format(DateTimeFormatter.ofPattern("yyyy-MM"));

            double income = 50000 + (Math.random() * 10000);
            double expense = 35000 + (Math.random() * 8000);

            trends.add(MonthlyTrend.builder()
                    .month(monthKey)
                    .income(income)
                    .expense(expense)
                    .savings(income - expense)
                    .build());
        }

        return trends;
    }

    @Override
    public List<CategoryExpense> getTopSpendingCategories(int userId, int limit) {
        List<CategoryExpense> all = getCategoryExpenses(userId,
                LocalDate.now().getYear(), LocalDate.now().getMonthValue());

        return all.stream()
                .sorted((a, b) -> Double.compare(b.getAmount(), a.getAmount()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public FinancialHealthScore getFinancialHealthScore(int userId) {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        // Calculate component scores
        double savingsRate = getSavingsRate(userId, year, month);
        double budgetAdherence = getBudgetAdherence(userId, year, month);

        MonthlySummary summary = getMonthlySummary(userId, year, month);
        double expenseToIncomeRatio = summary.getTotalIncome() > 0
                ? (summary.getTotalExpense() / summary.getTotalIncome()) * 100
                : 100;

        // Score calculations (0-100 scale)
        double savingsRateScore = Math.min(savingsRate * 2.5, 100); // 40% of total
        double budgetAdherenceScore = budgetAdherence; // 40% of total
        double expenseScore = Math.max(0, 100 - expenseToIncomeRatio); // 20% of total

        // Weighted overall score
        double overallScore = (savingsRateScore * 0.4) + (budgetAdherenceScore * 0.4) + (expenseScore * 0.2);

        String status;
        String recommendation;

        if (overallScore >= 80) {
            status = "EXCELLENT";
            recommendation = "Great job! Keep maintaining your financial discipline.";
        } else if (overallScore >= 60) {
            status = "GOOD";
            recommendation = "You're doing well. Consider increasing your savings rate.";
        } else if (overallScore >= 40) {
            status = "FAIR";
            recommendation = "Review your spending habits and stick to your budget.";
        } else {
            status = "POOR";
            recommendation = "Urgent: Reduce expenses and create a strict budget plan.";
        }

        return FinancialHealthScore.builder()
                .overallScore(Math.round(overallScore * 100.0) / 100.0)
                .savingsRateScore(Math.round(savingsRateScore * 100.0) / 100.0)
                .budgetAdherenceScore(Math.round(budgetAdherenceScore * 100.0) / 100.0)
                .expenseToIncomeScore(Math.round(expenseScore * 100.0) / 100.0)
                .healthStatus(status)
                .recommendation(recommendation)
                .build();
    }

    @Override
    public CashFlowSummary getCashFlowSummary(int userId, LocalDate startDate, LocalDate endDate) {
        // Mock calculation - in production, fetch from income/expense services
        double totalInflow = 150000;
        double totalOutflow = 105000;

        long months = java.time.temporal.ChronoUnit.MONTHS.between(startDate, endDate) + 1;

        return CashFlowSummary.builder()
                .totalInflow(totalInflow)
                .totalOutflow(totalOutflow)
                .netCashFlow(totalInflow - totalOutflow)
                .averageMonthlyIncome(totalInflow / months)
                .averageMonthlyExpense(totalOutflow / months)
                .build();
    }

    @Override
    public List<SpendingForecast> getSpendingForecast(int userId, int monthsAhead) {
        List<SpendingForecast> forecasts = new ArrayList<>();
        List<MonthlyTrend> historicalData = getMonthlyTrend(userId, 3);

        // Calculate average from last 3 months
        double avgExpense = historicalData.stream()
                .mapToDouble(MonthlyTrend::getExpense)
                .average()
                .orElse(0);

        double avgIncome = historicalData.stream()
                .mapToDouble(MonthlyTrend::getIncome)
                .average()
                .orElse(0);

        // Simple forecast with slight trend adjustment
        LocalDate today = LocalDate.now();
        for (int i = 1; i <= monthsAhead; i++) {
            LocalDate forecastDate = today.plusMonths(i);
            String monthKey = forecastDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));

            double trendFactor = 1 + (i * 0.02); // 2% monthly increase trend

            forecasts.add(SpendingForecast.builder()
                    .forecastMonth(monthKey)
                    .predictedExpense(avgExpense * trendFactor)
                    .predictedIncome(avgIncome * 1.01) // Slight income growth
                    .predictedSavings((avgIncome * 1.01) - (avgExpense * trendFactor))
                    .confidenceLevel(Math.max(60, 90 - (i * 10))) // Decreases with time
                    .build());
        }

        return forecasts;
    }

    @Override
    public double getSavingsRate(int userId, int year, int month) {
        MonthlySummary summary = getMonthlySummary(userId, year, month);
        return summary.getSavingsRate();
    }

    @Override
    public double getBudgetAdherence(int userId, int year, int month) {
        // Mock - in production, compare actual vs budgeted amounts from budget-service
        return 85.5; // 85.5% adherence
    }

    @Override
    public String exportTransactionsCSV(int userId, LocalDate startDate, LocalDate endDate) {
        try {
            StringWriter writer = new StringWriter();
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                    .withHeader("Date", "Type", "Category", "Description", "Amount", "Payment Method"));

            // Mock data - in production, fetch from expense/income services
            csvPrinter.printRecord("2026-04-20", "EXPENSE", "Food", "Lunch at restaurant", "450", "UPI");
            csvPrinter.printRecord("2026-04-18", "INCOME", "Salary", "Monthly salary", "50000", "BANK_TRANSFER");
            csvPrinter.printRecord("2026-04-15", "EXPENSE", "Transport", "Fuel", "2000", "CARD");

            csvPrinter.flush();
            return writer.toString();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate CSV", e);
        }
    }

    @Override
    public byte[] generateMonthlyReportPDF(int userId, int year, int month) {
        // Mock - in production, use iText7 to generate actual PDF with charts
        String reportContent = String.format(
                "Monthly Financial Report\nUser: %d\nPeriod: %d-%02d\n\nTotal Income: ₹50,000\nTotal Expense: ₹35,000\nNet Savings: ₹15,000",
                userId, year, month
        );

        return reportContent.getBytes();
    }

    @Override
    public void sendMonthlySummaryEmail(int userId, int year, int month) {
        MonthlySummary summary = getMonthlySummary(userId, year, month);

        String subject = String.format("Your Monthly Financial Summary - %d-%02d", year, month);
        String body = String.format(
                "Hello,\n\n" +
                        "Here's your financial summary for %d-%02d:\n\n" +
                        "Total Income: ₹%.2f\n" +
                        "Total Expense: ₹%.2f\n" +
                        "Net Savings: ₹%.2f\n" +
                        "Savings Rate: %.2f%%\n" +
                        "Budget Utilization: %.2f%%\n\n" +
                        "Keep up the good work!\n\n" +
                        "SpendSmart Team",
                year, month,
                summary.getTotalIncome(),
                summary.getTotalExpense(),
                summary.getNetSavings(),
                summary.getSavingsRate(),
                summary.getBudgetUtilization()
        );

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("user@example.com"); // In production, fetch from user-service
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            System.out.println("Monthly summary email sent to user: " + userId);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }

    // Helper methods
    private double getMockIncome(int userId, int year, int month) {
        // Mock - in production, call income-service API
        return 50000 + (Math.random() * 10000);
    }

    private double getMockExpense(int userId, int year, int month) {
        // Mock - in production, call expense-service API
        return 35000 + (Math.random() * 5000);
    }

    private double calculateBudgetUtilization(int userId, int year, int month) {
        // Mock - in production, call budget-service API
        return 75.5 + (Math.random() * 15);
    }
}