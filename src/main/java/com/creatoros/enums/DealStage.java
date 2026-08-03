package com.creatoros.enums;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DealStage {

    LEAD("Lead"),
    PITCH_SENT("Pitch Sent"),
    CONTRACT_REVIEW("Contract Review"),
    CONTENT_APPROVED("Content Approved"),
    INVOICE_SENT("Invoice Sent"),
    PAYMENT_RECEIVED("Payment Received");

    private final String label;

    DealStage(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static DealStage fromLabel(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Arrays.stream(values()).filter(s -> s.label.equalsIgnoreCase(value) || s.name().equalsIgnoreCase(value)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown deal stage: " + value));
    }
}
