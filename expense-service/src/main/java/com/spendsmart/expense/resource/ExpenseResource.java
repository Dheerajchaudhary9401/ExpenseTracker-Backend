package com.spendsmart.expense.resource;

import com.spendsmart.expense.dto.ExpenseRequest;
import com.spendsmart.expense.entity.Expense;
import com.spendsmart.expense.entity.Expense.ExpenseType;
import com.spendsmart.expense.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpenseResource {

    private final ExpenseService expenseService;

    @PostMapping("/user/{userId}")
    public ResponseEntity<Expense> addExpense(
            @PathVariable int userId,
            @Valid @RequestBody ExpenseRequest request) {
        return ResponseEntity.ok(expenseService.addExpense(userId, request));
    }

    @GetMapping("/{expenseId}")
    public ResponseEntity<Expense> getById(@PathVariable int expenseId) {
        return ResponseEntity.ok(expenseService.getExpenseById(expenseId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Expense>> getByUser(@PathVariable int userId) {
        return ResponseEntity.ok(expenseService.getExpensesByUser(userId));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Expense>> getByCategory(@PathVariable int categoryId) {
        return ResponseEntity.ok(expenseService.getExpensesByCategory(categoryId));
    }

    @GetMapping("/user/{userId}/range")
    public ResponseEntity<List<Expense>> getByDateRange(
            @PathVariable int userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(expenseService.getExpensesByDateRange(userId, startDate, endDate));
    }

    @GetMapping("/user/{userId}/month/{year}/{month}")
    public ResponseEntity<List<Expense>> getByMonth(
            @PathVariable int userId,
            @PathVariable int year,
            @PathVariable int month) {
        return ResponseEntity.ok(expenseService.getExpensesByMonth(userId, year, month));
    }

    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<Expense>> getByType(
            @PathVariable int userId,
            @PathVariable ExpenseType type) {
        return ResponseEntity.ok(expenseService.getExpensesByType(userId, type));
    }

    @GetMapping("/user/{userId}/search")
    public ResponseEntity<List<Expense>> search(
            @PathVariable int userId,
            @RequestParam String keyword) {
        return ResponseEntity.ok(expenseService.searchExpenses(userId, keyword));
    }

    @PutMapping("/{expenseId}")
    public ResponseEntity<Expense> updateExpense(
            @PathVariable int expenseId,
            @RequestBody ExpenseRequest request) {
        return ResponseEntity.ok(expenseService.updateExpense(expenseId, request));
    }

    @DeleteMapping("/{expenseId}")
    public ResponseEntity<String> deleteExpense(@PathVariable int expenseId) {
        expenseService.deleteExpense(expenseId);
        return ResponseEntity.ok("Expense deleted");
    }

    @GetMapping("/user/{userId}/total")
    public ResponseEntity<Double> getTotalByUser(@PathVariable int userId) {
        return ResponseEntity.ok(expenseService.getTotalByUser(userId));
    }

    @GetMapping("/category/{categoryId}/total")
    public ResponseEntity<Double> getTotalByCategory(
            @PathVariable int categoryId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(expenseService.getTotalByCategory(categoryId, startDate, endDate));
    }
}