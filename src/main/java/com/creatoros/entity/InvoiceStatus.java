package com.creatoros.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

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

    /** A draft has not been issued, so it carries no GST or TDS liability. */
    public boolean isBooked() {
        return this != DRAFT;
    }

    /** Still awaiting settlement - drives the payment collection screen. */
    public boolean isOutstanding() {
        return this == SENT || this == VIEWED || this == OVERDUE || this == PARTIALLY_PAID;
    }

    @JsonCreator
    public static InvoiceStatus fromLabel(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Arrays.stream(values())
                .filter(s -> s.label.equalsIgnoreCase(value) || s.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown invoice status: " + value));
    }
}
