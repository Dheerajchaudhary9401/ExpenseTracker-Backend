//package com.spendsmart.analytics.scheduler;
//
//import com.spendsmart.analytics.service.AnalyticsService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import java.time.LocalDate;
//
//@Component
//@RequiredArgsConstructor
//public class AnalyticsScheduler {
//
//    private final AnalyticsService analyticsService;
//
//    // Send monthly summary email on 1st of every month at 8 AM
//    @Scheduled(cron = "0 0 8 1 * *")
//    public void sendMonthlySummaries() {
//        System.out.println("Running scheduled job: Send monthly summary emails");
//
//        LocalDate lastMonth = LocalDate.now().minusMonths(1);
//        int year = lastMonth.getYear();
//        int month = lastMonth.getMonthValue();
//
//        // In production, loop through all active users
//        // For now, just send for user 1
//        //analyticsService.sendMonthlySummaryEmail(1, year, month);
//    }
//
//}

package com.spendsmart.analytics.scheduler;

import com.spendsmart.analytics.entity.UserRecord;
import com.spendsmart.analytics.repository.UserRecordRepository;
import com.spendsmart.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalyticsScheduler {

    private final AnalyticsService analyticsService;
    private final UserRecordRepository userRecordRepository;

    // Runs at 8 AM on the 1st of every month
    @Scheduled(cron = "0 0 8 1 * *")
    public void sendMonthlySummaries() {
        log.info("Running scheduled job: Send monthly summary emails");

        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        int year = lastMonth.getYear();
        int month = lastMonth.getMonthValue();

        List<UserRecord> users = userRecordRepository.findByActiveTrue();
        log.info("Found {} active users for monthly summary", users.size());

        for (UserRecord user : users) {
            try {
                analyticsService.sendMonthlySummaryEmail(
                        user.getUserId(), user.getEmail(), year, month
                );
                log.info("Monthly summary sent for userId={}", user.getUserId());
            } catch (Exception e) {
                log.error("Failed to send summary for userId={}: {}",
                        user.getUserId(), e.getMessage());
            }
        }
    }
}