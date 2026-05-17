package com.spendsmart.budgetrecurring.service;

import com.spendsmart.budgetrecurring.dto.NotificationRequest;
import com.spendsmart.budgetrecurring.entity.Budget;
import com.spendsmart.budgetrecurring.entity.Notification;
import com.spendsmart.budgetrecurring.entity.RecurringTransaction;
import java.util.List;

public interface NotificationService {

    List<Notification> getAll();
    Notification send(int recipientId, NotificationRequest request);

    void sendBudgetAlert(Budget budget);

    void sendRecurringReminder(RecurringTransaction recurring);

    void sendBulk(List<Integer> recipientIds, String title, String message);

    List<Notification> getByRecipient(int recipientId);

    List<Notification> getUnreadNotifications(int recipientId);

    long getUnreadCount(int recipientId);

    void markAsRead(int notificationId);

    void markAllRead(int recipientId);

    void acknowledge(int notificationId);

    void deleteNotification(int notificationId);

    void sendEmail(String to, String subject, String body);
}