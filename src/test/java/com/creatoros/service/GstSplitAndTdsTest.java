package com.creatoros.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.creatoros.enums.TdsSection;

class GstSplitAndTdsTest {

    private static final BigDecimal EIGHTEEN = new BigDecimal("18");
    private static final BigDecimal LAKH     = new BigDecimal("100000");

    private final GstCalculationService service = new GstCalculationService();

    @Test
    @DisplayName("splits an intra-state supply into equal CGST and SGST halves")
    void splitsIntraStateIntoHalves() {
        GstBreakdown breakdown = service.splitGst(LAKH, EIGHTEEN, false);

        assertThat(breakdown.cgstRate()).isEqualByComparingTo("9.00");
        assertThat(breakdown.cgstAmount()).isEqualByComparingTo("9000.00");
        assertThat(breakdown.sgstRate()).isEqualByComparingTo("9.00");
        assertThat(breakdown.sgstAmount()).isEqualByComparingTo("9000.00");
        assertThat(breakdown.igstAmount()).isEqualByComparingTo("0");
        assertThat(breakdown.totalTax()).isEqualByComparingTo("18000.00");
    }

    @Test
    @DisplayName("charges a single IGST line on an inter-state supply")
    void chargesIgstOnInterStateSupply() {
        GstBreakdown breakdown = service.splitGst(LAKH, EIGHTEEN, true);

        assertThat(breakdown.igstRate()).isEqualByComparingTo("18.00");
        assertThat(breakdown.igstAmount()).isEqualByComparingTo("18000.00");
        assertThat(breakdown.cgstAmount()).isEqualByComparingTo("0");
        assertThat(breakdown.sgstAmount()).isEqualByComparingTo("0");
        assertThat(breakdown.totalTax()).isEqualByComparingTo("18000.00");
    }

    @Test
    @DisplayName("keeps CGST plus SGST exactly equal to the total tax when halves round")
    void halvesAlwaysReconcileToTotalTax() {
        GstBreakdown breakdown = service.splitGst(new BigDecimal("1055.55"), EIGHTEEN, false);

        assertThat(breakdown.cgstAmount().add(breakdown.sgstAmount())).isEqualByComparingTo(breakdown.totalTax());
    }

    @ParameterizedTest
    @CsvSource({ "0, false", "-1, false", "0, true", "-1, true" })
    @DisplayName("taxes nothing on a non-positive taxable value")
    void taxesNothingOnNonPositiveValue(String taxable, boolean interState) {
        assertThat(service.splitGst(new BigDecimal(taxable), EIGHTEEN, interState).totalTax()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("treats a missing GST rate as an exempt supply rather than failing")
    void treatsMissingRateAsExempt() {
        assertThat(service.splitGst(LAKH, null, false).totalTax()).isEqualByComparingTo("0");
        assertThat(service.splitGst(LAKH, BigDecimal.ZERO, true).totalTax()).isEqualByComparingTo("0");
    }

    @ParameterizedTest
    @CsvSource({ "SECTION_194J, 10000.00", "SECTION_194C, 2000.00", "SECTION_194H, 5000.00", "NONE, 0" })
    @DisplayName("deducts TDS on the pre-GST taxable value, per CBDT Circular 23/2017")
    void deductsTdsOnPreGstValue(TdsSection section, String expected) {
        assertThat(service.calculateTds(LAKH, section)).isEqualByComparingTo(expected);
    }

    @Test
    @DisplayName("nets a 194J invoice down to the amount the brand will actually pay")
    void netsInvoiceToAmountBrandPays() {
        GstBreakdown breakdown = service.splitGst(LAKH, EIGHTEEN, false);
        BigDecimal invoiceTotal = LAKH.add(breakdown.totalTax());
        BigDecimal tds = service.calculateTds(LAKH, TdsSection.SECTION_194J);

        assertThat(invoiceTotal).isEqualByComparingTo("118000.00");
        assertThat(tds).isEqualByComparingTo("10000.00");
        assertThat(invoiceTotal.subtract(tds)).isEqualByComparingTo("108000.00");
    }

    @ParameterizedTest
    @CsvSource({ "29, 29, false", "29, 27, true", "07, 07, false", "27, 29, true" })
    @DisplayName("reads inter-state from the supplier and place-of-supply codes")
    void derivesInterStateFromStateCodes(String supplier, String placeOfSupply, boolean expected) {
        assertThat(service.isInterState(supplier, placeOfSupply)).isEqualTo(expected);
    }

    @Test
    @DisplayName("falls back to intra-state when a state code is missing rather than guessing IGST")
    void fallsBackToIntraStateWhenCodeMissing() {
        assertThat(service.isInterState(null, "29")).isFalse();
        assertThat(service.isInterState("29", null)).isFalse();
        assertThat(service.isInterState("", "")).isFalse();
    }

    @Test
    @DisplayName("resolves a place-of-supply name for the printed invoice")
    void resolvesPlaceOfSupplyName() {
        assertThat(service.resolvePlaceOfSupplyName("29")).isEqualTo("Karnataka");
        assertThat(service.resolvePlaceOfSupplyName("97")).isEqualTo("Other Territory");
        assertThat(service.resolvePlaceOfSupplyName("25")).isNull();
    }
}
