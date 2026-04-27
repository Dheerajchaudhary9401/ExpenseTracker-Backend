package com.spendsmart.expense.service.impl;

import com.spendsmart.expense.dto.ExpenseRequest;
import com.spendsmart.expense.entity.Expense;
import com.spendsmart.expense.entity.Expense.ExpenseType;
import com.spendsmart.expense.repository.ExpenseRepository;
import com.spendsmart.expense.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Override
    public Expense addExpense(int userId, ExpenseRequest request) {
        Expense expense = Expense.builder()
                .userId(userId)
                .categoryId(request.getCategoryId())
                .title(request.getTitle())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .type(request.getType())
                .paymentMethod(request.getPaymentMethod())
                .date(request.getDate())
                .notes(request.getNotes())
                .receiptUrl(request.getReceiptUrl())
                .isRecurring(request.isRecurring())
                .build();

        return expenseRepository.save(expense);
    }

    @Override
    public Expense getExpenseById(int expenseId) {
        return expenseRepository.findByExpenseId(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));
    }

    @Override
    public List<Expense> getExpensesByUser(int userId) {
        return expenseRepository.findByUserId(userId);
    }

    @Override
    public List<Expense> getExpensesByCategory(int categoryId) {
        return expenseRepository.findByCategoryId(categoryId);
    }

    @Override
    public List<Expense> getExpensesByDateRange(int userId, LocalDate startDate, LocalDate endDate) {
        return expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
    }

    @Override
    public List<Expense> getExpensesByMonth(int userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        return expenseRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
    }

    @Override
    public Expense updateExpense(int expenseId, ExpenseRequest request) {
        Expense expense = getExpenseById(expenseId);

        if (request.getTitle() != null) expense.setTitle(request.getTitle());
        if (request.getAmount() != null) expense.setAmount(request.getAmount());
        if (request.getCategoryId() != null) expense.setCategoryId(request.getCategoryId());
        if (request.getDate() != null) expense.setDate(request.getDate());
        if (request.getPaymentMethod() != null) expense.setPaymentMethod(request.getPaymentMethod());
        if (request.getNotes() != null) expense.setNotes(request.getNotes());
        if (request.getReceiptUrl() != null) expense.setReceiptUrl(request.getReceiptUrl());

        return expenseRepository.save(expense);
    }

    @Override
    @Transactional
    public void deleteExpense(int expenseId) {
        expenseRepository.deleteByExpenseId(expenseId);
    }

    @Override
    public Double getTotalByUser(int userId) {
        Double total = expenseRepository.sumAmountByUserId(userId);
        return total != null ? total : 0.0;
    }

    @Override
    public Double getTotalByCategory(int categoryId, LocalDate startDate, LocalDate endDate) {
        Double total = expenseRepository.sumAmountByCategoryAndDateBetween(categoryId, startDate, endDate);
        return total != null ? total : 0.0;
    }

    @Override
    public List<Expense> getExpensesByType(int userId, ExpenseType type) {
        return expenseRepository.findByUserIdAndType(userId, type);
    }

    @Override
    public List<Expense> searchExpenses(int userId, String keyword) {
        return expenseRepository.searchByKeyword(userId, keyword);
    }
}