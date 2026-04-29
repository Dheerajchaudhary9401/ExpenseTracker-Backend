package com.spendsmart.budgetrecurring.repository;

import com.spendsmart.budgetrecurring.entity.Budget;
import com.spendsmart.budgetrecurring.entity.Budget.Period;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Integer> {

    List<Budget> findByUserId(int userId);

    Optional<Budget> findByBudgetId(int budgetId);

    List<Budget> findByUserIdAndCategoryId(int userId, int categoryId);

    List<Budget> findByPeriod(Period period);

    List<Budget> findByIsActive(boolean isActive);

    List<Budget> findByUserIdAndIsActive(int userId, boolean isActive);

    long countByUserId(int userId);

    void deleteByBudgetId(int budgetId);
}