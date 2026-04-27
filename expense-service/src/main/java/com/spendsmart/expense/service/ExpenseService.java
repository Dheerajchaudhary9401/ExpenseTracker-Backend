package com.spendsmart.expense.service;

import com.spendsmart.expense.dto.ExpenseRequest;
import com.spendsmart.expense.entity.Expense;
import com.spendsmart.expense.entity.Expense.ExpenseType;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseService {

    Expense addExpense(int userId, ExpenseRequest request);

    Expense getExpenseById(int expenseId);

    List<Expense> getExpensesByUser(int userId);

    List<Expense> getExpensesByCategory(int categoryId);

    List<Expense> getExpensesByDateRange(int userId, LocalDate startDate, LocalDate endDate);

    List<Expense> getExpensesByMonth(int userId, int year, int month);

    Expense updateExpense(int expenseId, ExpenseRequest request);

    void deleteExpense(int expenseId);

    Double getTotalByUser(int userId);

    Double getTotalByCategory(int categoryId, LocalDate startDate, LocalDate endDate);

    List<Expense> getExpensesByType(int userId, ExpenseType type);

    List<Expense> searchExpenses(int userId, String keyword);
}