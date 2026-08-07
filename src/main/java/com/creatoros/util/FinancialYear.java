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
        int startYear = startYearOf(date);
        return "%d-%02d".formatted(startYear, (startYear + 1) % 100);
    }
}
