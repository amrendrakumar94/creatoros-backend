package com.creatoros.dto.quotation;

import com.creatoros.enums.QuotationStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateQuotationStatusRequest(

        @NotNull(message = "Status is required") QuotationStatus status) {
}
