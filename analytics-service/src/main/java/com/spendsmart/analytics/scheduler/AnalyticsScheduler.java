package com.spendsmart.analytics.scheduler;

import com.spendsmart.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class AnalyticsScheduler {

    private final AnalyticsService analyticsService;

    // Send monthly summary email on 1st of every month at 8 AM
    @Scheduled(cron = "0 0 8 1 * *")
    public void sendMonthlySummaries() {
        System.out.println("Running scheduled job: Send monthly summary emails");

        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        int year = lastMonth.getYear();
        int month = lastMonth.getMonthValue();

        // In production, loop through all active users
        // For now, just send for user 1
        analyticsService.sendMonthlySummaryEmail(1, year, month);
    }
}