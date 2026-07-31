package com.creatoros.dto.invoice;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record InvoiceItemDto(String id,

        @Size(max = 500) String description,

        @Size(max = 20) String sacCode,

        @Positive(message = "Quantity must be at least 1") Integer quantity,

        @NotNull(message = "Unit price is required") @PositiveOrZero(message = "Unit price cannot be negative") BigDecimal unitPrice,

        BigDecimal amount) {
}
