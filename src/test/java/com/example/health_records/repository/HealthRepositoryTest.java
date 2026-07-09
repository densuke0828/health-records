package com.example.health_records.repository;

import com.example.health_records.dto.MonthlyStatsProjection;
import com.example.health_records.entity.Health;
import com.example.health_records.enums.Conditions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@DataJpaTest
public class HealthRepositoryTest {
    @Autowired
    private HealthRepository healthRepository;

    private Health mayRecord;
    private Health juneRecord;
    private Health julyRecord;

    @BeforeEach
    void setUp() {
        mayRecord = healthRepository.save(Health.create(
                58.0, Conditions.NORMAL, 7.0,
                "まあまあ", LocalDate.of(2026, 5, 9)));
        juneRecord = healthRepository.save(Health.create(
                59.0, Conditions.NORMAL, 7.0,
                "早起きできた", LocalDate.of(2026, 6, 9)));
        julyRecord = healthRepository.save(Health.create(
                60.0, Conditions.GOOD, 8.0,
                "よく寝れた", LocalDate.of(2026, 7, 9)));
    }

    @Test
    void findByMonthlyHealth_指定した期間のリストが返る_両方指定() {
        List<Health> result = healthRepository.findByMonthlyHealth(
                        LocalDate.of(2026, 6, 1),
                        LocalDate.of(2026, 7, 31));

        assertThat(result)
                .hasSize(2)
                .extracting(Health::getRecordDate)
                .containsExactlyInAnyOrder(
                        juneRecord.getRecordDate(),
                        julyRecord.getRecordDate());
    }

    @Test
    void findByMonthlyHealth_指定した期間のリストが返る_fromのみ指定() {
        List<Health> result = healthRepository.findByMonthlyHealth(
                LocalDate.of(2026, 6, 1), null);

        assertThat(result)
                .hasSize(2)
                .extracting(Health::getRecordDate)
                .containsExactlyInAnyOrder(
                        juneRecord.getRecordDate(),
                        julyRecord.getRecordDate());
    }

    @Test
    void findByMonthlyHealth_指定した期間のリストが返る_toのみ指定() {
        List<Health> result = healthRepository.findByMonthlyHealth(
                null, LocalDate.of(2026, 6, 9));

        assertThat(result)
                .hasSize(2)
                .extracting(Health::getRecordDate)
                .containsExactlyInAnyOrder(
                        mayRecord.getRecordDate(),
                        juneRecord.getRecordDate());
    }

    @Test
    void findByMonthlyHealth_指定した期間のリストが返る_両方null() {
        List<Health> result = healthRepository.findByMonthlyHealth(null, null);

        assertThat(result)
                .hasSize(3)
                .extracting(Health::getRecordDate)
                .containsExactlyInAnyOrder(
                        mayRecord.getRecordDate(),
                        juneRecord.getRecordDate(),
                        julyRecord.getRecordDate());
    }

    @Test
    void findMonthlyStats_指定した年月の平均体重が返る() {
        healthRepository.save(Health.create(
                65.0, Conditions.GOOD, 8.0,
                "太った", LocalDate.of(2026, 7, 29)));

        Optional<MonthlyStatsProjection> result =
                healthRepository.findMonthlyStats(2026, 7);
        assertThat(result).isPresent().hasValueSatisfying(summary -> {
            assertThat(summary.getYear()).isEqualTo(2026);
            assertThat(summary.getMonth()).isEqualTo(7);
            assertThat(summary.getAvgWeight()).isEqualTo(62.5);
        });
    }
}
