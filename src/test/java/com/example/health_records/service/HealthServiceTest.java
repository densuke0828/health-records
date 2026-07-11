package com.example.health_records.service;

import com.example.health_records.dto.HealthRequest;
import com.example.health_records.dto.HealthUpdateRequest;
import com.example.health_records.dto.MonthlyStatsProjection;
import com.example.health_records.entity.Health;
import com.example.health_records.enums.Conditions;
import com.example.health_records.exception.HealthNotFoundException;
import com.example.health_records.repository.HealthRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class HealthServiceTest {
    @Mock
    private HealthRepository healthRepository;

    @InjectMocks
    private HealthService healthService;

    private Health mayRecord;
    private Health juneRecord;
    private Health julyRecord;

    @BeforeEach
    void setUp() {
        mayRecord = Health.create(
                70.0, Conditions.BAD, 6.0,
                "寝れなかった", LocalDate.of(2026, 5, 9));
        juneRecord = Health.create(
                65.0, Conditions.GOOD, 8.0,
                "よく寝れた", LocalDate.of(2026, 6, 9));
        julyRecord = Health.create(
                60.0, Conditions.NORMAL, 7.0,
                "ちょー痩せた", LocalDate.of(2026, 7, 9));
    }

    @Test
    void createHealth_登録した記録が返る() {
        HealthRequest request = new HealthRequest();
        ReflectionTestUtils.setField(request, "weight", 65.0);
        ReflectionTestUtils.setField(request, "condition", Conditions.GOOD);
        ReflectionTestUtils.setField(request, "sleepTime", 8.0);
        ReflectionTestUtils.setField(request, "memo", "よく寝れた");
        ReflectionTestUtils.setField(request, "recordDate", LocalDate.of(2026, 7, 9));
        Health health = Health.create(
                request.getWeight(), request.getCondition(), request.getSleepTime(),
                request.getMemo(), request.getRecordDate());
        given(healthRepository.save(any(Health.class))).willReturn(health);

        healthService.createHealth(request);

        ArgumentCaptor<Health> captor = ArgumentCaptor.forClass(Health.class);
        then(healthRepository).should().save(captor.capture());

        assertThat(captor.getValue().getWeight()).isEqualTo(65.0);
        assertThat(captor.getValue().getCondition()).isEqualTo(Conditions.GOOD);
        assertThat(captor.getValue().getSleepTime()).isEqualTo(8.0);
        assertThat(captor.getValue().getMemo()).isEqualTo("よく寝れた");
        assertThat(captor.getValue().getRecordDate()).isEqualTo(LocalDate.of(2026, 7, 9));
    }

    @Test
    void findAll_記録一覧が返る() {
        given(healthRepository.findAll())
                .willReturn(List.of(mayRecord, juneRecord, julyRecord));

        List<Health> result = healthService.findAll();

        then(healthRepository).should().findAll();
        assertThat(result)
                .hasSize(3)
                .extracting(Health::getCondition)
                .containsExactlyInAnyOrder(Conditions.GOOD, Conditions.NORMAL, Conditions.BAD);
    }

    @Test
    void findAll_空のリストが返る() {
        given(healthRepository.findAll()).willReturn(List.of());

        List<Health> result = healthService.findAll();

        then(healthRepository).should().findAll();
        assertThat(result).isEmpty();
    }

    @Test
    void getHealthRecordsInRange_絞り込み結果が返る_両方指定() {
        LocalDate from = LocalDate.of(2026, 6, 1);
        LocalDate to = LocalDate.of(2026, 7, 31);
        given(healthRepository.findByMonthlyHealth(from, to))
                .willReturn(List.of(juneRecord, julyRecord));

        List<Health> result = healthService.getHealthRecordsInRange(from, to);

        then(healthRepository).should()
                .findByMonthlyHealth(from, to);
        assertThat(result)
                .hasSize(2)
                .extracting(Health::getRecordDate)
                .containsExactlyInAnyOrder(
                        juneRecord.getRecordDate(), julyRecord.getRecordDate());
    }

    @Test
    void getHealthRecordsInRange_絞り込み結果が返る_fromのみ指定() {
        LocalDate from = LocalDate.of(2026, 6, 1);
        given(healthRepository.findByMonthlyHealth(from, null))
                .willReturn(List.of(juneRecord, julyRecord));

        List<Health> result = healthService.getHealthRecordsInRange(from, null);

        then(healthRepository).should()
                .findByMonthlyHealth(from, null);
        assertThat(result)
                .hasSize(2)
                .extracting(Health::getRecordDate)
                .containsExactlyInAnyOrder(
                        juneRecord.getRecordDate(), julyRecord.getRecordDate());
    }

    @Test
    void getHealthRecordsInRange_絞り込み結果が返る_toのみ指定() {
        LocalDate to = LocalDate.of(2026, 6, 30);
        given(healthRepository.findByMonthlyHealth(null, to))
                .willReturn(List.of(mayRecord, juneRecord));

        List<Health> result = healthService.getHealthRecordsInRange(null, to);

        then(healthRepository).should()
                .findByMonthlyHealth(null, to);
        assertThat(result)
                .hasSize(2)
                .extracting(Health::getRecordDate)
                .containsExactlyInAnyOrder(
                        mayRecord.getRecordDate(), juneRecord.getRecordDate());
    }

    @Test
    void getHealthRecordsInRange_絞り込み結果が返る_両方null指定() {
        given(healthRepository.findByMonthlyHealth(null, null))
                .willReturn(List.of(mayRecord, juneRecord, julyRecord));

        List<Health> result = healthService.getHealthRecordsInRange(null, null);

        then(healthRepository).should()
                .findByMonthlyHealth(null, null);
        assertThat(result)
                .hasSize(3)
                .extracting(Health::getRecordDate)
                .containsExactlyInAnyOrder(
                        mayRecord.getRecordDate(), juneRecord.getRecordDate(), julyRecord.getRecordDate());
    }

    @Test
    void getMonthlyStats_正常系_指定した年月の平均体重が返る() {
        MonthlyStatsProjection response = mock(MonthlyStatsProjection.class);
        given(response.getYear()).willReturn(2026);
        given(response.getMonth()).willReturn(7);
        given(response.getAvgWeight()).willReturn(62.5);
        given(healthRepository.findMonthlyStats(2026, 7))
                .willReturn(Optional.of(response));

        MonthlyStatsProjection result = healthService.getMonthlyStats(2026, 7);

        then(healthRepository).should().findMonthlyStats(2026, 7);
        assertThat(result.getYear()).isEqualTo(2026);
        assertThat(result.getMonth()).isEqualTo(7);
        assertThat(result.getAvgWeight()).isEqualTo(62.5);
    }

    @Test
    void getMonthlyStats_異常系_HealthNotFoundExceptionがスローされる() {
        given(healthRepository.findMonthlyStats(2025, 1))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> healthService.getMonthlyStats(2025, 1))
                .isInstanceOf(HealthNotFoundException.class)
                .hasMessageContaining("2025年1月");

        then(healthRepository).should().findMonthlyStats(2025, 1);
    }

    @Test
    void updateHealth_正常系_記録が更新される() {
        HealthUpdateRequest request = new HealthUpdateRequest();
        ReflectionTestUtils.setField(request, "weight", 80.0);
        ReflectionTestUtils.setField(request, "condition", Conditions.BAD);
        ReflectionTestUtils.setField(request, "sleepTime", 5.0);
        ReflectionTestUtils.setField(request, "memo", "あまり寝れなかった");
        given(healthRepository.findById(1L))
                .willReturn(Optional.of(mayRecord));

        Health result = healthService.updateHealth(1L, request);

        then(healthRepository).should().findById(1L);
        assertThat(result.getWeight()).isEqualTo(80.0);
        assertThat(result.getCondition()).isEqualTo(Conditions.BAD);
        assertThat(result.getSleepTime()).isEqualTo(5.0);
        assertThat(result.getMemo()).isEqualTo("あまり寝れなかった");
    }

    @Test
    void updateHealth_異常系_HealthNotFoundExceptionがスローされる() {
        given(healthRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> healthService.updateHealth(1L, new HealthUpdateRequest()))
                .isInstanceOf(HealthNotFoundException.class)
                .hasMessageContaining("記録ID");

        then(healthRepository).should().findById(1L);
    }

    @Test
    void deleteHealth_正常系_記録が削除される() {
        given(healthRepository.findById(1L)).willReturn(Optional.of(mayRecord));

        healthService.deleteHealth(1L);

        then(healthRepository).should().findById(1L);
        then(healthRepository).should().delete(mayRecord);
    }

    @Test
    void deleteHealth_異常系_HealthNotFoundExceptionがスローされる() {
        given(healthRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> healthService.deleteHealth(1L))
                .isInstanceOf(HealthNotFoundException.class)
                .hasMessageContaining("記録ID");

        then(healthRepository).should().findById(1L);
    }
}
