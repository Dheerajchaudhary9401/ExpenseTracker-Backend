package com.spendsmart.income.resource;

import com.spendsmart.income.dto.IncomeRequest;
import com.spendsmart.income.entity.Income;
import com.spendsmart.income.entity.Income.IncomeSource;
import com.spendsmart.income.service.IncomeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/incomes")
@RequiredArgsConstructor
public class IncomeResource {

    private final IncomeService incomeService;

    @PostMapping("/user/{userId}")
    public ResponseEntity<Income> addIncome(
            @PathVariable int userId,
            @Valid @RequestBody IncomeRequest request) {
        return ResponseEntity.ok(incomeService.addIncome(userId, request));
    }

    @GetMapping("/{incomeId}")
    public ResponseEntity<Income> getById(@PathVariable int incomeId) {
        return ResponseEntity.ok(incomeService.getIncomeById(incomeId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Income>> getByUser(@PathVariable int userId) {
        return ResponseEntity.ok(incomeService.getIncomesByUser(userId));
    }

    @GetMapping("/user/{userId}/source/{source}")
    public ResponseEntity<List<Income>> getBySource(
            @PathVariable int userId,
            @PathVariable IncomeSource source) {
        return ResponseEntity.ok(incomeService.getIncomesBySource(userId, source));
    }

    @GetMapping("/user/{userId}/range")
    public ResponseEntity<List<Income>> getByDateRange(
            @PathVariable int userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(incomeService.getIncomesByDateRange(userId, startDate, endDate));
    }

    @GetMapping("/user/{userId}/month/{year}/{month}")
    public ResponseEntity<List<Income>> getByMonth(
            @PathVariable int userId,
            @PathVariable int year,
            @PathVariable int month) {
        return ResponseEntity.ok(incomeService.getIncomesByMonth(userId, year, month));
    }

    @GetMapping("/user/{userId}/recurring")
    public ResponseEntity<List<Income>> getRecurring(@PathVariable int userId) {
        return ResponseEntity.ok(incomeService.getRecurringIncomes(userId));
    }

    @GetMapping("/user/{userId}/search")
    public ResponseEntity<List<Income>> search(
            @PathVariable int userId,
            @RequestParam String keyword) {
        return ResponseEntity.ok(incomeService.searchIncomes(userId, keyword));
    }

    @PutMapping("/{incomeId}")
    public ResponseEntity<Income> updateIncome(
            @PathVariable int incomeId,
            @RequestBody IncomeRequest request) {
        return ResponseEntity.ok(incomeService.updateIncome(incomeId, request));
    }

    @DeleteMapping("/{incomeId}")
    public ResponseEntity<String> deleteIncome(@PathVariable int incomeId) {
        incomeService.deleteIncome(incomeId);
        return ResponseEntity.ok("Income deleted");
    }

    @GetMapping("/user/{userId}/total")
    public ResponseEntity<Double> getTotalByUser(@PathVariable int userId) {
        return ResponseEntity.ok(incomeService.getTotalIncomeByUser(userId));
    }

    @GetMapping("/user/{userId}/total/month/{year}/{month}")
    public ResponseEntity<Double> getTotalByMonth(
            @PathVariable int userId,
            @PathVariable int year,
            @PathVariable int month) {
        return ResponseEntity.ok(incomeService.getTotalIncomeByMonth(userId, year, month));
    }
}