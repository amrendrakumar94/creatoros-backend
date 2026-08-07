package com.creatoros.enums;

import java.math.BigDecimal;
import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TdsSection {

    NONE("None", "0"),
    SECTION_194J("194J - Professional Fees (10%)", "10"),
    SECTION_194C("194C - Contract (2%)", "2"),
    SECTION_194H("194H - Commission (5%)", "5");

    private final String     label;
    private final BigDecimal rate;

    TdsSection(String label, String rate) {
        this.label = label;
        this.rate = new BigDecimal(rate);
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    public BigDecimal getRate() {
        return rate;
    }

    @JsonCreator
    public static TdsSection fromLabel(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Arrays.stream(values()).filter(s -> s.label.equalsIgnoreCase(value) || s.name().equalsIgnoreCase(value)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown TDS section: " + value));
    }
}
