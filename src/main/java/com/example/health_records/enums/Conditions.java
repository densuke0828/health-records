package com.example.health_records.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Conditions {
    GOOD("良い"),
    NORMAL("普通"),
    BAD("悪い");

    private final String displayName;
}
