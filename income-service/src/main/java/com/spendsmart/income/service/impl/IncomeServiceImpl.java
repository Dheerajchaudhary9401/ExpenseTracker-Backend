package com.spendsmart.income.service.impl;

import com.spendsmart.income.dto.IncomeRequest;
import com.spendsmart.income.entity.Income;
import com.spendsmart.income.entity.Income.IncomeSource;
import com.spendsmart.income.repository.IncomeRepository;
import com.spendsmart.income.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncomeServiceImpl implements IncomeService {

    private final IncomeRepository incomeRepository;

    @Override
    public Income addIncome(int userId, IncomeRequest request) {
        Income income = Income.builder()
                .userId(userId)
                .categoryId(request.getCategoryId())
                .title(request.getTitle())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .source(request.getSource())
                .date(request.getDate())
                .notes(request.getNotes())
                .isRecurring(request.isRecurring())
                .recurrencePeriod(request.getRecurrencePeriod())
                .build();

        return incomeRepository.save(income);
    }

    @Override
    public Income getIncomeById(int incomeId) {
        return incomeRepository.findByIncomeId(incomeId)
                .orElseThrow(() -> new RuntimeException("Income not found"));
    }

    @Override
    public List<Income> getIncomesByUser(int userId) {
        return incomeRepository.findByUserId(userId);
    }

    @Override
    public List<Income> getIncomesBySource(int userId, IncomeSource source) {
        return incomeRepository.findByUserIdAndSource(userId, source);
    }

    @Override
    public List<Income> getIncomesByDateRange(int userId, LocalDate startDate, LocalDate endDate) {
        return incomeRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
    }

    @Override
    public List<Income> getIncomesByMonth(int userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        return incomeRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
    }

    @Override
    public Income updateIncome(int incomeId, IncomeRequest request) {
        Income income = getIncomeById(incomeId);

        if (request.getTitle() != null) income.setTitle(request.getTitle());
        if (request.getAmount() != null) income.setAmount(request.getAmount());
        if (request.getCategoryId() != null) income.setCategoryId(request.getCategoryId());
        if (request.getSource() != null) income.setSource(request.getSource());
        if (request.getDate() != null) income.setDate(request.getDate());
        if (request.getNotes() != null) income.setNotes(request.getNotes());
        if (request.getRecurrencePeriod() != null) income.setRecurrencePeriod(request.getRecurrencePeriod());
        income.setRecurring(request.isRecurring());

        return incomeRepository.save(income);
    }

    @Override
    @Transactional
    public void deleteIncome(int incomeId) {
        incomeRepository.deleteByIncomeId(incomeId);
    }

    @Override
    public Double getTotalIncomeByUser(int userId) {
        Double total = incomeRepository.sumAmountByUserId(userId);
        return total != null ? total : 0.0;
    }

    @Override
    public Double getTotalIncomeByMonth(int userId, int year, int month) {
        Double total = incomeRepository.sumAmountByUserIdAndMonth(userId, year, month);
        return total != null ? total : 0.0;
    }

    @Override
    public List<Income> getRecurringIncomes(int userId) {
        return incomeRepository.findByUserIdAndIsRecurring(userId, true);
    }

    @Override
    public List<Income> searchIncomes(int userId, String keyword) {
        return incomeRepository.searchByKeyword(userId, keyword);
    }
}