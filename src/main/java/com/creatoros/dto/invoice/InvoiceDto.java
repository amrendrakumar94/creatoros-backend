package com.creatoros.dto.invoice;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

import com.creatoros.enums.InvoiceStatus;
import com.creatoros.enums.PaymentTerms;
import com.creatoros.enums.TdsSection;

public record InvoiceDto(

        String id,

        String invoiceNumber,

        String financialYear,

        String dealId,

        String dealNumber,

        InvoiceStatus status,

        boolean overdue,

        long daysOverdue,

        LocalDate issueDate,

        LocalDate dueDate,

        PaymentTerms paymentTerms,

        InvoicePartyDto supplier,

        InvoicePartyDto buyer,

        String placeOfSupplyState,

        String placeOfSupplyCode,

        boolean interState,

        boolean reverseCharge,

        boolean taxInvoice,

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

        BigDecimal invoiceTotal,

        TdsSection tdsSection,

        BigDecimal tdsRate,

        BigDecimal tdsAmount,

        BigDecimal netReceivable,

        BigDecimal amountPaid,

        BigDecimal tdsWithheld,

        BigDecimal balanceDue,

        String notes,

        String terms,

        List<InvoiceLineItemDto> lineItems,

        List<InvoicePaymentDto> payments,

        Timestamp scheduledSendAt,

        String scheduledSendEmail,

        Timestamp lastEmailedAt) {
}
