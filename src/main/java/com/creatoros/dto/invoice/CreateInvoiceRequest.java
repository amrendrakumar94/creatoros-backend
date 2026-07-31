package com.creatoros.dto.invoice;

import com.creatoros.entity.InvoiceStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/**
 * Create payload for a tax invoice.
 *
 * <p>Only the commercial inputs are accepted. The invoice number, every GST/TDS figure, and the
 * creator identity block are all derived server-side, so a client cannot post an invoice whose
 * tax maths disagrees with the statutory rates.
 */
public record CreateInvoiceRequest(

        @NotBlank(message = "Brand name is required")
        @Size(max = 200)
        String brandName,

        @JsonProperty("brandGSTIN")
        @Pattern(regexp = "^$|^(?i)[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][0-9A-Z][Z][0-9A-Z]$",
                message = "Brand GSTIN must be 15 characters in the standard GSTIN format")
        String brandGstin,

        @Size(max = 500)
        String brandAddress,

        LocalDate issueDate,

        LocalDate dueDate,

        @NotEmpty(message = "At least one invoice line is required")
        @Valid
        List<InvoiceItemDto> items,

        /** True applies IGST at 18%; false splits into CGST 9% + SGST 9%. */
        boolean isInterstate,

        InvoiceStatus status,

        /** Optional link back to the brand deal this invoice bills. */
        String dealId) {
}
