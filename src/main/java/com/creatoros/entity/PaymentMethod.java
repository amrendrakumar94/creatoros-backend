package com.creatoros.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * How an expense was paid.
 *
 * <p>These mirror the fixed union in the frontend's {@code Expense['paymentMethod']}. A
 * creator-owned list of payment accounts would be the better model; that needs a schema change
 * and a UI for managing accounts, so it is deliberately deferred.
 */
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
        return Arrays.stream(values())
                .filter(m -> m.label.equalsIgnoreCase(value) || m.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown payment method: " + value));
    }
}
