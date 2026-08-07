package com.creatoros.dto.dashboard;

import java.math.BigDecimal;

public record RevenueSummaryDto(

        BigDecimal recognised,

        BigDecimal invoicedInclusiveOfTax,

        BigDecimal collectedCash,

        BigDecimal outstanding,

        BigDecimal overdue,

        int overdueCount,

        int invoiceCount,

        int paidCount,

        BigDecimal averageInvoiceValue) {
}
