package com.creatoros.dto.invoice;

import java.time.LocalDate;

import com.creatoros.entity.InvoiceStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateInvoiceStatusRequest(

        @NotNull(message = "Status is required") InvoiceStatus status,

        LocalDate paidDate) {
}
