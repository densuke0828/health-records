package com.example.health_records.exception;

public class HealthNotFoundException extends RuntimeException {
    public HealthNotFoundException(Long id) {
        super("記録ID: " + id + " が見つかりません");
    }

    public HealthNotFoundException(String message) {
        super(message);
    }

    public String getUserMessage() {
        return "指定された記録がありません";
    }
}
