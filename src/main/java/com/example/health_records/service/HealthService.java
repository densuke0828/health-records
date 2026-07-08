package com.example.health_records.service;

import com.example.health_records.dto.HealthRequest;
import com.example.health_records.dto.HealthUpdateRequest;
import com.example.health_records.dto.MonthlyStatsProjection;
import com.example.health_records.entity.Health;
import com.example.health_records.repository.HealthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HealthService {

    private final HealthRepository healthRepository;

    @Transactional(readOnly = false)
    public Health createHealth(HealthRequest request) {
        return healthRepository.save(Health.create(
                request.getWeight(), request.getCondition(), request.getSleepTime(),
                request.getMemo(), request.getRecordDate()));
    }

    public List<Health> findAll() {
        return healthRepository.findAll();
    }

    public List<Health> getHealthRecordsInRange(LocalDate from, LocalDate to) {
        return healthRepository.findByMonthlyHealth(from, to);
    }

    public MonthlyStatsProjection getMonthlyStats(int year, int month) {
        return healthRepository.findMonthlyStats(year, month)
                .orElseThrow(() -> new NoSuchElementException("指定した年月のデータがありません"));
    }

    @Transactional(readOnly = false)
    public Health updateHealth(Long id, HealthUpdateRequest request) {
        Health foundHealth = healthRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("指定されたデータがありません"));
        foundHealth.update(request.getWeight(), request.getCondition(),
                            request.getSleepTime(), request.getMemo());
        return foundHealth;
    }

    @Transactional(readOnly = false)
    public void deleteHealth(Long id) {
        Health foundHealth = healthRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("指定されたデータがありません"));
        healthRepository.delete(foundHealth);
    }
}
