package com.spendsmart.analytics.resource;

import com.spendsmart.analytics.model.*;
import com.spendsmart.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsResource {

    private final AnalyticsService analyticsService;

    @GetMapping("/user/{userId}/summary/{year}/{month}")
    public ResponseEntity<MonthlySummary> getMonthlySummary(
            @PathVariable int userId,
            @PathVariable int year,
            @PathVariable int month) {
        return ResponseEntity.ok(analyticsService.getMonthlySummary(userId, year, month));
    }

    @GetMapping("/user/{userId}/category-expenses/{year}/{month}")
    public ResponseEntity<List<CategoryExpense>> getCategoryExpenses(
            @PathVariable int userId,
            @PathVariable int year,
            @PathVariable int month) {
        return ResponseEntity.ok(analyticsService.getCategoryExpenses(userId, year, month));
    }

    @GetMapping("/user/{userId}/trend")
    public ResponseEntity<List<MonthlyTrend>> getMonthlyTrend(
            @PathVariable int userId,
            @RequestParam(defaultValue = "12") int months) {
        return ResponseEntity.ok(analyticsService.getMonthlyTrend(userId, months));
    }

    @GetMapping("/user/{userId}/top-categories")
    public ResponseEntity<List<CategoryExpense>> getTopSpending(
            @PathVariable int userId,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(analyticsService.getTopSpendingCategories(userId, limit));
    }

    @GetMapping("/user/{userId}/health-score")
    public ResponseEntity<FinancialHealthScore> getHealthScore(@PathVariable int userId) {
        return ResponseEntity.ok(analyticsService.getFinancialHealthScore(userId));
    }

    @GetMapping("/user/{userId}/cash-flow")
    public ResponseEntity<CashFlowSummary> getCashFlow(
            @PathVariable int userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(analyticsService.getCashFlowSummary(userId, startDate, endDate));
    }

    @GetMapping("/user/{userId}/forecast")
    public ResponseEntity<List<SpendingForecast>> getForecast(
            @PathVariable int userId,
            @RequestParam(defaultValue = "3") int monthsAhead) {
        return ResponseEntity.ok(analyticsService.getSpendingForecast(userId, monthsAhead));
    }

    @GetMapping("/user/{userId}/savings-rate/{year}/{month}")
    public ResponseEntity<Double> getSavingsRate(
            @PathVariable int userId,
            @PathVariable int year,
            @PathVariable int month) {
        return ResponseEntity.ok(analyticsService.getSavingsRate(userId, year, month));
    }

    @GetMapping("/user/{userId}/budget-adherence/{year}/{month}")
    public ResponseEntity<Double> getBudgetAdherence(
            @PathVariable int userId,
            @PathVariable int year,
            @PathVariable int month) {
        return ResponseEntity.ok(analyticsService.getBudgetAdherence(userId, year, month));
    }

    @GetMapping("/user/{userId}/export/csv")
    public ResponseEntity<String> exportCSV(
            @PathVariable int userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        String csv = analyticsService.exportTransactionsCSV(userId, startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "transactions.csv");

        return ResponseEntity.ok()
                .headers(headers)
                .body(csv);
    }

    @GetMapping("/user/{userId}/export/pdf/{year}/{month}")
    public ResponseEntity<byte[]> exportPDF(
            @PathVariable int userId,
            @PathVariable int year,
            @PathVariable int month) {

        byte[] pdf = analyticsService.generateMonthlyReportPDF(userId, year, month);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "monthly-report.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }

    @PostMapping("/user/{userId}/send-summary/{year}/{month}")
    public ResponseEntity<String> sendMonthlySummary(
            @PathVariable int userId,
            @PathVariable int year,
            @PathVariable int month,
            @RequestParam String email) {
        analyticsService.sendMonthlySummaryEmail(userId, email, year, month); //added email here
        return ResponseEntity.ok("Monthly summary email sent successfully");
    }
}