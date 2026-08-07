package com.creatoros.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;

import com.creatoros.enums.GstStateCode;
import com.creatoros.enums.TdsSection;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GstCalculationService {

    private static final BigDecimal ITC_NUMERATOR   = new BigDecimal("18");
    private static final BigDecimal ITC_DENOMINATOR = new BigDecimal("118");
    private static final BigDecimal HUNDRED         = new BigDecimal("100");
    private static final BigDecimal TWO             = new BigDecimal("2");

    /**
     * Input tax credit reclaimable on a GST-invoiced business expense: the 18% GST component
     * already baked into the gross amount paid.
     */
    public BigDecimal calculateInputTaxCredit(BigDecimal grossAmount, boolean hasGstInvoice) {
        if (!hasGstInvoice || grossAmount == null || grossAmount.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return scale(grossAmount.multiply(ITC_NUMERATOR).divide(ITC_DENOMINATOR, 4, RoundingMode.HALF_UP));
    }

    public boolean isInterState(String supplierStateCode, String placeOfSupplyCode) {
        if (supplierStateCode == null || supplierStateCode.isBlank() || placeOfSupplyCode == null || placeOfSupplyCode.isBlank()) {
            return false;
        }
        return !supplierStateCode.trim().equals(placeOfSupplyCode.trim());
    }

    public GstBreakdown splitGst(BigDecimal taxableAmount, BigDecimal gstRate, boolean interState) {
        if (taxableAmount == null || taxableAmount.signum() <= 0 || gstRate == null || gstRate.signum() <= 0) {
            return GstBreakdown.zero();
        }

        if (interState) {
            BigDecimal igst = percentOf(taxableAmount, gstRate);
            return new GstBreakdown(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, scale(gstRate), igst, igst);
        }

        BigDecimal halfRate = gstRate.divide(TWO, 4, RoundingMode.HALF_UP);
        BigDecimal half = percentOf(taxableAmount, halfRate);
        return new GstBreakdown(scale(halfRate), half, scale(halfRate), half, BigDecimal.ZERO, BigDecimal.ZERO, half.add(half));
    }

    /**
     * TDS is deducted on the value of services excluding GST, per CBDT Circular 23/2017, whenever
     * the tax component is shown separately on the invoice.
     */
    public BigDecimal calculateTds(BigDecimal taxableAmount, TdsSection section) {
        if (taxableAmount == null || taxableAmount.signum() <= 0 || section == null || section == TdsSection.NONE) {
            return BigDecimal.ZERO;
        }
        return percentOf(taxableAmount, section.getRate());
    }

    public String resolvePlaceOfSupplyName(String stateCode) {
        return GstStateCode.ofCode(stateCode).map(GstStateCode::getStateName).orElse(null);
    }

    private BigDecimal percentOf(BigDecimal amount, BigDecimal rate) {
        return scale(amount.multiply(rate).divide(HUNDRED, 4, RoundingMode.HALF_UP));
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
