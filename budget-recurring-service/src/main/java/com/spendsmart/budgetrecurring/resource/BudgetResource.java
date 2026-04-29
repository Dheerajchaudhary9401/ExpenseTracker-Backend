package com.spendsmart.budgetrecurring.resource;

import com.spendsmart.budgetrecurring.dto.BudgetRequest;
import com.spendsmart.budgetrecurring.entity.Budget;
import com.spendsmart.budgetrecurring.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/budgets")
@RequiredArgsConstructor
public class BudgetResource {

    private final BudgetService budgetService;

    @PostMapping("/user/{userId}")
    public ResponseEntity<Budget> createBudget(
            @PathVariable int userId,
            @Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.ok(budgetService.createBudget(userId, request));
    }

    @GetMapping("/{budgetId}")
    public ResponseEntity<Budget> getById(@PathVariable int budgetId) {
        return ResponseEntity.ok(budgetService.getBudgetById(budgetId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Budget>> getByUser(@PathVariable int userId) {
        return ResponseEntity.ok(budgetService.getBudgetsByUser(userId));
    }

    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<Budget>> getActive(@PathVariable int userId) {
        return ResponseEntity.ok(budgetService.getActiveBudgets(userId));
    }

    @GetMapping("/user/{userId}/category/{categoryId}")
    public ResponseEntity<List<Budget>> getByCategory(
            @PathVariable int userId,
            @PathVariable int categoryId) {
        return ResponseEntity.ok(budgetService.getBudgetsByCategory(userId, categoryId));
    }

    @PutMapping("/{budgetId}")
    public ResponseEntity<Budget> updateBudget(
            @PathVariable int budgetId,
            @RequestBody BudgetRequest request) {
        return ResponseEntity.ok(budgetService.updateBudget(budgetId, request));
    }

    @DeleteMapping("/{budgetId}")
    public ResponseEntity<String> deleteBudget(@PathVariable int budgetId) {
        budgetService.deleteBudget(budgetId);
        return ResponseEntity.ok("Budget deleted successfully");
    }

    @PutMapping("/{budgetId}/spent")
    public ResponseEntity<String> updateSpent(
            @PathVariable int budgetId,
            @RequestBody Map<String, Double> body) {
        budgetService.updateSpentAmount(budgetId, body.get("amount"));
        return ResponseEntity.ok("Spent amount updated");
    }

    @GetMapping("/{budgetId}/progress")
    public ResponseEntity<Double> getProgress(@PathVariable int budgetId) {
        return ResponseEntity.ok(budgetService.getBudgetProgress(budgetId));
    }

    @PostMapping("/user/{userId}/check-alerts")
    public ResponseEntity<String> checkAlerts(@PathVariable int userId) {
        budgetService.checkBudgetAlerts(userId);
        return ResponseEntity.ok("Budget alerts checked");
    }
}