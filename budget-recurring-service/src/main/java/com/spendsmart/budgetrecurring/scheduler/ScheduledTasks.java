package com.spendsmart.budgetrecurring.scheduler;

import com.spendsmart.budgetrecurring.service.BudgetService;
import com.spendsmart.budgetrecurring.service.RecurringTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final RecurringTransactionService recurringService;
    private final BudgetService budgetService;

    // Run every day at midnight
    @Scheduled(cron = "0 0 0 * * *")
    public void processRecurringTransactions() {
        System.out.println("Running scheduled job: Process recurring transactions");
        recurringService.processUpcomingDue();
    }

    // Run on 1st of every month at 1 AM
    @Scheduled(cron = "0 0 1 1 * *")
    public void resetBudgets() {
        System.out.println("Running scheduled job: Reset budget periods");
        budgetService.resetBudgetPeriod();
    }
}