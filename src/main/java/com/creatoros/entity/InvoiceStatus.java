package com.creatoros.entity;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum InvoiceStatus {

    DRAFT("Draft"),
    SENT("Sent"),
    VIEWED("Viewed"),
    PARTIALLY_PAID("Partially Paid"),
    PAID("Paid"),
    OVERDUE("Overdue");

    private final String label;

    InvoiceStatus(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    public boolean isBooked() {
        return this != DRAFT;
    }

    public boolean isOutstanding() {
        return this == SENT || this == VIEWED || this == OVERDUE || this == PARTIALLY_PAID;
    }

    @JsonCreator
    public static InvoiceStatus fromLabel(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Arrays.stream(values()).filter(s -> s.label.equalsIgnoreCase(value) || s.name().equalsIgnoreCase(value)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown invoice status: " + value));
    }
}
