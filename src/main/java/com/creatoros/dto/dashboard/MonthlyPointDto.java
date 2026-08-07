package com.creatoros.dto.dashboard;

import java.math.BigDecimal;

public record MonthlyPointDto(

        String month,

        String label,

        BigDecimal revenue,

        BigDecimal collected,

        BigDecimal expenses,

        BigDecimal profit,

        BigDecimal cashFlow) {
}
