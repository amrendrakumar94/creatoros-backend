package com.creatoros.enums;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PermissionKey {

    MANAGE_BRANDS("Manage Brands"),
    MANAGE_DEALS("Manage Deals"),
    MANAGE_CAMPAIGNS("Manage Campaigns"),
    MANAGE_CONTENT("Manage Content"),
    MANAGE_DELIVERABLES("Manage Deliverables"),
    MANAGE_FINANCES("Manage Finances"),
    MANAGE_INVOICES("Manage Invoices"),
    MANAGE_PAYMENTS("Manage Payments"),
    MANAGE_EXPENSES("Manage Expenses"),
    VIEW_DASHBOARD("View Dashboard");

    private final String label;

    PermissionKey(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static PermissionKey fromLabel(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Arrays.stream(values()).filter(p -> p.label.equalsIgnoreCase(value) || p.name().equalsIgnoreCase(value)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown permission: " + value));
    }
}
