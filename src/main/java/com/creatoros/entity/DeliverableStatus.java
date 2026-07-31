package com.creatoros.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/** Production state of a single deliverable. */
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
        return Arrays.stream(values())
                .filter(s -> s.label.equalsIgnoreCase(value) || s.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown deliverable status: " + value));
    }
}
