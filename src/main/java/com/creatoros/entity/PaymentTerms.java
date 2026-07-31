package com.creatoros.entity;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentTerms {

    NET_15("Net 15", 15),
    NET_30("Net 30", 30),
    NET_45("Net 45", 45),
    SPLIT_50_50("50% Upfront, 50% Post-Publish", 30);

    private final String label;
    private final int    netDays;

    PaymentTerms(String label, int netDays) {
        this.label = label;
        this.netDays = netDays;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    public int getNetDays() {
        return netDays;
    }

    @JsonCreator
    public static PaymentTerms fromLabel(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Arrays.stream(values()).filter(t -> t.label.equalsIgnoreCase(value) || t.name().equalsIgnoreCase(value)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown payment terms: " + value));
    }
}
