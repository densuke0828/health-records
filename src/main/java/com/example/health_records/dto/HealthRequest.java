package com.example.health_records.dto;

import com.example.health_records.enums.Conditions;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class HealthRequest {
    @NotNull(message = "体重は必須です")
    @Min(value = 1, message = "体重は1kg以上で入力してください")
    private Double weight;

    @NotNull(message = "体調は必須です")
    private Conditions condition;

    @NotNull(message = "睡眠時間は必須です")
    private Double sleepTime;

    @Size(max = 200, message = "メモは200文字以内で入力してください")
    private String memo;

    @NotNull(message = "日付は必須です")
    private LocalDate recordDate;
}
