package com.spendsmart.analytics.service.impl;

import com.spendsmart.analytics.model.*;
import com.spendsmart.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.StringWriter;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final JavaMailSender mailSender;

    private final WebClient.Builder webClientBuilder;

    @Value("${service.expense.url}")
    private String expenseServiceUrl;

    @Value("${service.income.url}")
    private String incomeServiceUrl;

    @Value("${service.budget.url}")
    private String budgetServiceUrl;

    private double getExpenseTotal(int userId, int year, int month) {
        try {
            YearMonth ym = YearMonth.of(year, month);
            String start = ym.atDay(1).toString();
            String end   = ym.atEndOfMonth().toString();

            Double total = webClientBuilder.build()
                    .get()
                    .uri(expenseServiceUrl + "/expenses/user/{userId}/range?startDate={s}&endDate={e}",
                            userId, start, end)
                    .retrieve()
                    // ParameterizedTypeReference handles generic types like List<Map>
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .map(expenses -> expenses.stream()
                            .mapToDouble(e -> ((Number) e.getOrDefault("amount", 0)).doubleValue())
                            .sum())
                    .onErrorReturn(0.0)  // if expense-service is down, return 0
                    .block();

            return total != null ? total : 0.0;
        } catch (Exception e) {
            log.warn("Failed to fetch expense total from expense-service: {}", e.getMessage());
            return 0.0;
        }
    }

    private double getIncomeTotal(int userId, int year, int month) {
        try {
            Double total = webClientBuilder.build()
                    .get()
                    .uri(incomeServiceUrl + "/incomes/user/{userId}/total/month/{year}/{month}",
                            userId, year, month)
                    .retrieve()
                    .bodyToMono(Double.class)
                    .onErrorReturn(0.0)
                    .block();

            return total != null ? total : 0.0;
        } catch (Exception e) {
            log.warn("Failed to fetch income total from income-service: {}", e.getMessage());
            return 0.0;
        }
    }

    private List<Map<String, Object>> getExpenseListForMonth(int userId, int year, int month) {
        try {
            YearMonth ym = YearMonth.of(year, month);
            String start = ym.atDay(1).toString();
            String end   = ym.atEndOfMonth().toString();

            List<Map<String, Object>> expenses = webClientBuilder.build()
                    .get()
                    .uri(expenseServiceUrl + "/expenses/user/{userId}/range?startDate={s}&endDate={e}",
                            userId, start, end)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .onErrorReturn(Collections.emptyList())
                    .block();

            return expenses != null ? expenses : Collections.emptyList();
        } catch (Exception e) {
            log.warn("Failed to fetch expenses: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<Map<String, Object>> getActiveBudgets(int userId) {
        try {
            List<Map<String, Object>> budgets = webClientBuilder.build()
                    .get()
                    .uri(budgetServiceUrl + "/budgets/user/{userId}/active", userId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .onErrorReturn(Collections.emptyList())
                    .block();

            return budgets != null ? budgets : Collections.emptyList();
        } catch (Exception e) {
            log.warn("Failed to fetch budgets: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public MonthlySummary getMonthlySummary(int userId, int year, int month) {
        double totalIncome  = getIncomeTotal(userId, year, month);
        double totalExpense = getExpenseTotal(userId, year, month);
        double netSavings   = totalIncome - totalExpense;
        double savingsRate  = totalIncome > 0 ? (netSavings / totalIncome) * 100 : 0;
        double budgetUtil   = calculateBudgetUtilization(userId, year, month);

        List<Map<String, Object>> expenses = getExpenseListForMonth(userId, year, month);

        return MonthlySummary.builder()
                .year(year).month(month)
                .totalIncome(totalIncome).totalExpense(totalExpense)
                .netSavings(netSavings).savingsRate(Math.round(savingsRate * 100.0) / 100.0)
                .budgetUtilization(budgetUtil)
                .transactionCount(expenses.size())
                .build();
    }

    @Override
    public List<CategoryExpense> getCategoryExpenses(int userId, int year, int month) {
        List<Map<String, Object>> expenses = getExpenseListForMonth(userId, year, month);

        // Group by categoryId and sum amounts
        Map<Integer, Double> categoryTotals = new HashMap<>();
        Map<Integer, Integer> categoryCounts = new HashMap<>();

        for (Map<String, Object> expense : expenses) {
            int categoryId = ((Number) expense.getOrDefault("categoryId", 0)).intValue();
            double amount  = ((Number) expense.getOrDefault("amount", 0)).doubleValue();
            categoryTotals.merge(categoryId, amount, Double::sum);
            categoryCounts.merge(categoryId, 1, Integer::sum);
        }

        double grandTotal = categoryTotals.values().stream().mapToDouble(Double::doubleValue).sum();

        return categoryTotals.entrySet().stream()
                .map(entry -> CategoryExpense.builder()
                        .categoryId(entry.getKey())
                        .categoryName("Category-" + entry.getKey())
                        .amount(entry.getValue())
                        .percentage(grandTotal > 0 ? (entry.getValue() / grandTotal) * 100 : 0)
                        .transactionCount(categoryCounts.getOrDefault(entry.getKey(), 0))
                        .build())
                .sorted((a, b) -> Double.compare(b.getAmount(), a.getAmount()))
                .collect(Collectors.toList());
    }

    @Override
    public List<MonthlyTrend> getMonthlyTrend(int userId, int months) {
        List<MonthlyTrend> trends = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = months - 1; i >= 0; i--) {
            LocalDate date  = today.minusMonths(i);
            String monthKey = date.format(DateTimeFormatter.ofPattern("yyyy-MM"));

            double income  = getIncomeTotal(userId, date.getYear(), date.getMonthValue());
            double expense = getExpenseTotal(userId, date.getYear(), date.getMonthValue());

            trends.add(MonthlyTrend.builder()
                    .month(monthKey).income(income)
                    .expense(expense).savings(income - expense)
                    .build());
        }
        return trends;
    }

    @Override
    public List<CategoryExpense> getTopSpendingCategories(int userId, int limit) {
        LocalDate now = LocalDate.now();
        return getCategoryExpenses(userId, now.getYear(), now.getMonthValue())
                .stream().limit(limit).collect(Collectors.toList());
    }

    @Override
    public FinancialHealthScore getFinancialHealthScore(int userId) {
        LocalDate now  = LocalDate.now();
        int year  = now.getYear();
        int month = now.getMonthValue();

        double savingsRate      = getSavingsRate(userId, year, month);
        double budgetAdherence  = getBudgetAdherence(userId, year, month);
        MonthlySummary summary  = getMonthlySummary(userId, year, month);

        double expenseToIncomeRatio = summary.getTotalIncome() > 0
                ? (summary.getTotalExpense() / summary.getTotalIncome()) * 100 : 100;

        double savingsRateScore = Math.min(savingsRate * 2.5, 100);
        double expenseScore     = Math.max(0, 100 - expenseToIncomeRatio);
        double overallScore     = (savingsRateScore * 0.4) + (budgetAdherence * 0.4) + (expenseScore * 0.2);

        String status; String recommendation;
        if (overallScore >= 80)      { status = "EXCELLENT"; recommendation = "Great job! Keep maintaining your financial discipline."; }
        else if (overallScore >= 60) { status = "GOOD";      recommendation = "You're doing well. Consider increasing your savings rate."; }
        else if (overallScore >= 40) { status = "FAIR";      recommendation = "Review your spending habits and stick to your budget."; }
        else                         { status = "POOR";      recommendation = "Urgent: Reduce expenses and create a strict budget plan."; }

        return FinancialHealthScore.builder()
                .overallScore(Math.round(overallScore * 100.0) / 100.0)
                .savingsRateScore(Math.round(savingsRateScore * 100.0) / 100.0)
                .budgetAdherenceScore(Math.round(budgetAdherence * 100.0) / 100.0)
                .expenseToIncomeScore(Math.round(expenseScore * 100.0) / 100.0)
                .healthStatus(status).recommendation(recommendation)
                .build();
    }

    @Override
    public CashFlowSummary getCashFlowSummary(int userId, LocalDate startDate, LocalDate endDate) {
        long months = java.time.temporal.ChronoUnit.MONTHS.between(startDate, endDate) + 1;
        double totalInflow = 0.0, totalOutflow = 0.0;

        for (int i = 0; i < months; i++) {
            LocalDate d = startDate.plusMonths(i);
            totalInflow  += getIncomeTotal(userId, d.getYear(), d.getMonthValue());
            totalOutflow += getExpenseTotal(userId, d.getYear(), d.getMonthValue());
        }

        return CashFlowSummary.builder()
                .totalInflow(totalInflow).totalOutflow(totalOutflow)
                .netCashFlow(totalInflow - totalOutflow)
                .averageMonthlyIncome(months > 0 ? totalInflow / months : 0)
                .averageMonthlyExpense(months > 0 ? totalOutflow / months : 0)
                .build();
    }

    @Override
    public List<SpendingForecast> getSpendingForecast(int userId, int monthsAhead) {
        List<MonthlyTrend> historical = getMonthlyTrend(userId, 3);
        double avgExpense = historical.stream().mapToDouble(MonthlyTrend::getExpense).average().orElse(0);
        double avgIncome  = historical.stream().mapToDouble(MonthlyTrend::getIncome).average().orElse(0);

        List<SpendingForecast> forecasts = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 1; i <= monthsAhead; i++) {
            String monthKey  = today.plusMonths(i).format(DateTimeFormatter.ofPattern("yyyy-MM"));
            double trendFactor = 1 + (i * 0.02);
            forecasts.add(SpendingForecast.builder()
                    .forecastMonth(monthKey)
                    .predictedExpense(avgExpense * trendFactor)
                    .predictedIncome(avgIncome * 1.01)
                    .predictedSavings((avgIncome * 1.01) - (avgExpense * trendFactor))
                    .confidenceLevel(Math.max(60, 90 - (i * 10)))
                    .build());
        }
        return forecasts;
    }

    @Override
    public double getSavingsRate(int userId, int year, int month) {
        return getMonthlySummary(userId, year, month).getSavingsRate();
    }

    @Override
    public double getBudgetAdherence(int userId, int year, int month) {
        List<Map<String, Object>> budgets = getActiveBudgets(userId);
        if (budgets.isEmpty()) return 100.0;

        double totalAdherence = 0.0;
        for (Map<String, Object> budget : budgets) {
            double limitAmount = ((Number) budget.getOrDefault("limitAmount", 1)).doubleValue();
            double spentAmount = ((Number) budget.getOrDefault("spentAmount", 0)).doubleValue();
            double usagePercent = limitAmount > 0 ? (spentAmount / limitAmount) * 100 : 0;
            totalAdherence += Math.max(0, 100 - usagePercent);
        }
        return Math.round((totalAdherence / budgets.size()) * 100.0) / 100.0;
    }

    @Override
    public String exportTransactionsCSV(int userId, LocalDate startDate, LocalDate endDate) {
        try {
            // Fetch real expenses from expense-service
            List<Map<String, Object>> expenses = webClientBuilder.build()
                    .get()
                    .uri(expenseServiceUrl + "/expenses/user/{userId}/range?startDate={s}&endDate={e}",
                            userId, startDate, endDate)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .onErrorReturn(Collections.emptyList())
                    .block();

            // Fetch real incomes from income-service
            List<Map<String, Object>> incomes = webClientBuilder.build()
                    .get()
                    .uri(incomeServiceUrl + "/incomes/user/{userId}/range?startDate={s}&endDate={e}",
                            userId, startDate, endDate)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .onErrorReturn(Collections.emptyList())
                    .block();

            StringWriter writer = new StringWriter();
            CSVPrinter csv = new CSVPrinter(writer, CSVFormat.DEFAULT
                    .withHeader("Date", "Type", "Title", "Amount", "Currency", "Payment Method", "Notes"));

            if (expenses != null) {
                for (Map<String, Object> e : expenses) {
                    csv.printRecord(
                            e.get("date"), "EXPENSE", e.get("title"),
                            e.get("amount"), e.getOrDefault("currency", "INR"),
                            e.getOrDefault("paymentMethod", ""), e.getOrDefault("notes", ""));
                }
            }
            if (incomes != null) {
                for (Map<String, Object> i : incomes) {
                    csv.printRecord(
                            i.get("date"), "INCOME", i.get("title"),
                            i.get("amount"), i.getOrDefault("currency", "INR"),
                            i.getOrDefault("source", ""), i.getOrDefault("notes", ""));
                }
            }

            csv.flush();
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate CSV: " + e.getMessage());
        }
    }

    @Override
    public byte[] generateMonthlyReportPDF(int userId, int year, int month) {
        MonthlySummary summary = getMonthlySummary(userId, year, month);
        String content = String.format(
                "SpendSmart Monthly Report\nUser: %d | Period: %d-%02d\n\n" +
                        "Total Income:      ₹%.2f\n" +
                        "Total Expense:     ₹%.2f\n" +
                        "Net Savings:       ₹%.2f\n" +
                        "Savings Rate:      %.2f%%\n" +
                        "Budget Adherence:  %.2f%%\n" +
                        "Transactions:      %d",
                userId, year, month,
                summary.getTotalIncome(), summary.getTotalExpense(),
                summary.getNetSavings(), summary.getSavingsRate(),
                summary.getBudgetUtilization(), summary.getTransactionCount());
        return content.getBytes();
    }

    @Override
    public void sendMonthlySummaryEmail(int userId, int year, int month) {
        MonthlySummary summary = getMonthlySummary(userId, year, month);
        String subject = String.format("Your Monthly Financial Summary - %d-%02d", year, month);
        String body = String.format(
                "Hello,\n\nHere's your financial summary for %d-%02d:\n\n" +
                        "Total Income:    ₹%.2f\nTotal Expense:   ₹%.2f\n" +
                        "Net Savings:     ₹%.2f\nSavings Rate:    %.2f%%\n\n" +
                        "Keep up the good work!\n\nSpendSmart Team",
                year, month, summary.getTotalIncome(), summary.getTotalExpense(),
                summary.getNetSavings(), summary.getSavingsRate());
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("user-" + userId + "@spendsmart.com");
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Monthly summary email sent for userId={}", userId);
        } catch (Exception e) {
            log.error("Failed to send email for userId={}: {}", userId, e.getMessage());
        }
    }

    private double calculateBudgetUtilization(int userId, int year, int month) {
        List<Map<String, Object>> budgets = getActiveBudgets(userId);
        if (budgets.isEmpty()) return 0.0;

        double totalUtil = 0.0;
        for (Map<String, Object> b : budgets) {
            double limit = ((Number) b.getOrDefault("limitAmount", 1)).doubleValue();
            double spent = ((Number) b.getOrDefault("spentAmount", 0)).doubleValue();
            totalUtil += limit > 0 ? (spent / limit) * 100 : 0;
        }
        return Math.round((totalUtil / budgets.size()) * 100.0) / 100.0;
    }
}