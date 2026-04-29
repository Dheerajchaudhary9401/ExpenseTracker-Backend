package com.spendsmart.income.repository;

import com.spendsmart.income.entity.Income;
import com.spendsmart.income.entity.Income.IncomeSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface IncomeRepository extends JpaRepository<Income, Integer> {

    List<Income> findByUserId(int userId);

    List<Income> findByUserIdAndSource(int userId, IncomeSource source);

    List<Income> findByUserIdAndDateBetween(int userId, LocalDate startDate, LocalDate endDate);

    List<Income> findByUserIdAndIsRecurring(int userId, boolean isRecurring);

    @Query("SELECT SUM(i.amount) FROM Income i WHERE i.userId = :userId")
    Double sumAmountByUserId(@Param("userId") int userId);

    @Query("SELECT SUM(i.amount) FROM Income i WHERE i.userId = :userId AND i.date BETWEEN :startDate AND :endDate")
    Double sumAmountByUserIdAndDateBetween(
            @Param("userId") int userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT SUM(i.amount) FROM Income i WHERE i.userId = :userId AND YEAR(i.date) = :year AND MONTH(i.date) = :month")
    Double sumAmountByUserIdAndMonth(
            @Param("userId") int userId,
            @Param("year") int year,
            @Param("month") int month
    );

    Optional<Income> findByIncomeId(int incomeId);

    void deleteByIncomeId(int incomeId);

    @Query("SELECT i FROM Income i WHERE i.userId = :userId AND (LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(i.notes) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Income> searchByKeyword(@Param("userId") int userId, @Param("keyword") String keyword);
}