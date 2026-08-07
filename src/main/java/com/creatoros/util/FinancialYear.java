package com.creatoros.util;

import java.time.LocalDate;

public final class FinancialYear {

    private static final int APRIL = 4;

    private FinancialYear() {
    }

    public static int startYearOf(LocalDate date) {
        return date.getMonthValue() >= APRIL ? date.getYear() : date.getYear() - 1;
    }

    public static String labelOf(LocalDate date) {
        return labelOfStartYear(startYearOf(date));
    }

    public static String labelOfStartYear(int startYear) {
        return "%d-%02d".formatted(startYear, (startYear + 1) % 100);
    }

    public static int startYearOfLabel(String label) {
        if (label == null || !label.matches("\\d{4}-\\d{2}")) {
            throw new IllegalArgumentException("Financial year must look like 2026-27, got: " + label);
        }
        return Integer.parseInt(label.substring(0, 4));
    }
}
