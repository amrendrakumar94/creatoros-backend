package com.creatoros.entity;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum NotificationType {

    PAYMENT("payment"),
    INVOICE("invoice"),
    TAX("tax"),
    DEAL("deal"),
    SYSTEM("system");

    private final String label;

    NotificationType(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static NotificationType fromLabel(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Arrays.stream(values()).filter(t -> t.label.equalsIgnoreCase(value) || t.name().equalsIgnoreCase(value)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown notification type: " + value));
    }
}
