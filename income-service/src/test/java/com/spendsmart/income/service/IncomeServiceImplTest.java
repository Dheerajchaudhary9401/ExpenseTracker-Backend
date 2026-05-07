package com.spendsmart.income.service;

import com.spendsmart.income.dto.IncomeRequest;
import com.spendsmart.income.entity.Income;
import com.spendsmart.income.entity.Income.IncomeSource;
import com.spendsmart.income.entity.Income.RecurrencePeriod;
import com.spendsmart.income.repository.IncomeRepository;
import com.spendsmart.income.service.impl.IncomeServiceImpl;
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
@DisplayName("IncomeServiceImpl - Unit Tests")
class IncomeServiceImplTest {

    @Mock private IncomeRepository incomeRepository;
    @InjectMocks private IncomeServiceImpl incomeService;

    private Income sampleIncome;
    private IncomeRequest incomeRequest;

    @BeforeEach
    void setUp() {
        sampleIncome = Income.builder()
                .incomeId(1).userId(10).categoryId(3)
                .title("Monthly Salary").amount(50000.0).currency("INR")
                .source(IncomeSource.SALARY).date(LocalDate.of(2026, 5, 1))
                .notes("May salary").isRecurring(true)
                .recurrencePeriod(RecurrencePeriod.MONTHLY).build();

        incomeRequest = new IncomeRequest();
        incomeRequest.setTitle("Monthly Salary"); incomeRequest.setAmount(50000.0);
        incomeRequest.setCategoryId(3); incomeRequest.setSource(IncomeSource.SALARY);
        incomeRequest.setDate(LocalDate.of(2026, 5, 1)); incomeRequest.setNotes("May salary");
        incomeRequest.setCurrency("INR"); incomeRequest.setRecurring(true);
        incomeRequest.setRecurrencePeriod(RecurrencePeriod.MONTHLY);
    }

    @Test @DisplayName("addIncome() - Should save and return income")
    void addIncome_ValidRequest_SavesAndReturns() {
        when(incomeRepository.save(any(Income.class))).thenReturn(sampleIncome);
        Income result = incomeService.addIncome(10, incomeRequest);
        assertNotNull(result);
        assertEquals("Monthly Salary", result.getTitle());
        assertEquals(50000.0, result.getAmount());
        assertEquals(10, result.getUserId());
        verify(incomeRepository, times(1)).save(any(Income.class));
    }

    @Test @DisplayName("addIncome() - Should map all fields correctly")
    void addIncome_MapsAllFields() {
        when(incomeRepository.save(any(Income.class))).thenReturn(sampleIncome);
        Income result = incomeService.addIncome(10, incomeRequest);
        assertEquals(IncomeSource.SALARY, result.getSource());
        assertTrue(result.isRecurring());
        assertEquals(RecurrencePeriod.MONTHLY, result.getRecurrencePeriod());
    }

    @Test @DisplayName("getIncomeById() - Should return income when found")
    void getIncomeById_Found() {
        when(incomeRepository.findByIncomeId(1)).thenReturn(Optional.of(sampleIncome));
        Income result = incomeService.getIncomeById(1);
        assertEquals(1, result.getIncomeId());
        assertEquals("Monthly Salary", result.getTitle());
    }

    @Test @DisplayName("getIncomeById() - Should throw when not found")
    void getIncomeById_NotFound_Throws() {
        when(incomeRepository.findByIncomeId(999)).thenReturn(Optional.empty());
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> incomeService.getIncomeById(999));
        assertEquals("Income not found", ex.getMessage());
    }

    @Test @DisplayName("getIncomesByUser() - Should return all incomes")
    void getIncomesByUser_ReturnsAll() {
        when(incomeRepository.findByUserId(10)).thenReturn(Arrays.asList(sampleIncome, sampleIncome));
        assertEquals(2, incomeService.getIncomesByUser(10).size());
    }

    @Test @DisplayName("getIncomesBySource() - Should filter by source")
    void getIncomesBySource_ReturnsFiltered() {
        when(incomeRepository.findByUserIdAndSource(10, IncomeSource.SALARY))
                .thenReturn(List.of(sampleIncome));
        List<Income> result = incomeService.getIncomesBySource(10, IncomeSource.SALARY);
        assertEquals(1, result.size());
        assertEquals(IncomeSource.SALARY, result.get(0).getSource());
    }

    @Test @DisplayName("getIncomesByMonth() - Should use correct date range")
    void getIncomesByMonth_CorrectDateRange() {
        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end   = LocalDate.of(2026, 5, 31);
        when(incomeRepository.findByUserIdAndDateBetween(10, start, end)).thenReturn(List.of(sampleIncome));
        List<Income> result = incomeService.getIncomesByMonth(10, 2026, 5);
        assertEquals(1, result.size());
        verify(incomeRepository).findByUserIdAndDateBetween(10, start, end);
    }

    @Test @DisplayName("getRecurringIncomes() - Should return only recurring incomes")
    void getRecurringIncomes_ReturnsRecurring() {
        when(incomeRepository.findByUserIdAndIsRecurring(10, true)).thenReturn(List.of(sampleIncome));
        List<Income> result = incomeService.getRecurringIncomes(10);
        assertEquals(1, result.size());
        assertTrue(result.get(0).isRecurring());
    }

    @Test @DisplayName("updateIncome() - Should update non-null fields")
    void updateIncome_UpdatesFields() {
        IncomeRequest req = new IncomeRequest();
        req.setTitle("Bonus"); req.setAmount(75000.0);
        when(incomeRepository.findByIncomeId(1)).thenReturn(Optional.of(sampleIncome));
        when(incomeRepository.save(any(Income.class))).thenReturn(sampleIncome);
        incomeService.updateIncome(1, req);
        assertEquals("Bonus", sampleIncome.getTitle());
        assertEquals(75000.0, sampleIncome.getAmount());
        verify(incomeRepository).save(sampleIncome);
    }

    @Test @DisplayName("deleteIncome() - Should call deleteByIncomeId")
    void deleteIncome_CallsRepository() {
        doNothing().when(incomeRepository).deleteByIncomeId(1);
        incomeService.deleteIncome(1);
        verify(incomeRepository, times(1)).deleteByIncomeId(1);
    }

    @Test @DisplayName("getTotalIncomeByUser() - Should return total")
    void getTotalIncomeByUser_ReturnsTotal() {
        when(incomeRepository.sumAmountByUserId(10)).thenReturn(50000.0);
        assertEquals(50000.0, incomeService.getTotalIncomeByUser(10));
    }

    @Test @DisplayName("getTotalIncomeByUser() - Should return 0.0 when null")
    void getTotalIncomeByUser_Null_ReturnsZero() {
        when(incomeRepository.sumAmountByUserId(10)).thenReturn(null);
        assertEquals(0.0, incomeService.getTotalIncomeByUser(10));
    }

    @Test @DisplayName("getTotalIncomeByMonth() - Should return monthly total")
    void getTotalIncomeByMonth_ReturnsTotal() {
        when(incomeRepository.sumAmountByUserIdAndMonth(10, 2026, 5)).thenReturn(50000.0);
        assertEquals(50000.0, incomeService.getTotalIncomeByMonth(10, 2026, 5));
    }

    @Test @DisplayName("getTotalIncomeByMonth() - Should return 0.0 when null")
    void getTotalIncomeByMonth_Null_ReturnsZero() {
        when(incomeRepository.sumAmountByUserIdAndMonth(10, 2026, 5)).thenReturn(null);
        assertEquals(0.0, incomeService.getTotalIncomeByMonth(10, 2026, 5));
    }

    @Test @DisplayName("searchIncomes() - Should return matching results")
    void searchIncomes_ReturnsMatches() {
        when(incomeRepository.searchByKeyword(10, "salary")).thenReturn(List.of(sampleIncome));
        assertEquals(1, incomeService.searchIncomes(10, "salary").size());
    }

    @Test @DisplayName("searchIncomes() - Should return empty list for no match")
    void searchIncomes_NoMatch() {
        when(incomeRepository.searchByKeyword(10, "xyz")).thenReturn(Collections.emptyList());
        assertTrue(incomeService.searchIncomes(10, "xyz").isEmpty());
    }
}