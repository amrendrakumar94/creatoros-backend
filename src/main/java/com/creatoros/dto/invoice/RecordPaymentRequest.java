package com.creatoros.dto.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.creatoros.enums.SettlementMethod;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record RecordPaymentRequest(

        @NotNull(message = "Amount received is required") @Positive(message = "Amount received must be greater than zero") BigDecimal amount,

        LocalDate receivedOn,

        @NotNull(message = "How the money arrived is required") SettlementMethod method,

        @Size(max = 120) String reference,

        @PositiveOrZero(message = "TDS withheld cannot be negative") BigDecimal tdsWithheld,

        String notes) {
}
