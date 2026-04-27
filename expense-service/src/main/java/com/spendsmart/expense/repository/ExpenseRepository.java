package com.spendsmart.expense.repository;

import com.spendsmart.expense.entity.Expense;
import com.spendsmart.expense.entity.Expense.ExpenseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Integer> {

    List<Expense> findByUserId(int userId);

    List<Expense> findByUserIdAndType(int userId, ExpenseType type);

    List<Expense> findByCategoryId(int categoryId);

    List<Expense> findByUserIdAndDate(int userId, LocalDate date);

    List<Expense> findByUserIdAndDateBetween(int userId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.userId = :userId")
    Double sumAmountByUserId(@Param("userId") int userId);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.userId = :userId AND e.date BETWEEN :startDate AND :endDate")
    Double sumAmountByUserIdAndDateBetween(
            @Param("userId") int userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.categoryId = :categoryId AND e.date BETWEEN :startDate AND :endDate")
    Double sumAmountByCategoryAndDateBetween(
            @Param("categoryId") int categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    Optional<Expense> findByExpenseId(int expenseId);

    void deleteByExpenseId(int expenseId);

    @Query("SELECT e FROM Expense e WHERE e.userId = :userId AND (LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(e.notes) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Expense> searchByKeyword(@Param("userId") int userId, @Param("keyword") String keyword);
}