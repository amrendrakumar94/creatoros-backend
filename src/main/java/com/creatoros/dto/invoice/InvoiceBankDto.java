package com.creatoros.dto.invoice;

/** Payout block printed on the invoice; snapshotted from the profile at issue time. */
public record InvoiceBankDto(
        String bankName,
        String accountNumber,
        String ifscCode,
        String upiId) {
}
