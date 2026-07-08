package com.example.health_records.entity;

import com.example.health_records.enums.Conditions;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "health_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Health {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "weight", nullable = false)
    private Double weight;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition", nullable = false)
    private Conditions condition;

    @Column(name = "sleep_time", nullable = false)
    private Double sleepTime;

    @Column(name = "memo", length = 200)
    private String memo;

    @Column(name = "date", nullable = false)
    private LocalDate recordDate;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static Health create(
            Double weight, Conditions condition, Double sleepTime,
            String memo, LocalDate recordDate) {
        return Health.builder()
                .weight(weight)
                .condition(condition)
                .sleepTime(sleepTime)
                .memo(memo)
                .recordDate(recordDate)
                .build();
    }

    public void update(
            Double weight, Conditions condition, Double sleepTime, String memo) {
        if (weight != null) {
            this.weight = weight;
        }
        if (condition != null) {
            this.condition = condition;
        }
        if (sleepTime != null) {
            this.sleepTime = sleepTime;
        }
        if (memo != null) {
            this.memo = memo;
        }
    }
}
