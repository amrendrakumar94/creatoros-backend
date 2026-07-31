package com.creatoros.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GstCalculationService {

    private static final BigDecimal GST_RATE        = new BigDecimal("0.18");
    private static final BigDecimal HALF_GST_RATE   = new BigDecimal("0.09");
    private static final BigDecimal TDS_RATE        = new BigDecimal("0.10");
    private static final BigDecimal ITC_NUMERATOR   = new BigDecimal("18");
    private static final BigDecimal ITC_DENOMINATOR = new BigDecimal("118");

    public GstBreakdown calculate(BigDecimal subtotal, boolean interstate) {
        BigDecimal taxable = scale(subtotal == null ? BigDecimal.ZERO : subtotal);

        BigDecimal cgst = BigDecimal.ZERO;
        BigDecimal sgst = BigDecimal.ZERO;
        BigDecimal igst = BigDecimal.ZERO;

        if (interstate) {
            igst = scale(taxable.multiply(GST_RATE));
        } else {
            cgst = scale(taxable.multiply(HALF_GST_RATE));
            sgst = scale(taxable.multiply(HALF_GST_RATE));
        }

        BigDecimal totalGst = scale(cgst.add(sgst).add(igst));
        BigDecimal tds = scale(taxable.multiply(TDS_RATE));
        BigDecimal totalAmount = scale(taxable.add(totalGst));
        BigDecimal netReceivable = scale(totalAmount.subtract(tds));

        return new GstBreakdown(taxable, cgst, sgst, igst, totalGst, tds, totalAmount, netReceivable);
    }

    public BigDecimal calculateInputTaxCredit(BigDecimal grossAmount, boolean hasGstInvoice) {
        if (!hasGstInvoice || grossAmount == null || grossAmount.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return scale(grossAmount.multiply(ITC_NUMERATOR).divide(ITC_DENOMINATOR, 4, RoundingMode.HALF_UP));
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public record GstBreakdown(BigDecimal subtotal, BigDecimal cgst, BigDecimal sgst, BigDecimal igst, BigDecimal totalGst, BigDecimal tdsDeducted,
            BigDecimal totalAmount, BigDecimal netReceivable) {
    }
}
