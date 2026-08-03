package com.creatoros.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GstCalculationService {

    private static final BigDecimal ITC_NUMERATOR   = new BigDecimal("18");
    private static final BigDecimal ITC_DENOMINATOR = new BigDecimal("118");

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

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
