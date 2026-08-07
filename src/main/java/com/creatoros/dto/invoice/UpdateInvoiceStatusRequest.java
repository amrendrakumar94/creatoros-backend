package com.creatoros.dto.invoice;

import com.creatoros.enums.InvoiceStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateInvoiceStatusRequest(

        @NotNull(message = "Status is required") InvoiceStatus status) {
}
