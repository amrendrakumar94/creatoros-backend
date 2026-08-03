package com.creatoros.enums;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ExpenseCategory {

    EQUIPMENT("Equipment"),
    SOFTWARE_AND_SUBSCRIPTIONS("Software & Subscriptions"),
    TRAVEL_AND_SHOOTS("Travel & Shoots"),
    TEAM_AND_FREELANCERS("Team & Freelancers"),
    OFFICE_AND_STUDIO("Office & Studio"),
    MARKETING("Marketing");

    private final String label;

    ExpenseCategory(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static ExpenseCategory fromLabel(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Arrays.stream(values()).filter(c -> c.label.equalsIgnoreCase(value) || c.name().equalsIgnoreCase(value)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown expense category: " + value));
    }
}
