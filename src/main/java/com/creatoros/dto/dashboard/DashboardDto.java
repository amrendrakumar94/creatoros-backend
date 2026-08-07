package com.creatoros.dto.dashboard;

import java.math.BigDecimal;
import java.util.List;

public record DashboardDto(

        String financialYear,

        String financialYearLabel,

        RevenueSummaryDto revenue,

        ExpenseSummaryDto expenses,

        GstPositionDto gst,

        TdsPositionDto tds,

        PipelineSummaryDto pipeline,

        BigDecimal netProfit,

        BigDecimal profitMargin,

        BigDecimal netCashFlow,

        List<MonthlyPointDto> monthly,

        List<CategoryTotalDto> expenseByCategory) {
}
