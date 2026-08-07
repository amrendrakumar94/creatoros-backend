package com.creatoros.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FinancialYearTest {

    @ParameterizedTest
    @CsvSource({ "2026-04-01, 2026", "2026-08-08, 2026", "2026-12-31, 2026", "2027-01-01, 2026", "2027-03-31, 2026", "2027-04-01, 2027" })
    @DisplayName("rolls the year on 1 April, not 1 January")
    void rollsOnFirstOfApril(String date, int expectedStartYear) {
        assertThat(FinancialYear.startYearOf(LocalDate.parse(date))).isEqualTo(expectedStartYear);
    }

    @ParameterizedTest
    @CsvSource({ "2026-04-01, 2026-27", "2027-03-31, 2026-27", "2027-04-01, 2027-28", "2029-05-10, 2029-30", "2030-02-15, 2029-30" })
    @DisplayName("labels the year the way the UI does")
    void labelsMatchFrontendConvention(String date, String expectedLabel) {
        assertThat(FinancialYear.labelOf(LocalDate.parse(date))).isEqualTo(expectedLabel);
    }

    @ParameterizedTest
    @CsvSource({ "2099-04-01, 2099-00", "2100-04-01, 2100-01" })
    @DisplayName("pads the century rollover rather than emitting a single digit")
    void padsCenturyRollover(String date, String expectedLabel) {
        assertThat(FinancialYear.labelOf(LocalDate.parse(date))).isEqualTo(expectedLabel);
    }
}
