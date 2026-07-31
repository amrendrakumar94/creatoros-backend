package com.creatoros.dto.expense;

import com.creatoros.entity.ExpenseCategory;
import com.creatoros.entity.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Mirrors the {@code Expense} interface in src/types/creatorOS.ts. */
public record ExpenseDto(
        String id,
        String title,
        ExpenseCategory category,
        BigDecimal amount,
        LocalDate date,
        String vendor,
        String gstin,
        boolean hasGstInvoice,
        BigDecimal gstClaimableAmount,
        String receiptUrl,
        PaymentMethod paymentMethod,
        String notes,
        boolean taxDeductible) {
}
