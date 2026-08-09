package com.creatoros.enums;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Role {

    CREATOR("Creator"),
    ADMIN("Admin"),
    MANAGER("Manager"),
    EDITOR("Editor"),
    ACCOUNTANT("Accountant");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    public String asAuthority() {
        return "ROLE_" + name();
    }

    public boolean isTeamAssignable() {
        return this == MANAGER || this == EDITOR || this == ACCOUNTANT;
    }

    @JsonCreator
    public static Role fromLabel(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Arrays.stream(values()).filter(r -> r.label.equalsIgnoreCase(value) || r.name().equalsIgnoreCase(value)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown role: " + value));
    }
}
