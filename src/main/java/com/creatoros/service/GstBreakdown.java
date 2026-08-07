package com.creatoros.service;

import java.math.BigDecimal;

public record GstBreakdown(

        BigDecimal cgstRate,

        BigDecimal cgstAmount,

        BigDecimal sgstRate,

        BigDecimal sgstAmount,

        BigDecimal igstRate,

        BigDecimal igstAmount,

        BigDecimal totalTax) {

    public static GstBreakdown zero() {
        return new GstBreakdown(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO);
    }
}
