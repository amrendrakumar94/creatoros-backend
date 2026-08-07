package com.creatoros.serviceimpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.creatoros.dto.dashboard.CategoryTotalDto;
import com.creatoros.dto.dashboard.DashboardDto;
import com.creatoros.dto.dashboard.ExpenseSummaryDto;
import com.creatoros.dto.dashboard.GstPositionDto;
import com.creatoros.dto.dashboard.MonthlyPointDto;
import com.creatoros.dto.dashboard.PipelineSummaryDto;
import com.creatoros.dto.dashboard.RevenueSummaryDto;
import com.creatoros.dto.dashboard.TdsPositionDto;
import com.creatoros.entity.BrandDeal;
import com.creatoros.entity.Expense;
import com.creatoros.entity.Invoice;
import com.creatoros.entity.InvoicePayment;
import com.creatoros.enums.DealStage;
import com.creatoros.enums.ExpenseCategory;
import com.creatoros.util.FinancialYear;

@Component
public class DashboardAssembler {

    private static final BigDecimal HUNDRED     = new BigDecimal("100");
    private static final int        FY_MONTHS   = 12;
    private static final int        APRIL       = 4;

    public DashboardDto assemble(int startYear, boolean gstRegistered, List<Invoice> allInvoices, List<InvoicePayment> allPayments,
            List<Expense> allExpenses, List<BrandDeal> allDeals, LocalDate asOf) {

        List<Invoice> issued = allInvoices.stream().filter(i -> i.getStatus().isIssued()).filter(i -> inYear(i.getIssueDate(), startYear))
                .toList();
        List<InvoicePayment> payments = allPayments.stream().filter(p -> inYear(p.getReceivedOn(), startYear)).toList();
        List<Expense> expenses = allExpenses.stream().filter(e -> inYear(e.getExpenseDate(), startYear)).toList();

        RevenueSummaryDto revenue = revenueOf(issued, payments, asOf);
        ExpenseSummaryDto expenseSummary = expensesOf(expenses);

        BigDecimal netProfit = revenue.recognised().subtract(expenseSummary.netOfItc());
        BigDecimal margin = revenue.recognised().signum() == 0 ? BigDecimal.ZERO
                : scale(netProfit.multiply(HUNDRED).divide(revenue.recognised(), 4, RoundingMode.HALF_UP));

        return new DashboardDto(FinancialYear.labelOfStartYear(startYear), "FY %s".formatted(FinancialYear.labelOfStartYear(startYear)), revenue,
                expenseSummary, gstOf(issued, expenseSummary, gstRegistered), tdsOf(issued, payments), pipelineOf(allDeals, allInvoices),
                netProfit, margin, revenue.collectedCash().subtract(expenseSummary.grossPaid()),
                monthlyOf(startYear, issued, payments, expenses), categoriesOf(expenses, expenseSummary.grossPaid()));
    }

    private RevenueSummaryDto revenueOf(List<Invoice> issued, List<InvoicePayment> payments, LocalDate asOf) {
        BigDecimal recognised = sum(issued, i -> i.getSubtotal().subtract(i.getDiscountAmount()));
        BigDecimal inclusive = sum(issued, Invoice::getInvoiceTotal);
        BigDecimal collected = sum(payments, InvoicePayment::getAmount);
        BigDecimal outstanding = sum(issued, Invoice::getBalanceDue);

        List<Invoice> overdue = issued.stream().filter(i -> i.isOverdue(asOf)).toList();
        int paidCount = (int) issued.stream().filter(i -> i.getBalanceDue().signum() == 0).count();
        BigDecimal average = issued.isEmpty() ? BigDecimal.ZERO : scale(recognised.divide(new BigDecimal(issued.size()), 4, RoundingMode.HALF_UP));

        return new RevenueSummaryDto(recognised, inclusive, collected, outstanding, sum(overdue, Invoice::getBalanceDue), overdue.size(),
                issued.size(), paidCount, average);
    }

    private ExpenseSummaryDto expensesOf(List<Expense> expenses) {
        BigDecimal gross = sum(expenses, Expense::getAmount);
        BigDecimal itc = sum(expenses, Expense::getGstClaimableAmount);
        BigDecimal deductible = sum(expenses.stream().filter(Expense::isTaxDeductible).toList(), Expense::getAmount);

        return new ExpenseSummaryDto(gross, itc, gross.subtract(itc), deductible, expenses.size());
    }

    private GstPositionDto gstOf(List<Invoice> issued, ExpenseSummaryDto expenses, boolean gstRegistered) {
        BigDecimal output = sum(issued, Invoice::getTotalTax);
        BigDecimal credit = expenses.reclaimableItc();
        return new GstPositionDto(output, credit, output.subtract(credit), gstRegistered);
    }

    private TdsPositionDto tdsOf(List<Invoice> issued, List<InvoicePayment> payments) {
        BigDecimal expected = sum(issued, Invoice::getTdsAmount);
        BigDecimal withheld = sum(payments, InvoicePayment::getTdsWithheld);
        return new TdsPositionDto(expected, withheld, expected.subtract(withheld).max(BigDecimal.ZERO));
    }

    private PipelineSummaryDto pipelineOf(List<BrandDeal> deals, List<Invoice> allInvoices) {
        List<BrandDeal> open = deals.stream().filter(d -> d.getStage() != DealStage.PAYMENT_RECEIVED).toList();

        Set<Long> invoicedDealIds = allInvoices.stream().filter(i -> i.getStatus().isIssued()).map(Invoice::getDeal).filter(Objects::nonNull)
                .map(BrandDeal::getId).collect(Collectors.toSet());
        List<BrandDeal> uninvoiced = open.stream().filter(d -> !invoicedDealIds.contains(d.getId())).toList();

        return new PipelineSummaryDto(open.size(), sum(open, BrandDeal::getAmount), uninvoiced.size(), sum(uninvoiced, BrandDeal::getAmount));
    }

    private List<MonthlyPointDto> monthlyOf(int startYear, List<Invoice> issued, List<InvoicePayment> payments, List<Expense> expenses) {
        Map<YearMonth, BigDecimal> revenue = groupBy(issued, i -> YearMonth.from(i.getIssueDate()), i -> i.getSubtotal().subtract(i.getDiscountAmount()));
        Map<YearMonth, BigDecimal> collected = groupBy(payments, p -> YearMonth.from(p.getReceivedOn()), InvoicePayment::getAmount);
        Map<YearMonth, BigDecimal> grossSpend = groupBy(expenses, e -> YearMonth.from(e.getExpenseDate()), Expense::getAmount);
        Map<YearMonth, BigDecimal> netSpend = groupBy(expenses, e -> YearMonth.from(e.getExpenseDate()),
                e -> e.getAmount().subtract(e.getGstClaimableAmount()));

        List<MonthlyPointDto> series = new ArrayList<>(FY_MONTHS);
        YearMonth cursor = YearMonth.of(startYear, APRIL);
        for (int i = 0; i < FY_MONTHS; i++) {
            BigDecimal monthRevenue = revenue.getOrDefault(cursor, BigDecimal.ZERO);
            BigDecimal monthCollected = collected.getOrDefault(cursor, BigDecimal.ZERO);
            BigDecimal monthGross = grossSpend.getOrDefault(cursor, BigDecimal.ZERO);
            BigDecimal monthNet = netSpend.getOrDefault(cursor, BigDecimal.ZERO);

            series.add(new MonthlyPointDto(cursor.toString(), cursor.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH), monthRevenue,
                    monthCollected, monthGross, monthRevenue.subtract(monthNet), monthCollected.subtract(monthGross)));
            cursor = cursor.plusMonths(1);
        }
        return series;
    }

    private List<CategoryTotalDto> categoriesOf(List<Expense> expenses, BigDecimal gross) {
        Map<ExpenseCategory, BigDecimal> byCategory = groupBy(expenses, Expense::getCategory, Expense::getAmount);

        return byCategory.entrySet().stream()
                .map(e -> new CategoryTotalDto(e.getKey(), e.getValue(),
                        gross.signum() == 0 ? BigDecimal.ZERO : scale(e.getValue().multiply(HUNDRED).divide(gross, 4, RoundingMode.HALF_UP))))
                .sorted(Comparator.comparing(CategoryTotalDto::amount).reversed()).toList();
    }

    private <T, K> Map<K, BigDecimal> groupBy(List<T> items, java.util.function.Function<T, K> key, java.util.function.Function<T, BigDecimal> value) {
        Map<K, BigDecimal> totals = new LinkedHashMap<>();
        for (T item : items) {
            totals.merge(key.apply(item), value.apply(item), BigDecimal::add);
        }
        return totals;
    }

    private <T> BigDecimal sum(List<T> items, java.util.function.Function<T, BigDecimal> value) {
        return scale(items.stream().map(value).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private boolean inYear(LocalDate date, int startYear) {
        return date != null && FinancialYear.startYearOf(date) == startYear;
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
