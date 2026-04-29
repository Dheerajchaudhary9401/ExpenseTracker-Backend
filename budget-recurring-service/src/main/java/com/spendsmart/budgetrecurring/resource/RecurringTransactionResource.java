package com.spendsmart.budgetrecurring.resource;

import com.spendsmart.budgetrecurring.dto.RecurringTransactionRequest;
import com.spendsmart.budgetrecurring.entity.RecurringTransaction;
import com.spendsmart.budgetrecurring.entity.RecurringTransaction.TransactionType;
import com.spendsmart.budgetrecurring.service.RecurringTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
        import java.util.List;

@RestController
@RequestMapping("/recurring")
@RequiredArgsConstructor
public class RecurringTransactionResource {

    private final RecurringTransactionService recurringService;

    @PostMapping("/user/{userId}")
    public ResponseEntity<RecurringTransaction> addRecurring(
            @PathVariable int userId,
            @Valid @RequestBody RecurringTransactionRequest request) {
        return ResponseEntity.ok(recurringService.addRecurring(userId, request));
    }

    @GetMapping("/{recurringId}")
    public ResponseEntity<RecurringTransaction> getById(@PathVariable int recurringId) {
        return ResponseEntity.ok(recurringService.getById(recurringId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RecurringTransaction>> getByUser(@PathVariable int userId) {
        return ResponseEntity.ok(recurringService.getByUser(userId));
    }

    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<RecurringTransaction>> getActive(@PathVariable int userId) {
        return ResponseEntity.ok(recurringService.getActiveRecurring(userId));
    }

    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<RecurringTransaction>> getByType(
            @PathVariable int userId,
            @PathVariable TransactionType type) {
        return ResponseEntity.ok(recurringService.getByType(userId, type));
    }

    @GetMapping("/user/{userId}/upcoming")
    public ResponseEntity<List<RecurringTransaction>> getUpcoming(@PathVariable int userId) {
        return ResponseEntity.ok(recurringService.getUpcomingThisMonth(userId));
    }

    @PutMapping("/{recurringId}")
    public ResponseEntity<RecurringTransaction> updateRecurring(
            @PathVariable int recurringId,
            @RequestBody RecurringTransactionRequest request) {
        return ResponseEntity.ok(recurringService.updateRecurring(recurringId, request));
    }

    @PutMapping("/{recurringId}/deactivate")
    public ResponseEntity<String> deactivate(@PathVariable int recurringId) {
        recurringService.deactivateRecurring(recurringId);
        return ResponseEntity.ok("Recurring transaction deactivated");
    }

    @DeleteMapping("/{recurringId}")
    public ResponseEntity<String> deleteRecurring(@PathVariable int recurringId) {
        recurringService.deleteRecurring(recurringId);
        return ResponseEntity.ok("Recurring transaction deleted");
    }

    @PostMapping("/{recurringId}/update-next-due")
    public ResponseEntity<String> updateNextDue(@PathVariable int recurringId) {
        recurringService.updateNextDueDate(recurringId);
        return ResponseEntity.ok("Next due date updated");
    }

    @PostMapping("/process-due")
    public ResponseEntity<String> processDue() {
        recurringService.processUpcomingDue();
        return ResponseEntity.ok("Processed upcoming recurring transactions");
    }
}