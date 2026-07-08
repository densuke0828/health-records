package com.example.health_records.dto;

import com.example.health_records.enums.Conditions;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class HealthUpdateRequest {
    @Min(value = 1, message = "体重は1kg以上で入力してください")
    private Double weight;

    private Conditions condition;

    private Double sleepTime;

    @Size(max = 200, message = "メモは200文字以内で入力してください")
    private String memo;
}
