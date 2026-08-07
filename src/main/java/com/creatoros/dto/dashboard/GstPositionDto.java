package com.creatoros.dto.dashboard;

import java.math.BigDecimal;

public record GstPositionDto(

        BigDecimal outputTax,

        BigDecimal inputTaxCredit,

        BigDecimal netPayable,

        boolean registered) {
}
