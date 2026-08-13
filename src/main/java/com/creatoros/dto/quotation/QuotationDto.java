package com.creatoros.dto.quotation;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

import com.creatoros.enums.QuotationStatus;
import com.creatoros.enums.TdsSection;

public record QuotationDto(

        String id,

        String quotationNumber,

        String financialYear,

        String dealId,

        String dealNumber,

        QuotationStatus status,

        LocalDate issueDate,

        LocalDate validUntil,

        boolean expired,

        QuotationPartyDto supplier,

        QuotationPartyDto buyer,

        String placeOfSupplyState,

        String placeOfSupplyCode,

        boolean interState,

        boolean reverseCharge,

        boolean taxQuotation,

        BigDecimal subtotal,

        BigDecimal discountAmount,

        BigDecimal taxableValue,

        BigDecimal cgstRate,

        BigDecimal cgstAmount,

        BigDecimal sgstRate,

        BigDecimal sgstAmount,

        BigDecimal igstRate,

        BigDecimal igstAmount,

        BigDecimal totalTax,

        BigDecimal quotationTotal,

        TdsSection tdsSection,

        BigDecimal tdsRate,

        BigDecimal tdsAmount,

        String notes,

        String terms,

        List<QuotationLineItemDto> lineItems,

        Timestamp scheduledSendAt,

        String scheduledSendEmail,

        Timestamp lastEmailedAt,

        String convertedInvoiceId,

        String convertedInvoiceNumber) {
}
