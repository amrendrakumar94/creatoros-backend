package com.creatoros.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GstCalculationServiceTest {

    private final GstCalculationService service = new GstCalculationService();

    @Test
    @DisplayName("extracts the full 18% component baked into a GST-inclusive amount")
    void extractsInclusiveGstComponent() {
        assertThat(service.calculateInputTaxCredit(new BigDecimal("118000"), true)).isEqualByComparingTo("18000.00");
    }

    @ParameterizedTest
    @CsvSource({ "1000, 152.54", "1180, 180.00", "2499.99, 381.35", "0.01, 0.00" })
    @DisplayName("rounds the extracted credit half-up to paise")
    void roundsHalfUpToPaise(String gross, String expected) {
        assertThat(service.calculateInputTaxCredit(new BigDecimal(gross), true)).isEqualByComparingTo(expected);
    }

    @Test
    @DisplayName("claims nothing without a GST invoice, however large the spend")
    void claimsNothingWithoutGstInvoice() {
        assertThat(service.calculateInputTaxCredit(new BigDecimal("118000"), false)).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("treats a missing amount as nothing to claim rather than failing")
    void treatsNullAmountAsZero() {
        assertThat(service.calculateInputTaxCredit(null, true)).isEqualByComparingTo("0");
    }

    @ParameterizedTest
    @CsvSource({ "0", "-1", "-5000.50" })
    @DisplayName("never returns a negative or zero-spend credit")
    void rejectsNonPositiveAmounts(String gross) {
        assertThat(service.calculateInputTaxCredit(new BigDecimal(gross), true)).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("always returns a 2dp scale so stored and returned values agree")
    void alwaysScalesToTwoDecimals() {
        assertThat(service.calculateInputTaxCredit(new BigDecimal("118000"), true).scale()).isEqualTo(2);
    }
}
