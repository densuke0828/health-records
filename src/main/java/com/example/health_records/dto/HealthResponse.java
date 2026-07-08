package com.example.health_records.dto;

import com.example.health_records.entity.Health;
import com.example.health_records.enums.Conditions;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class HealthResponse {
    private Long id;
    private Double weight;
    private Conditions condition;
    private Double sleepTime;
    private String memo;
    private LocalDate recordDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static HealthResponse from(Health health) {
        return HealthResponse.builder()
                .id(health.getId())
                .weight(health.getWeight())
                .condition(health.getCondition())
                .sleepTime(health.getSleepTime())
                .memo(health.getMemo())
                .recordDate(health.getRecordDate())
                .createdAt(health.getCreatedAt())
                .updatedAt(health.getUpdatedAt())
                .build();
    }
}
