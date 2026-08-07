package com.creatoros.dto.dashboard;

import java.math.BigDecimal;

import com.creatoros.enums.ExpenseCategory;

public record CategoryTotalDto(

        ExpenseCategory category,

        BigDecimal amount,

        BigDecimal share) {
}
