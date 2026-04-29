package com.spendsmart.budgetrecurring.service.impl;

import com.spendsmart.budgetrecurring.dto.RecurringTransactionRequest;
import com.spendsmart.budgetrecurring.entity.RecurringTransaction;
import com.spendsmart.budgetrecurring.entity.RecurringTransaction.TransactionType;
import com.spendsmart.budgetrecurring.repository.RecurringTransactionRepository;
import com.spendsmart.budgetrecurring.service.RecurringTransactionService;
import com.spendsmart.budgetrecurring.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecurringTransactionServiceImpl implements RecurringTransactionService {

    private final RecurringTransactionRepository recurringRepository;
    private final NotificationService notificationService;

    @Override
    public RecurringTransaction addRecurring(int userId, RecurringTransactionRequest request) {
        RecurringTransaction recurring = RecurringTransaction.builder()
                .userId(userId)
                .categoryId(request.getCategoryId())
                .title(request.getTitle())
                .amount(request.getAmount())
                .type(request.getType())
                .frequency(request.getFrequency())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .nextDueDate(request.getStartDate())
                .description(request.getDescription())
                .paymentMethod(request.getPaymentMethod())
                .isActive(true)
                .build();

        return recurringRepository.save(recurring);
    }

    @Override
    public RecurringTransaction getById(int recurringId) {
        return recurringRepository.findByRecurringId(recurringId)
                .orElseThrow(() -> new RuntimeException("Recurring transaction not found"));
    }

    @Override
    public List<RecurringTransaction> getByUser(int userId) {
        return recurringRepository.findByUserId(userId);
    }

    @Override
    public List<RecurringTransaction> getActiveRecurring(int userId) {
        return recurringRepository.findByUserIdAndIsActive(userId, true);
    }

    @Override
    public RecurringTransaction updateRecurring(int recurringId, RecurringTransactionRequest request) {
        RecurringTransaction recurring = getById(recurringId);

        if (request.getTitle() != null) recurring.setTitle(request.getTitle());
        if (request.getAmount() != null) recurring.setAmount(request.getAmount());
        if (request.getFrequency() != null) recurring.setFrequency(request.getFrequency());
        if (request.getEndDate() != null) recurring.setEndDate(request.getEndDate());
        if (request.getDescription() != null) recurring.setDescription(request.getDescription());

        return recurringRepository.save(recurring);
    }

    @Override
    public void deactivateRecurring(int recurringId) {
        RecurringTransaction recurring = getById(recurringId);
        recurring.setActive(false);
        recurringRepository.save(recurring);
    }

    @Override
    @Transactional
    public void deleteRecurring(int recurringId) {
        recurringRepository.deleteById(recurringId);
    }

    @Override
    @Transactional
    public void processUpcomingDue() {
        LocalDate today = LocalDate.now();
        List<RecurringTransaction> dueTxns = recurringRepository.findByNextDueDateBefore(today.plusDays(1));

        for (RecurringTransaction recurring : dueTxns) {
            if (recurring.isActive()) {
                // Generate transaction (this would call expense/income service API in production)
                generateTransactionFromRecurring(recurring);

                // Update next due date
                updateNextDueDate(recurring.getRecurringId());

                // Send notification 3 days before due
                if (recurring.getNextDueDate().isBefore(today.plusDays(4))) {
                    notificationService.sendRecurringReminder(recurring);
                }
            }
        }
    }

    @Override
    public void updateNextDueDate(int recurringId) {
        RecurringTransaction recurring = getById(recurringId);
        LocalDate nextDate = recurring.getNextDueDate();

        switch (recurring.getFrequency()) {
            case DAILY:
                nextDate = nextDate.plusDays(1);
                break;
            case WEEKLY:
                nextDate = nextDate.plusWeeks(1);
                break;
            case MONTHLY:
                nextDate = nextDate.plusMonths(1);
                break;
            case QUARTERLY:
                nextDate = nextDate.plusMonths(3);
                break;
            case YEARLY:
                nextDate = nextDate.plusYears(1);
                break;
        }

        recurring.setNextDueDate(nextDate);
        recurringRepository.save(recurring);
    }

    @Override
    public void generateTransactionFromRecurring(RecurringTransaction recurring) {
        // In production, this would call expense-service or income-service REST API
        // For now, just log it
        System.out.println("Generated transaction from recurring: " + recurring.getTitle()
                + " Amount: " + recurring.getAmount());
    }

    @Override
    public List<RecurringTransaction> getUpcomingThisMonth(int userId) {
        YearMonth thisMonth = YearMonth.now();
        LocalDate start = thisMonth.atDay(1);
        LocalDate end = thisMonth.atEndOfMonth();

        return recurringRepository.findByNextDueDateBetween(start, end);
    }

    @Override
    public List<RecurringTransaction> getByType(int userId, TransactionType type) {
        return recurringRepository.findByUserIdAndType(userId, type);
    }
}