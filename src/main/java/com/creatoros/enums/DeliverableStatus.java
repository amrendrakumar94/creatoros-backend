package com.creatoros.enums;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DeliverableStatus {

    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    SUBMITTED_FOR_REVIEW("Submitted for Review"),
    APPROVED("Approved"),
    PUBLISHED("Published");

    private final String label;

    DeliverableStatus(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static DeliverableStatus fromLabel(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Arrays.stream(values()).filter(s -> s.label.equalsIgnoreCase(value) || s.name().equalsIgnoreCase(value)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown deliverable status: " + value));
    }
}
