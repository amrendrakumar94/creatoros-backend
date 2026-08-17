package com.creatoros.enums;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SettlementMethod {

    NEFT("NEFT"),
    RTGS("RTGS"),
    IMPS("IMPS"),
    UPI("UPI"),
    CHEQUE("Cheque"),
    CASH("Cash"),
    RAZORPAY("Razorpay"),
    OTHER("Other");

    private final String label;

    SettlementMethod(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static SettlementMethod fromLabel(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Arrays.stream(values()).filter(m -> m.label.equalsIgnoreCase(value) || m.name().equalsIgnoreCase(value)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown settlement method: " + value));
    }
}
