package com.spendsmart.income.service;

import com.spendsmart.income.dto.IncomeRequest;
import com.spendsmart.income.entity.Income;
import com.spendsmart.income.entity.Income.IncomeSource;
import java.time.LocalDate;
import java.util.List;

public interface IncomeService {

    Income addIncome(int userId, IncomeRequest request);

    Income getIncomeById(int incomeId);

    List<Income> getIncomesByUser(int userId);

    List<Income> getIncomesBySource(int userId, IncomeSource source);

    List<Income> getIncomesByDateRange(int userId, LocalDate startDate, LocalDate endDate);

    List<Income> getIncomesByMonth(int userId, int year, int month);

    Income updateIncome(int incomeId, IncomeRequest request);

    void deleteIncome(int incomeId);

    Double getTotalIncomeByUser(int userId);

    Double getTotalIncomeByMonth(int userId, int year, int month);

    List<Income> getRecurringIncomes(int userId);

    List<Income> searchIncomes(int userId, String keyword);
}