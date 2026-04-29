package com.spendsmart.budgetrecurring.service;

import com.spendsmart.budgetrecurring.dto.RecurringTransactionRequest;
import com.spendsmart.budgetrecurring.entity.RecurringTransaction;
import com.spendsmart.budgetrecurring.entity.RecurringTransaction.TransactionType;
import java.time.LocalDate;
import java.util.List;

public interface RecurringTransactionService {

    RecurringTransaction addRecurring(int userId, RecurringTransactionRequest request);

    RecurringTransaction getById(int recurringId);

    List<RecurringTransaction> getByUser(int userId);

    List<RecurringTransaction> getActiveRecurring(int userId);

    RecurringTransaction updateRecurring(int recurringId, RecurringTransactionRequest request);

    void deactivateRecurring(int recurringId);

    void deleteRecurring(int recurringId);

    void processUpcomingDue();

    void updateNextDueDate(int recurringId);

    void generateTransactionFromRecurring(RecurringTransaction recurring);

    List<RecurringTransaction> getUpcomingThisMonth(int userId);

    List<RecurringTransaction> getByType(int userId, TransactionType type);
}