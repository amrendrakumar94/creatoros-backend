package com.creatoros.dto.expense;

import com.creatoros.entity.ExpenseCategory;
import com.creatoros.entity.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Create/update payload for an expense. {@code gstClaimableAmount} is absent by design - the
 * server derives the input tax credit from the amount and whether a GST invoice exists.
 */
public record ExpenseRequest(

        @NotBlank(message = "Title is required")
        @Size(max = 300)
        String title,

        @NotNull(message = "Category is required")
        ExpenseCategory category,

        @NotNull(message = "Amount is required")
        @PositiveOrZero(message = "Amount cannot be negative")
        BigDecimal amount,

        LocalDate date,

        @Size(max = 200)
        String vendor,

        @Pattern(regexp = "^$|^(?i)[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][0-9A-Z][Z][0-9A-Z]$",
                message = "Vendor GSTIN must be 15 characters in the standard GSTIN format")
        String gstin,

        boolean hasGstInvoice,

        @Size(max = 500)
        String receiptUrl,

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod,

        String notes,

        Boolean taxDeductible) {
}
