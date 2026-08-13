package com.creatoros.dto.quotation;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record QuotationLineItemDto(

        String id,

        @NotBlank(message = "Describe what you are quoting for") @Size(max = 500) String description,

        @Size(max = 10) String sacCode,

        @NotNull(message = "Quantity is required") @Positive(message = "Quantity must be greater than zero") BigDecimal quantity,

        @Size(max = 20) String unit,

        @NotNull(message = "Rate is required") @PositiveOrZero(message = "Rate cannot be negative") BigDecimal rate,

        BigDecimal gstRate,

        BigDecimal taxableAmount) {
}
