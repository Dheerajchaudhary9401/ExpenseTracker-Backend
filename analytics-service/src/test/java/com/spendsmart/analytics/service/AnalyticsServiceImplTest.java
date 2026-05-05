package com.spendsmart.analytics.service;

import com.spendsmart.analytics.model.*;
import com.spendsmart.analytics.service.impl.AnalyticsServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsServiceImpl - Unit Tests")
class AnalyticsServiceImplTest {

    @Mock private JavaMailSender mailSender;
    @InjectMocks private AnalyticsServiceImpl analyticsService;

    @Test @DisplayName("getMonthlySummary() - Should return summary with all fields populated")
    void getMonthlySummary_ReturnsPopulatedSummary() {
        MonthlySummary result = analyticsService.getMonthlySummary(1, 2026, 5);
        assertNotNull(result);
        assertEquals(2026, result.getYear());
        assertEquals(5, result.getMonth());
        assertTrue(result.getTotalIncome() > 0);
        assertTrue(result.getTotalExpense() > 0);
    }

    @Test @DisplayName("getMonthlySummary() - Net savings should be income minus expense")
    void getMonthlySummary_NetSavings_CorrectCalculation() {
        MonthlySummary result = analyticsService.getMonthlySummary(1, 2026, 5);
        double expectedNet = result.getTotalIncome() - result.getTotalExpense();
        assertEquals(expectedNet, result.getNetSavings(), 0.01);
    }

    @Test @DisplayName("getMonthlySummary() - Savings rate should be between 0 and 100")
    void getMonthlySummary_SavingsRate_ValidRange() {
        MonthlySummary result = analyticsService.getMonthlySummary(1, 2026, 5);
        assertTrue(result.getSavingsRate() >= 0 && result.getSavingsRate() <= 100);
    }

    @Test @DisplayName("getCategoryExpenses() - Should return 5 categories")
    void getCategoryExpenses_ReturnsFiveCategories() {
        List<CategoryExpense> result = analyticsService.getCategoryExpenses(1, 2026, 5);
        assertNotNull(result);
        assertEquals(5, result.size());
    }

    @Test @DisplayName("getCategoryExpenses() - All amounts should be positive")
    void getCategoryExpenses_AllAmountsPositive() {
        List<CategoryExpense> result = analyticsService.getCategoryExpenses(1, 2026, 5);
        assertTrue(result.stream().allMatch(c -> c.getAmount() > 0));
    }

    @Test @DisplayName("getCategoryExpenses() - All percentages should sum to ~100")
    void getCategoryExpenses_PercentagesSumToHundred() {
        List<CategoryExpense> result = analyticsService.getCategoryExpenses(1, 2026, 5);
        double totalPercentage = result.stream().mapToDouble(CategoryExpense::getPercentage).sum();
        assertEquals(100.0, totalPercentage, 1.0); // allow 1% tolerance
    }

    @Test @DisplayName("getMonthlyTrend() - Should return correct number of months")
    void getMonthlyTrend_ReturnsCorrectCount() {
        List<MonthlyTrend> result = analyticsService.getMonthlyTrend(1, 6);
        assertEquals(6, result.size());
    }

    @Test @DisplayName("getMonthlyTrend() - Each month should have income, expense, savings")
    void getMonthlyTrend_AllFieldsPopulated() {
        List<MonthlyTrend> result = analyticsService.getMonthlyTrend(1, 3);
        result.forEach(trend -> {
            assertNotNull(trend.getMonth());
            assertTrue(trend.getIncome() > 0);
            assertTrue(trend.getExpense() > 0);
        });
    }

    @Test @DisplayName("getTopSpendingCategories() - Should return at most 'limit' categories")
    void getTopSpendingCategories_RespectsLimit() {
        List<CategoryExpense> result = analyticsService.getTopSpendingCategories(1, 3);
        assertTrue(result.size() <= 3);
    }

    @Test @DisplayName("getTopSpendingCategories() - Should be sorted descending by amount")
    void getTopSpendingCategories_SortedDescending() {
        List<CategoryExpense> result = analyticsService.getTopSpendingCategories(1, 5);
        for (int i = 0; i < result.size() - 1; i++) {
            assertTrue(result.get(i).getAmount() >= result.get(i + 1).getAmount());
        }
    }

    @Test @DisplayName("getFinancialHealthScore() - Should return score between 0 and 100")
    void getFinancialHealthScore_ValidRange() {
        FinancialHealthScore result = analyticsService.getFinancialHealthScore(1);
        assertNotNull(result);
        assertTrue(result.getOverallScore() >= 0 && result.getOverallScore() <= 100);
    }

    @Test @DisplayName("getFinancialHealthScore() - Should return valid health status")
    void getFinancialHealthScore_ValidStatus() {
        FinancialHealthScore result = analyticsService.getFinancialHealthScore(1);
        List<String> validStatuses = List.of("EXCELLENT", "GOOD", "FAIR", "POOR");
        assertTrue(validStatuses.contains(result.getHealthStatus()));
    }

    @Test @DisplayName("getFinancialHealthScore() - Should always have a recommendation")
    void getFinancialHealthScore_HasRecommendation() {
        FinancialHealthScore result = analyticsService.getFinancialHealthScore(1);
        assertNotNull(result.getRecommendation());
        assertFalse(result.getRecommendation().isEmpty());
    }

    @Test @DisplayName("getCashFlowSummary() - Net should be inflow minus outflow")
    void getCashFlowSummary_NetCashFlow_Correct() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end   = LocalDate.of(2026, 3, 31);
        CashFlowSummary result = analyticsService.getCashFlowSummary(1, start, end);
        assertNotNull(result);
        assertEquals(result.getTotalInflow() - result.getTotalOutflow(), result.getNetCashFlow(), 0.01);
    }

    @Test @DisplayName("getSpendingForecast() - Should return correct number of months ahead")
    void getSpendingForecast_ReturnsCorrectCount() {
        List<SpendingForecast> result = analyticsService.getSpendingForecast(1, 3);
        assertEquals(3, result.size());
    }

    @Test @DisplayName("getSpendingForecast() - Confidence level should be between 0 and 100")
    void getSpendingForecast_ValidConfidence() {
        List<SpendingForecast> result = analyticsService.getSpendingForecast(1, 3);
        result.forEach(f -> assertTrue(f.getConfidenceLevel() >= 0 && f.getConfidenceLevel() <= 100));
    }

    @Test @DisplayName("exportTransactionsCSV() - Should return non-empty CSV string")
    void exportTransactionsCSV_ReturnsNonEmpty() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end   = LocalDate.of(2026, 5, 31);
        String csv = analyticsService.exportTransactionsCSV(1, start, end);
        assertNotNull(csv);
        assertFalse(csv.isEmpty());
        assertTrue(csv.contains("Date"));     // header row present
        assertTrue(csv.contains("Amount"));
    }

    @Test @DisplayName("generateMonthlyReportPDF() - Should return non-empty byte array")
    void generateMonthlyReportPDF_ReturnsByteArray() {
        byte[] pdf = analyticsService.generateMonthlyReportPDF(1, 2026, 5);
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test @DisplayName("sendMonthlySummaryEmail() - Should call mailSender.send()")
    void sendMonthlySummaryEmail_CallsMailSender() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        assertDoesNotThrow(() -> analyticsService.sendMonthlySummaryEmail(1, 2026, 5));
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test @DisplayName("getSavingsRate() - Should return value between 0 and 100")
    void getSavingsRate_ValidRange() {
        double rate = analyticsService.getSavingsRate(1, 2026, 5);
        assertTrue(rate >= 0 && rate <= 100);
    }

    @Test @DisplayName("getBudgetAdherence() - Should return valid percentage")
    void getBudgetAdherence_ReturnsValidValue() {
        double adherence = analyticsService.getBudgetAdherence(1, 2026, 5);
        assertTrue(adherence >= 0 && adherence <= 100);
    }
}