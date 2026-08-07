package com.creatoros.dto.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.creatoros.enums.SettlementMethod;

public record InvoicePaymentDto(

        String id,

        BigDecimal amount,

        LocalDate receivedOn,

        SettlementMethod method,

        String reference,

        BigDecimal tdsWithheld,

        String notes) {
}
