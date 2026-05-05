package com.spendsmart.expense.service;

import com.spendsmart.expense.dto.ExpenseRequest;
import com.spendsmart.expense.entity.Expense;
import com.spendsmart.expense.entity.Expense.ExpenseType;
import com.spendsmart.expense.entity.Expense.PaymentMethod;
import com.spendsmart.expense.repository.ExpenseRepository;
import com.spendsmart.expense.service.impl.ExpenseServiceImpl;
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
@DisplayName("ExpenseServiceImpl - Unit Tests")
class ExpenseServiceImplTest {

    @Mock private ExpenseRepository expenseRepository;
    @InjectMocks private ExpenseServiceImpl expenseService;

    private Expense sampleExpense;
    private ExpenseRequest expenseRequest;

    @BeforeEach
    void setUp() {
        sampleExpense = Expense.builder()
                .expenseId(1).userId(10).categoryId(2)
                .title("Lunch at Cafe").amount(250.0).currency("INR")
                .type(ExpenseType.EXPENSE).paymentMethod(PaymentMethod.UPI)
                .date(LocalDate.of(2026, 5, 1)).notes("Team lunch").isRecurring(false).build();

        expenseRequest = new ExpenseRequest();
        expenseRequest.setCategoryId(2); expenseRequest.setTitle("Lunch at Cafe");
        expenseRequest.setAmount(250.0); expenseRequest.setCurrency("INR");
        expenseRequest.setType(ExpenseType.EXPENSE); expenseRequest.setPaymentMethod(PaymentMethod.UPI);
        expenseRequest.setDate(LocalDate.of(2026, 5, 1)); expenseRequest.setNotes("Team lunch");
    }

    @Test @DisplayName("addExpense() - Should save and return expense")
    void addExpense_ValidRequest_SavesAndReturns() {
        when(expenseRepository.save(any(Expense.class))).thenReturn(sampleExpense);
        Expense result = expenseService.addExpense(10, expenseRequest);
        assertNotNull(result);
        assertEquals("Lunch at Cafe", result.getTitle());
        assertEquals(10, result.getUserId());
        verify(expenseRepository, times(1)).save(any(Expense.class));
    }

    @Test @DisplayName("getExpenseById() - Should return expense when found")
    void getExpenseById_Found() {
        when(expenseRepository.findByExpenseId(1)).thenReturn(Optional.of(sampleExpense));
        Expense result = expenseService.getExpenseById(1);
        assertEquals(1, result.getExpenseId());
    }

    @Test @DisplayName("getExpenseById() - Should throw exception when not found")
    void getExpenseById_NotFound_Throws() {
        when(expenseRepository.findByExpenseId(999)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class, () -> expenseService.getExpenseById(999));
        assertEquals("Expense not found", ex.getMessage());
    }

    @Test @DisplayName("getExpensesByUser() - Should return all expenses for user")
    void getExpensesByUser_ReturnsAll() {
        when(expenseRepository.findByUserId(10)).thenReturn(Arrays.asList(sampleExpense, sampleExpense));
        assertEquals(2, expenseService.getExpensesByUser(10).size());
    }

    @Test @DisplayName("getExpensesByUser() - Should return empty list when no expenses")
    void getExpensesByUser_Empty() {
        when(expenseRepository.findByUserId(10)).thenReturn(Collections.emptyList());
        assertTrue(expenseService.getExpensesByUser(10).isEmpty());
    }

    @Test @DisplayName("getExpensesByMonth() - Should use correct date range for May 2026")
    void getExpensesByMonth_CorrectDateRange() {
        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end   = LocalDate.of(2026, 5, 31);
        when(expenseRepository.findByUserIdAndDateBetween(10, start, end)).thenReturn(List.of(sampleExpense));
        List<Expense> result = expenseService.getExpensesByMonth(10, 2026, 5);
        assertEquals(1, result.size());
        verify(expenseRepository).findByUserIdAndDateBetween(10, start, end);
    }

    @Test @DisplayName("getExpensesByMonth() - Should handle February correctly")
    void getExpensesByMonth_February_CorrectEndDate() {
        LocalDate start = LocalDate.of(2026, 2, 1);
        LocalDate end   = LocalDate.of(2026, 2, 28);
        when(expenseRepository.findByUserIdAndDateBetween(10, start, end)).thenReturn(Collections.emptyList());
        expenseService.getExpensesByMonth(10, 2026, 2);
        verify(expenseRepository).findByUserIdAndDateBetween(10, start, end);
    }

    @Test @DisplayName("getExpensesByType() - Should filter by expense type")
    void getExpensesByType_ReturnsFiltered() {
        when(expenseRepository.findByUserIdAndType(10, ExpenseType.EXPENSE)).thenReturn(List.of(sampleExpense));
        List<Expense> result = expenseService.getExpensesByType(10, ExpenseType.EXPENSE);
        assertEquals(1, result.size());
    }

    @Test @DisplayName("getExpensesByCategory() - Should return expenses for category")
    void getExpensesByCategory_ReturnsExpenses() {
        when(expenseRepository.findByCategoryId(2)).thenReturn(List.of(sampleExpense));
        assertEquals(1, expenseService.getExpensesByCategory(2).size());
    }

    @Test @DisplayName("updateExpense() - Should update non-null fields")
    void updateExpense_UpdatesFields() {
        ExpenseRequest req = new ExpenseRequest();
        req.setTitle("Dinner"); req.setAmount(500.0);
        when(expenseRepository.findByExpenseId(1)).thenReturn(Optional.of(sampleExpense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(sampleExpense);
        expenseService.updateExpense(1, req);
        assertEquals("Dinner", sampleExpense.getTitle());
        assertEquals(500.0, sampleExpense.getAmount());
        verify(expenseRepository).save(sampleExpense);
    }

    @Test @DisplayName("updateExpense() - Should not update null fields")
    void updateExpense_NullFields_Unchanged() {
        ExpenseRequest req = new ExpenseRequest();
        req.setTitle(null); req.setAmount(null);
        when(expenseRepository.findByExpenseId(1)).thenReturn(Optional.of(sampleExpense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(sampleExpense);
        expenseService.updateExpense(1, req);
        assertEquals("Lunch at Cafe", sampleExpense.getTitle());
        assertEquals(250.0, sampleExpense.getAmount());
    }

    @Test @DisplayName("deleteExpense() - Should call repository deleteByExpenseId")
    void deleteExpense_CallsRepository() {
        doNothing().when(expenseRepository).deleteByExpenseId(1);
        expenseService.deleteExpense(1);
        verify(expenseRepository, times(1)).deleteByExpenseId(1);
    }

    @Test @DisplayName("getTotalByUser() - Should return total from repository")
    void getTotalByUser_ReturnsTotal() {
        when(expenseRepository.sumAmountByUserId(10)).thenReturn(1500.0);
        assertEquals(1500.0, expenseService.getTotalByUser(10));
    }

    @Test @DisplayName("getTotalByUser() - Should return 0.0 when null from repository")
    void getTotalByUser_Null_ReturnsZero() {
        when(expenseRepository.sumAmountByUserId(10)).thenReturn(null);
        assertEquals(0.0, expenseService.getTotalByUser(10));
    }

    @Test @DisplayName("getTotalByCategory() - Should return total for date range")
    void getTotalByCategory_ReturnsTotal() {
        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end   = LocalDate.of(2026, 5, 31);
        when(expenseRepository.sumAmountByCategoryAndDateBetween(2, start, end)).thenReturn(750.0);
        assertEquals(750.0, expenseService.getTotalByCategory(2, start, end));
    }

    @Test @DisplayName("getTotalByCategory() - Should return 0.0 when null")
    void getTotalByCategory_Null_ReturnsZero() {
        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end   = LocalDate.of(2026, 5, 31);
        when(expenseRepository.sumAmountByCategoryAndDateBetween(2, start, end)).thenReturn(null);
        assertEquals(0.0, expenseService.getTotalByCategory(2, start, end));
    }

    @Test @DisplayName("searchExpenses() - Should return matching results")
    void searchExpenses_ReturnsMatches() {
        when(expenseRepository.searchByKeyword(10, "lunch")).thenReturn(List.of(sampleExpense));
        assertEquals(1, expenseService.searchExpenses(10, "lunch").size());
    }

    @Test @DisplayName("searchExpenses() - Should return empty list for no match")
    void searchExpenses_NoMatch() {
        when(expenseRepository.searchByKeyword(10, "xyz")).thenReturn(Collections.emptyList());
        assertTrue(expenseService.searchExpenses(10, "xyz").isEmpty());
    }
}