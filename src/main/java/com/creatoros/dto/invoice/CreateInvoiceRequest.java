package com.creatoros.dto.invoice;

import java.time.LocalDate;
import java.util.List;

import com.creatoros.enums.InvoiceStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateInvoiceRequest(

        @NotBlank(message = "Brand name is required") @Size(max = 200) String brandName,

        @JsonProperty("brandGSTIN") @Pattern(regexp = "^$|^(?i)[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][0-9A-Z][Z][0-9A-Z]$", message = "Brand GSTIN must be 15 characters in the standard GSTIN format") String brandGstin,

        @Size(max = 500) String brandAddress,

        LocalDate issueDate,

        LocalDate dueDate,

        @NotEmpty(message = "At least one invoice line is required") @Valid List<InvoiceItemDto> items,

        boolean isInterstate,

        InvoiceStatus status,

        String dealId) {
}
