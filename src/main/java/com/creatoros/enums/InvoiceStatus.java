package com.creatoros.enums;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum InvoiceStatus {

    DRAFT("Draft"),
    SENT("Sent"),
    PARTIALLY_PAID("Partially Paid"),
    PAID("Paid"),
    CANCELLED("Cancelled");

    private final String label;

    InvoiceStatus(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    public boolean isSettleable() {
        return this == SENT || this == PARTIALLY_PAID;
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
