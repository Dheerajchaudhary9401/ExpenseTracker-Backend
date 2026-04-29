package com.spendsmart.budgetrecurring.repository;

import com.spendsmart.budgetrecurring.entity.RecurringTransaction;
import com.spendsmart.budgetrecurring.entity.RecurringTransaction.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Integer> {

    List<RecurringTransaction> findByUserId(int userId);

    List<RecurringTransaction> findByUserIdAndType(int userId, TransactionType type);

    List<RecurringTransaction> findByUserIdAndIsActive(int userId, boolean isActive);

    List<RecurringTransaction> findByNextDueDateBefore(LocalDate date);

    List<RecurringTransaction> findByNextDueDateBetween(LocalDate startDate, LocalDate endDate);

    Optional<RecurringTransaction> findByRecurringId(int recurringId);

    long countByUserIdAndIsActive(int userId, boolean isActive);
}