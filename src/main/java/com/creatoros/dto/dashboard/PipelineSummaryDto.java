package com.creatoros.dto.dashboard;

import java.math.BigDecimal;

public record PipelineSummaryDto(

        int openDealCount,

        BigDecimal openDealValue,

        int uninvoicedDealCount,

        BigDecimal uninvoicedDealValue) {
}
