package com.creatoros.dto.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.creatoros.enums.PaymentTerms;
import com.creatoros.enums.TdsSection;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record InvoiceRequest(

        String dealId,

        @NotBlank(message = "Brand name is required") @Size(max = 200) String buyerName,

        @Size(max = 200) String buyerLegalName,

        @Pattern(regexp = "^$|^(?i)[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][0-9A-Z][Z][0-9A-Z]$", message = "Buyer GSTIN must be in the standard 15-character format") String buyerGstin,

        @Email(message = "Enter a valid buyer email") @Size(max = 255) String buyerEmail,

        @Size(max = 500) String buyerAddress,

        @Size(max = 120) String buyerCity,

        @Pattern(regexp = "^$|^(0[1-9]|[12][0-9]|3[0-8]|97)$", message = "Buyer state code must be a valid 2-digit GST state code") String buyerStateCode,

        @Pattern(regexp = "^$|^[1-9][0-9]{5}$", message = "Buyer pincode must be 6 digits") String buyerPincode,

        LocalDate issueDate,

        PaymentTerms paymentTerms,

        @PositiveOrZero(message = "GST rate cannot be negative") @DecimalMax(value = "28.00", message = "GST rate cannot exceed 28%") BigDecimal gstRate,

        @PositiveOrZero(message = "Discount cannot be negative") BigDecimal discountAmount,

        TdsSection tdsSection,

        Boolean reverseCharge,

        String notes,

        String terms,

        @NotEmpty(message = "An invoice needs at least one line item") @Valid List<InvoiceLineItemDto> lineItems) {
}
