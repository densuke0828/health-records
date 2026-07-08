package com.example.health_records.repository;

import com.example.health_records.dto.MonthlyStatsProjection;
import com.example.health_records.entity.Health;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HealthRepository extends JpaRepository<Health, Long> {
    @Query("""
           SELECT h
           FROM Health h
           WHERE (:from IS NULL OR h.recordDate >= :from)
            AND (:to IS NULL OR h.recordDate <= :to)
           """)
    List<Health> findByMonthlyHealth(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("""
           SELECT YEAR(h.recordDate) AS year,
                  MONTH(h.recordDate) AS month,
                  AVG(h.weight) AS avgWeight
           FROM Health h
           WHERE YEAR(h.recordDate) = :year AND MONTH(h.recordDate) = :month
           GROUP BY YEAR(h.recordDate), MONTH(h.recordDate)
           """)
    Optional<MonthlyStatsProjection> findMonthlyStats(
            @Param("year") int year,
            @Param("month") int month);
}
