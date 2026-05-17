package com.spendsmart.analytics.repository;

import com.spendsmart.analytics.entity.UserRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserRecordRepository extends JpaRepository<UserRecord, Integer> {
    List<UserRecord> findByActiveTrue();
}