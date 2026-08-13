package com.creatoros.enums;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum QuotationStatus {

    DRAFT("Draft"),
    SENT("Sent"),
    ACCEPTED("Accepted"),
    REJECTED("Rejected"),
    EXPIRED("Expired");

    private final String label;

    QuotationStatus(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    public boolean isFinal() {
        return this == ACCEPTED || this == REJECTED || this == EXPIRED;
    }

    public boolean isEditable() {
        return !isFinal();
    }

    @JsonCreator
    public static QuotationStatus fromLabel(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Arrays.stream(values()).filter(s -> s.label.equalsIgnoreCase(value) || s.name().equalsIgnoreCase(value)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown quotation status: " + value));
    }
}
