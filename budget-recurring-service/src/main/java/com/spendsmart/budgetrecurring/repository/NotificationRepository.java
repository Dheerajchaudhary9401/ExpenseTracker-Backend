package com.spendsmart.budgetrecurring.repository;

import com.spendsmart.budgetrecurring.entity.Notification;
import com.spendsmart.budgetrecurring.entity.Notification.NotificationType;
import com.spendsmart.budgetrecurring.entity.Notification.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByRecipientId(int recipientId);

    List<Notification> findByRecipientIdAndIsRead(int recipientId, boolean isRead);

    long countByRecipientIdAndIsRead(int recipientId, boolean isRead);

    List<Notification> findByType(NotificationType type);

    List<Notification> findBySeverity(Severity severity);

    List<Notification> findByIsAcknowledged(boolean isAcknowledged);

    void deleteByNotificationId(int notificationId);
}