package com.creatoros.dto.dashboard;

import java.math.BigDecimal;

public record TdsPositionDto(

        BigDecimal deductedOnInvoices,

        BigDecimal withheldOnPayments,

        BigDecimal awaitingWithholding) {
}
