package com.spendsmart.analytics.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRecord {

    @Id
    private int userId;

    private String email;
    private boolean active;
    private LocalDateTime createdAt;
}