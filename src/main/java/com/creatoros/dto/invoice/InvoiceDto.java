package com.creatoros.dto.invoice;

import com.creatoros.enums.InvoiceStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record InvoiceDto(

        String id,

        String invoiceNumber,

        String brandName,

        @JsonProperty("brandGSTIN")
        String brandGstin,

        String brandAddress,

        String creatorName,

        @JsonProperty("creatorGSTIN")
        String creatorGstin,

        @JsonProperty("creatorPAN")
        String creatorPan,

        InvoiceBankDto creatorBankDetails,

        LocalDate issueDate,
        LocalDate dueDate,
        List<InvoiceItemDto> items,
        BigDecimal subtotal,
        boolean isInterstate,
        BigDecimal cgstAmount,
        BigDecimal sgstAmount,
        BigDecimal igstAmount,
        BigDecimal totalGst,
        BigDecimal tdsDeducted,
        BigDecimal totalAmount,
        BigDecimal netReceivable,
        InvoiceStatus status,
        String dealId,
        LocalDate paidDate,
        int reminderSentCount,
        LocalDate lastReminderDate,
        LocalDate expectedSettlementDate) {
}
