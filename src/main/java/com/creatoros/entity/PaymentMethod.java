package com.creatoros.entity;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentMethod {

    HDFC_CREDIT_CARD("HDFC Credit Card"),
    ICICI_BUSINESS_BANK("ICICI Business Bank"),
    UPI("UPI"),
    PETTY_CASH("Petty Cash");

    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static PaymentMethod fromLabel(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Arrays.stream(values()).filter(m -> m.label.equalsIgnoreCase(value) || m.name().equalsIgnoreCase(value)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown payment method: " + value));
    }
}
