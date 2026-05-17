package com.spendsmart.analytics.resource;

import com.spendsmart.analytics.dto.UserRecordDto;
import com.spendsmart.analytics.entity.UserRecord;
import com.spendsmart.analytics.repository.UserRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/analytics/users")
@RequiredArgsConstructor
@Slf4j
public class UserRecordResource {

    private final UserRecordRepository userRecordRepository;

    // Called by auth-service after register or Google login
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserRecordDto dto) {
        // Only create if doesn't already exist (idempotent)
        if (!userRecordRepository.existsById(dto.getUserId())) {
            UserRecord record = new UserRecord();
            record.setUserId(dto.getUserId());
            record.setEmail(dto.getEmail());
            record.setActive(true);
            record.setCreatedAt(LocalDateTime.now());
            userRecordRepository.save(record);
            log.info("Registered userId={} in analytics", dto.getUserId());
        }
        return ResponseEntity.ok("OK");
    }

    // Called by auth-service when user is deactivated
    @PutMapping("/{userId}/deactivate")
    public ResponseEntity<String> deactivateUser(@PathVariable int userId) {
        userRecordRepository.findById(userId).ifPresent(record -> {
            record.setActive(false);
            userRecordRepository.save(record);
            log.info("Deactivated userId={} in analytics", userId);
        });
        return ResponseEntity.ok("OK");
    }
}