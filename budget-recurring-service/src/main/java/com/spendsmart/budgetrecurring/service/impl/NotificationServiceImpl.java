package com.spendsmart.budgetrecurring.service.impl;

import com.spendsmart.budgetrecurring.dto.NotificationRequest;
import com.spendsmart.budgetrecurring.entity.Budget;
import com.spendsmart.budgetrecurring.entity.Notification;
import com.spendsmart.budgetrecurring.entity.Notification.NotificationType;
import com.spendsmart.budgetrecurring.entity.Notification.Severity;
import com.spendsmart.budgetrecurring.entity.RecurringTransaction;
import com.spendsmart.budgetrecurring.repository.NotificationRepository;
import com.spendsmart.budgetrecurring.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;

    @Override
    public Notification send(int recipientId, NotificationRequest request) {
        Notification notification = Notification.builder()
                .recipientId(recipientId)
                .type(request.getType())
                .severity(request.getSeverity())
                .title(request.getTitle())
                .message(request.getMessage())
                .relatedId(request.getRelatedId())
                .relatedType(request.getRelatedType())
                .isRead(false)
                .isAcknowledged(false)
                .build();

        return notificationRepository.save(notification);
    }

    @Override
    public void sendBudgetAlert(Budget budget) {
        double progress = (budget.getSpentAmount() / budget.getLimitAmount()) * 100;
        String title = "Budget Alert: " + budget.getName();
        String message = String.format(
                "You've spent %.2f%% of your %s budget (%.2f / %.2f %s)",
                progress, budget.getName(), budget.getSpentAmount(),
                budget.getLimitAmount(), budget.getCurrency()
        );

        Severity severity = progress >= 100 ? Severity.CRITICAL : Severity.WARNING;

        NotificationRequest request = new NotificationRequest();
        request.setTitle(title);
        request.setMessage(message);
        request.setType(NotificationType.BUDGET_ALERT);
        request.setSeverity(severity);
        request.setRelatedId(budget.getBudgetId());
        request.setRelatedType("BUDGET");

        send(budget.getUserId(), request);

        // Send email if critical
        if (severity == Severity.CRITICAL) {
            sendEmail("user@example.com", title, message);
        }
    }

    @Override
    public void sendRecurringReminder(RecurringTransaction recurring) {
        String title = "Upcoming: " + recurring.getTitle();
        String message = String.format(
                "Reminder: %s (%.2f) is due on %s",
                recurring.getTitle(), recurring.getAmount(), recurring.getNextDueDate()
        );

        NotificationRequest request = new NotificationRequest();
        request.setTitle(title);
        request.setMessage(message);
        request.setType(NotificationType.RECURRING_DUE);
        request.setSeverity(Severity.INFO);
        request.setRelatedId(recurring.getRecurringId());
        request.setRelatedType("RECURRING");

        send(recurring.getUserId(), request);
    }

    @Override
    public void sendBulk(List<Integer> recipientIds, String title, String message) {
        for (Integer recipientId : recipientIds) {
            NotificationRequest request = new NotificationRequest();
            request.setTitle(title);
            request.setMessage(message);
            request.setType(NotificationType.SYSTEM);
            request.setSeverity(Severity.INFO);

            send(recipientId, request);
        }
    }

    @Override
    public List<Notification> getByRecipient(int recipientId) {
        return notificationRepository.findByRecipientId(recipientId);
    }

    @Override
    public List<Notification> getUnreadNotifications(int recipientId) {
        return notificationRepository.findByRecipientIdAndIsRead(recipientId, false);
    }

    @Override
    public long getUnreadCount(int recipientId) {
        return notificationRepository.countByRecipientIdAndIsRead(recipientId, false);
    }

    @Override
    public void markAsRead(int notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllRead(int recipientId) {
        List<Notification> notifications = notificationRepository
                .findByRecipientIdAndIsRead(recipientId, false);
        notifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(notifications);
    }

    @Override
    public void acknowledge(int notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setAcknowledged(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void deleteNotification(int notificationId) {
        notificationRepository.deleteByNotificationId(notificationId);
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            System.out.println("Email sent to: " + to);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }
}