package com.example.health_records.controller;

import com.example.health_records.dto.HealthRequest;
import com.example.health_records.dto.HealthResponse;
import com.example.health_records.dto.HealthUpdateRequest;
import com.example.health_records.dto.MonthlyStatsProjection;
import com.example.health_records.entity.Health;
import com.example.health_records.service.HealthService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/health-records")
public class HealthController {
    private final HealthService healthService;

    @PostMapping
    public ResponseEntity<HealthResponse> createHealth(
            @Validated @RequestBody HealthRequest request) {
        Health health = healthService.createHealth(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(HealthResponse.from(health));
    }

    @GetMapping
    public ResponseEntity<List<HealthResponse>> getHealth(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        List<Health> healths =
                from == null && to == null ?
                        healthService.findAll() :
                        healthService.getHealthRecordsInRange(from, to);
        List<HealthResponse> responses = healths
                .stream()
                .map(HealthResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/stats")
    public ResponseEntity<MonthlyStatsProjection> getMonthlyStats(
            @RequestParam int year, @RequestParam @Min(1) @Max(12)  int month) {
        return ResponseEntity.ok(healthService.getMonthlyStats(year, month));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<HealthResponse> updateHealth(
            @PathVariable Long id, @Validated @RequestBody HealthUpdateRequest request) {
        Health health = healthService.updateHealth(id, request);
        return ResponseEntity.ok(HealthResponse.from(health));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHealth(@PathVariable Long id) {
        healthService.deleteHealth(id);
        return ResponseEntity.noContent().build();
    }
}
