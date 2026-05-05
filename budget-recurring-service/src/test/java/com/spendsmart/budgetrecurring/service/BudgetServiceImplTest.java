package com.spendsmart.budgetrecurring.service;

import com.spendsmart.budgetrecurring.dto.BudgetRequest;
import com.spendsmart.budgetrecurring.entity.Budget;
import com.spendsmart.budgetrecurring.entity.Budget.Period;
import com.spendsmart.budgetrecurring.repository.BudgetRepository;
import com.spendsmart.budgetrecurring.service.impl.BudgetServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BudgetServiceImpl - Unit Tests")
class BudgetServiceImplTest {

    @Mock private BudgetRepository budgetRepository;
    @Mock private NotificationService notificationService;
    @InjectMocks private BudgetServiceImpl budgetService;

    private Budget sampleBudget;
    private BudgetRequest budgetRequest;

    @BeforeEach
    void setUp() {
        sampleBudget = Budget.builder()
                .budgetId(1).userId(10).categoryId(2).name("Food Budget")
                .limitAmount(5000.0).currency("INR").period(Period.MONTHLY)
                .startDate(LocalDate.of(2026, 5, 1)).endDate(LocalDate.of(2026, 5, 31))
                .spentAmount(0.0).alertThreshold(80.0).isActive(true).build();

        budgetRequest = new BudgetRequest();
        budgetRequest.setName("Food Budget"); budgetRequest.setCategoryId(2);
        budgetRequest.setLimitAmount(5000.0); budgetRequest.setPeriod(Period.MONTHLY);
        budgetRequest.setStartDate(LocalDate.of(2026, 5, 1));
        budgetRequest.setEndDate(LocalDate.of(2026, 5, 31));
        budgetRequest.setAlertThreshold(80.0); budgetRequest.setCurrency("INR");
    }

    @Test @DisplayName("createBudget() - Should save and return budget")
    void createBudget_Success() {
        when(budgetRepository.save(any(Budget.class))).thenReturn(sampleBudget);
        Budget result = budgetService.createBudget(10, budgetRequest);
        assertNotNull(result);
        assertEquals("Food Budget", result.getName());
        assertEquals(10, result.getUserId());
        assertEquals(0.0, result.getSpentAmount());
        assertTrue(result.isActive());
        verify(budgetRepository, times(1)).save(any(Budget.class));
    }

    @Test @DisplayName("createBudget() - Should default startDate to today when null")
    void createBudget_NullStartDate_DefaultsToToday() {
        budgetRequest.setStartDate(null);
        when(budgetRepository.save(any(Budget.class))).thenReturn(sampleBudget);
        budgetService.createBudget(10, budgetRequest);
        verify(budgetRepository).save(argThat(b -> b.getStartDate() != null));
    }

    @Test @DisplayName("getBudgetById() - Should return budget when found")
    void getBudgetById_Found() {
        when(budgetRepository.findByBudgetId(1)).thenReturn(Optional.of(sampleBudget));
        Budget result = budgetService.getBudgetById(1);
        assertEquals(1, result.getBudgetId());
        assertEquals("Food Budget", result.getName());
    }

    @Test @DisplayName("getBudgetById() - Should throw when not found")
    void getBudgetById_NotFound_Throws() {
        when(budgetRepository.findByBudgetId(999)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> budgetService.getBudgetById(999));
        assertEquals("Budget not found", ex.getMessage());
    }

    @Test @DisplayName("getBudgetsByUser() - Should return all budgets for user")
    void getBudgetsByUser_ReturnsAll() {
        when(budgetRepository.findByUserId(10)).thenReturn(Arrays.asList(sampleBudget, sampleBudget));
        assertEquals(2, budgetService.getBudgetsByUser(10).size());
    }

    @Test @DisplayName("getActiveBudgets() - Should return only active budgets")
    void getActiveBudgets_ReturnsActive() {
        when(budgetRepository.findByUserIdAndIsActive(10, true)).thenReturn(List.of(sampleBudget));
        List<Budget> result = budgetService.getActiveBudgets(10);
        assertEquals(1, result.size());
        assertTrue(result.get(0).isActive());
    }

    @Test @DisplayName("updateBudget() - Should update non-null fields and save")
    void updateBudget_UpdatesFields() {
        BudgetRequest req = new BudgetRequest();
        req.setName("Updated Budget"); req.setLimitAmount(8000.0); req.setAlertThreshold(90.0);
        when(budgetRepository.findByBudgetId(1)).thenReturn(Optional.of(sampleBudget));
        when(budgetRepository.save(any(Budget.class))).thenReturn(sampleBudget);
        budgetService.updateBudget(1, req);
        assertEquals("Updated Budget", sampleBudget.getName());
        assertEquals(8000.0, sampleBudget.getLimitAmount());
        assertEquals(90.0, sampleBudget.getAlertThreshold());
        verify(budgetRepository).save(sampleBudget);
    }

    @Test @DisplayName("deleteBudget() - Should call deleteByBudgetId")
    void deleteBudget_CallsRepository() {
        doNothing().when(budgetRepository).deleteByBudgetId(1);
        budgetService.deleteBudget(1);
        verify(budgetRepository, times(1)).deleteByBudgetId(1);
    }

    @Test @DisplayName("updateSpentAmount() - Should add amount to spentAmount")
    void updateSpentAmount_AddsToSpent() {
        sampleBudget.setSpentAmount(1000.0);
        when(budgetRepository.findByBudgetId(1)).thenReturn(Optional.of(sampleBudget));
        when(budgetRepository.save(any(Budget.class))).thenReturn(sampleBudget);
        budgetService.updateSpentAmount(1, 500.0);
        assertEquals(1500.0, sampleBudget.getSpentAmount());
        verify(budgetRepository).save(sampleBudget);
    }

    @Test @DisplayName("updateSpentAmount() - Should send alert when threshold exceeded")
    void updateSpentAmount_ThresholdExceeded_SendsAlert() {
        // 80% of 5000 = 4000 — spending 4500 triggers alert
        sampleBudget.setSpentAmount(0.0);
        sampleBudget.setLimitAmount(5000.0);
        sampleBudget.setAlertThreshold(80.0);
        when(budgetRepository.findByBudgetId(1)).thenReturn(Optional.of(sampleBudget));
        when(budgetRepository.save(any(Budget.class))).thenReturn(sampleBudget);
        doNothing().when(notificationService).sendBudgetAlert(any(Budget.class));
        budgetService.updateSpentAmount(1, 4500.0);
        verify(notificationService, times(1)).sendBudgetAlert(sampleBudget);
    }

    @Test @DisplayName("updateSpentAmount() - Should NOT alert when threshold not exceeded")
    void updateSpentAmount_BelowThreshold_NoAlert() {
        sampleBudget.setSpentAmount(0.0);
        when(budgetRepository.findByBudgetId(1)).thenReturn(Optional.of(sampleBudget));
        when(budgetRepository.save(any(Budget.class))).thenReturn(sampleBudget);
        // 100 out of 5000 = 2% — well below 80% threshold
        budgetService.updateSpentAmount(1, 100.0);
        verify(notificationService, never()).sendBudgetAlert(any());
    }

    @Test @DisplayName("getBudgetProgress() - Should calculate correct percentage")
    void getBudgetProgress_ReturnsCorrectPercentage() {
        sampleBudget.setSpentAmount(2500.0); // 50% of 5000
        when(budgetRepository.findByBudgetId(1)).thenReturn(Optional.of(sampleBudget));
        double progress = budgetService.getBudgetProgress(1);
        assertEquals(50.0, progress);
    }

    @Test @DisplayName("getBudgetsByCategory() - Should filter by userId and categoryId")
    void getBudgetsByCategory_ReturnsFiltered() {
        when(budgetRepository.findByUserIdAndCategoryId(10, 2)).thenReturn(List.of(sampleBudget));
        List<Budget> result = budgetService.getBudgetsByCategory(10, 2);
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getCategoryId());
    }
}