package com.creatoros.dto.dashboard;

import java.math.BigDecimal;

public record ExpenseSummaryDto(

        BigDecimal grossPaid,

        BigDecimal reclaimableItc,

        BigDecimal netOfItc,

        BigDecimal taxDeductible,

        int count) {
}
