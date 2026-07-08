package com.example.health_records.dto;

public interface MonthlyStatsProjection {
    Integer getYear();
    Integer getMonth();
    Double getAvgWeight();
}
