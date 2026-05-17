package com.spendsmart.budgetrecurring.resource;

import com.spendsmart.budgetrecurring.dto.NotificationRequest;
import com.spendsmart.budgetrecurring.entity.Notification;
import com.spendsmart.budgetrecurring.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationResource {

    private final NotificationService notificationService;

    @GetMapping("/all")
    public ResponseEntity<List<Notification>> getAll() {
        return ResponseEntity.ok(notificationService.getAll());
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<Notification> sendNotification(
            @PathVariable int userId,
            @Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.ok(notificationService.send(userId, request));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getByUser(@PathVariable int userId) {
        return ResponseEntity.ok(notificationService.getByRecipient(userId));
    }

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<Notification>> getUnread(@PathVariable int userId) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(userId));
    }

    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Long> getUnreadCount(@PathVariable int userId) {
        return ResponseEntity.ok(notificationService.getUnreadCount(userId));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<String> markAsRead(@PathVariable int notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok("Notification marked as read");
    }

    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<String> markAllRead(@PathVariable int userId) {
        notificationService.markAllRead(userId);
        return ResponseEntity.ok("All notifications marked as read");
    }

    @PutMapping("/{notificationId}/acknowledge")
    public ResponseEntity<String> acknowledge(@PathVariable int notificationId) {
        notificationService.acknowledge(notificationId);
        return ResponseEntity.ok("Notification acknowledged");
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<String> deleteNotification(@PathVariable int notificationId) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok("Notification deleted");
    }

    @PostMapping("/bulk")
    public ResponseEntity<String> sendBulk(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> recipientIds = (List<Integer>) body.get("recipientIds");
        String title = (String) body.get("title");
        String message = (String) body.get("message");

        notificationService.sendBulk(recipientIds, title, message);
        return ResponseEntity.ok("Bulk notifications sent");
    }

    @PostMapping("/email")
    public ResponseEntity<String> sendEmail(@RequestBody Map<String, String> body) {
        notificationService.sendEmail(
                body.get("to"),
                body.get("subject"),
                body.get("body")
        );
        return ResponseEntity.ok("Email sent");
    }
}