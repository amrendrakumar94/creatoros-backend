package com.creatoros.dto.invoice;

import com.creatoros.entity.InvoiceStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateInvoiceStatusRequest(
        @NotNull(message = "Status is required")
        InvoiceStatus status,

        /** Only meaningful when moving to Paid; defaults to today. */
        LocalDate paidDate) {
}
