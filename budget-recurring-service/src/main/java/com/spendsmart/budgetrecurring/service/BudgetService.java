package com.spendsmart.budgetrecurring.service;

import com.spendsmart.budgetrecurring.dto.BudgetRequest;
import com.spendsmart.budgetrecurring.entity.Budget;
import java.util.List;

public interface BudgetService {

    Budget createBudget(int userId, BudgetRequest request);

    Budget getBudgetById(int budgetId);

    List<Budget> getBudgetsByUser(int userId);

    List<Budget> getActiveBudgets(int userId);

    Budget updateBudget(int budgetId, BudgetRequest request);

    void deleteBudget(int budgetId);

    void updateSpentAmount(int budgetId, double amount);

    double getBudgetProgress(int budgetId);

    void checkBudgetAlerts(int userId);

    void resetBudgetPeriod();

    List<Budget> getBudgetsByCategory(int userId, int categoryId);
}