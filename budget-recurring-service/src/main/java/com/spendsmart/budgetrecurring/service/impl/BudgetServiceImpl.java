package com.spendsmart.budgetrecurring.service.impl;

import com.spendsmart.budgetrecurring.dto.BudgetRequest;
import com.spendsmart.budgetrecurring.entity.Budget;
import com.spendsmart.budgetrecurring.repository.BudgetRepository;
import com.spendsmart.budgetrecurring.service.BudgetService;
import com.spendsmart.budgetrecurring.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final NotificationService notificationService;

    @Override
    public Budget createBudget(int userId, BudgetRequest request) {
        Budget budget = Budget.builder()
                .userId(userId)
                .categoryId(request.getCategoryId())
                .name(request.getName())
                .limitAmount(request.getLimitAmount())
                .currency(request.getCurrency())
                .period(request.getPeriod())
                .startDate(request.getStartDate() != null ? request.getStartDate() : LocalDate.now())
                .endDate(request.getEndDate())
                .alertThreshold(request.getAlertThreshold())
                .spentAmount(0.0)
                .isActive(true)
                .build();

        return budgetRepository.save(budget);
    }

    @Override
    public Budget getBudgetById(int budgetId) {
        return budgetRepository.findByBudgetId(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found"));
    }

    @Override
    public List<Budget> getBudgetsByUser(int userId) {
        return budgetRepository.findByUserId(userId);
    }

    @Override
    public List<Budget> getActiveBudgets(int userId) {
        return budgetRepository.findByUserIdAndIsActive(userId, true);
    }

    @Override
    public Budget updateBudget(int budgetId, BudgetRequest request) {
        Budget budget = getBudgetById(budgetId);

        if (request.getName() != null) budget.setName(request.getName());
        if (request.getLimitAmount() != null) budget.setLimitAmount(request.getLimitAmount());
        if (request.getAlertThreshold() != null) budget.setAlertThreshold(request.getAlertThreshold());
        if (request.getStartDate() != null) budget.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) budget.setEndDate(request.getEndDate());

        return budgetRepository.save(budget);
    }

    @Override
    @Transactional
    public void deleteBudget(int budgetId) {
        budgetRepository.deleteByBudgetId(budgetId);
    }

    @Override
    public void updateSpentAmount(int budgetId, double amount) {
        Budget budget = getBudgetById(budgetId);
        budget.setSpentAmount(budget.getSpentAmount() + amount);
        budgetRepository.save(budget);

        // Check if alert needed
        double progressPercentage = (budget.getSpentAmount() / budget.getLimitAmount()) * 100;
        if (progressPercentage >= budget.getAlertThreshold()) {
            notificationService.sendBudgetAlert(budget);
        }
    }

    @Override
    public double getBudgetProgress(int budgetId) {
        Budget budget = getBudgetById(budgetId);
        return (budget.getSpentAmount() / budget.getLimitAmount()) * 100;
    }

    @Override
    public void checkBudgetAlerts(int userId) {
        List<Budget> budgets = getActiveBudgets(userId);
        for (Budget budget : budgets) {
            double progress = (budget.getSpentAmount() / budget.getLimitAmount()) * 100;
            if (progress >= budget.getAlertThreshold()) {
                notificationService.sendBudgetAlert(budget);
            }
        }
    }

    @Override
    @Transactional
    public void resetBudgetPeriod() {
        List<Budget> budgets = budgetRepository.findByIsActive(true);
        for (Budget budget : budgets) {
            LocalDate today = LocalDate.now();
            if (budget.getEndDate() != null && today.isAfter(budget.getEndDate())) {
                budget.setSpentAmount(0.0);

                // Set new period dates based on budget period
                switch (budget.getPeriod()) {
                    case WEEKLY:
                        budget.setStartDate(today);
                        budget.setEndDate(today.plusWeeks(1));
                        break;
                    case MONTHLY:
                        budget.setStartDate(today);
                        budget.setEndDate(today.plusMonths(1));
                        break;
                    default:
                        break;
                }
                budgetRepository.save(budget);
            }
        }
    }

    @Override
    public List<Budget> getBudgetsByCategory(int userId, int categoryId) {
        return budgetRepository.findByUserIdAndCategoryId(userId, categoryId);
    }
}