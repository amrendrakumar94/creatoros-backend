package com.creatoros.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Authoritative Indian GST and TDS maths for creator service invoices.
 *
 * <p>This is the server-side counterpart of {@code calculateInvoiceBreakdown} in the frontend's
 * utils/formatters.ts. The client copy is now only a preview: every stored figure comes from
 * here, so a tampered or stale client cannot persist an invoice with incorrect tax.
 *
 * <p>Rates: 18% GST on advertising services (SAC 9983xx), split CGST 9% + SGST 9% for an
 * intra-state supply or charged as IGST 18% inter-state. TDS is 10% of the taxable value under
 * section 194J, withheld by the brand and claimed back by the creator in their ITR.
 */
@Service
@Slf4j
public class GstCalculationService {

    private static final BigDecimal GST_RATE = new BigDecimal("0.18");
    private static final BigDecimal HALF_GST_RATE = new BigDecimal("0.09");
    private static final BigDecimal TDS_RATE = new BigDecimal("0.10");
    private static final BigDecimal ITC_NUMERATOR = new BigDecimal("18");
    private static final BigDecimal ITC_DENOMINATOR = new BigDecimal("118");

    /**
     * @param subtotal     taxable value before tax
     * @param interstate   true charges IGST, false splits CGST + SGST
     */
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

    /**
     * Input tax credit embedded in a GST-inclusive expense: amount x 18/118.
     *
     * @return zero when the vendor did not issue a GST invoice, since nothing is claimable then
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

    /** Immutable result of a tax computation. */
    public record GstBreakdown(
            BigDecimal subtotal,
            BigDecimal cgst,
            BigDecimal sgst,
            BigDecimal igst,
            BigDecimal totalGst,
            BigDecimal tdsDeducted,
            BigDecimal totalAmount,
            BigDecimal netReceivable) {
    }
}
