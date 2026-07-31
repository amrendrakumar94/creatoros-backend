package com.creatoros.dto.expense;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.creatoros.entity.ExpenseCategory;
import com.creatoros.entity.PaymentMethod;

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
