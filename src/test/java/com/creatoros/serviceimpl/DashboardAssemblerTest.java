package com.creatoros.serviceimpl;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.creatoros.dto.dashboard.DashboardDto;
import com.creatoros.dto.dashboard.MonthlyPointDto;
import com.creatoros.entity.BrandDeal;
import com.creatoros.entity.Expense;
import com.creatoros.entity.Invoice;
import com.creatoros.entity.InvoicePayment;
import com.creatoros.enums.DealStage;
import com.creatoros.enums.ExpenseCategory;
import com.creatoros.enums.InvoiceStatus;
import com.creatoros.enums.PlatformType;
import com.creatoros.enums.TdsSection;

class DashboardAssemblerTest {

    private static final int       FY_2026 = 2026;
    private static final LocalDate TODAY   = LocalDate.parse("2026-08-08");

    private final DashboardAssembler assembler = new DashboardAssembler();

    private static Invoice invoice(InvoiceStatus status, String issueDate, String taxable, String tax, String tds, String balance) {
        BigDecimal taxableValue = new BigDecimal(taxable);
        return Invoice.builder().id(1L).status(status).issueDate(LocalDate.parse(issueDate)).dueDate(LocalDate.parse(issueDate).plusDays(30))
                .subtotal(taxableValue).discountAmount(BigDecimal.ZERO).totalTax(new BigDecimal(tax))
                .invoiceTotal(taxableValue.add(new BigDecimal(tax))).tdsSection(TdsSection.SECTION_194J).tdsAmount(new BigDecimal(tds))
                .amountPaid(BigDecimal.ZERO).balanceDue(new BigDecimal(balance)).build();
    }

    private static Expense expense(String date, String amount, String itc, ExpenseCategory category) {
        return Expense.builder().amount(new BigDecimal(amount)).gstClaimableAmount(new BigDecimal(itc)).expenseDate(LocalDate.parse(date))
                .category(category).taxDeductible(true).build();
    }

    private static InvoicePayment payment(String receivedOn, String amount, String tdsWithheld) {
        return InvoicePayment.builder().amount(new BigDecimal(amount)).tdsWithheld(new BigDecimal(tdsWithheld))
                .receivedOn(LocalDate.parse(receivedOn)).build();
    }

    private DashboardDto assemble(List<Invoice> invoices, List<InvoicePayment> payments, List<Expense> expenses, List<BrandDeal> deals) {
        return assembler.assemble(FY_2026, true, invoices, payments, expenses, deals, TODAY);
    }

    @Test
    @DisplayName("recognises revenue net of GST, because collected tax is never the creator's income")
    void revenueExcludesGst() {
        DashboardDto d = assemble(List.of(invoice(InvoiceStatus.SENT, "2026-08-01", "100000", "18000", "10000", "118000")), List.of(), List.of(),
                List.of());

        assertThat(d.revenue().recognised()).isEqualByComparingTo("100000.00");
        assertThat(d.revenue().invoicedInclusiveOfTax()).isEqualByComparingTo("118000.00");
    }

    @Test
    @DisplayName("ignores drafts and cancelled invoices, which are not income")
    void excludesDraftAndCancelled() {
        DashboardDto d = assemble(List.of(invoice(InvoiceStatus.DRAFT, "2026-08-01", "50000", "9000", "5000", "59000"),
                invoice(InvoiceStatus.CANCELLED, "2026-08-02", "70000", "12600", "7000", "82600"),
                invoice(InvoiceStatus.PAID, "2026-08-03", "40000", "7200", "4000", "0")), List.of(), List.of(), List.of());

        assertThat(d.revenue().recognised()).isEqualByComparingTo("40000.00");
        assertThat(d.revenue().invoiceCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("nets GST payable down by input tax credit already claimed on expenses")
    void gstNetsOutputAgainstInputCredit() {
        DashboardDto d = assemble(List.of(invoice(InvoiceStatus.SENT, "2026-08-01", "100000", "18000", "10000", "118000")), List.of(),
                List.of(expense("2026-08-04", "59000", "9000", ExpenseCategory.EQUIPMENT)), List.of());

        assertThat(d.gst().outputTax()).isEqualByComparingTo("18000.00");
        assertThat(d.gst().inputTaxCredit()).isEqualByComparingTo("9000.00");
        assertThat(d.gst().netPayable()).isEqualByComparingTo("9000.00");
    }

    @Test
    @DisplayName("treats reclaimable input credit as recoverable, not as a cost of doing business")
    void profitNetsItcOutOfExpenses() {
        DashboardDto d = assemble(List.of(invoice(InvoiceStatus.SENT, "2026-08-01", "100000", "18000", "10000", "118000")), List.of(),
                List.of(expense("2026-08-04", "59000", "9000", ExpenseCategory.EQUIPMENT)), List.of());

        assertThat(d.expenses().grossPaid()).isEqualByComparingTo("59000.00");
        assertThat(d.expenses().netOfItc()).isEqualByComparingTo("50000.00");
        assertThat(d.netProfit()).isEqualByComparingTo("50000.00");
        assertThat(d.profitMargin()).isEqualByComparingTo("50.00");
    }

    @Test
    @DisplayName("reports cash flow from money actually banked, not from what was invoiced")
    void cashFlowUsesBankedCashAndGrossSpend() {
        DashboardDto d = assemble(List.of(invoice(InvoiceStatus.PARTIALLY_PAID, "2026-08-01", "100000", "18000", "10000", "68000")),
                List.of(payment("2026-08-15", "50000", "0")), List.of(expense("2026-08-04", "59000", "9000", ExpenseCategory.EQUIPMENT)),
                List.of());

        assertThat(d.revenue().collectedCash()).isEqualByComparingTo("50000.00");
        assertThat(d.netCashFlow()).isEqualByComparingTo("-9000.00");
        assertThat(d.netProfit()).isEqualByComparingTo("50000.00");
    }

    @Test
    @DisplayName("separates TDS the invoices expect from TDS a brand has actually withheld")
    void tracksTdsExpectedVersusWithheld() {
        DashboardDto d = assemble(List.of(invoice(InvoiceStatus.PARTIALLY_PAID, "2026-08-01", "100000", "18000", "10000", "68000")),
                List.of(payment("2026-08-15", "50000", "4000")), List.of(), List.of());

        assertThat(d.tds().deductedOnInvoices()).isEqualByComparingTo("10000.00");
        assertThat(d.tds().withheldOnPayments()).isEqualByComparingTo("4000.00");
        assertThat(d.tds().awaitingWithholding()).isEqualByComparingTo("6000.00");
    }

    @Test
    @DisplayName("scopes each figure to the Indian financial year by its own date basis")
    void scopesEachFigureToTheFinancialYear() {
        DashboardDto d = assemble(
                List.of(invoice(InvoiceStatus.SENT, "2026-03-31", "999999", "0", "0", "0"),
                        invoice(InvoiceStatus.SENT, "2026-04-01", "100000", "18000", "10000", "118000"),
                        invoice(InvoiceStatus.SENT, "2027-04-01", "888888", "0", "0", "0")),
                List.of(payment("2026-03-31", "777777", "0"), payment("2026-06-10", "25000", "0")),
                List.of(expense("2026-03-31", "666666", "0", ExpenseCategory.EQUIPMENT),
                        expense("2026-09-09", "10000", "0", ExpenseCategory.SOFTWARE_AND_SUBSCRIPTIONS)),
                List.of());

        assertThat(d.financialYear()).isEqualTo("2026-27");
        assertThat(d.revenue().recognised()).isEqualByComparingTo("100000.00");
        assertThat(d.revenue().collectedCash()).isEqualByComparingTo("25000.00");
        assertThat(d.expenses().grossPaid()).isEqualByComparingTo("10000.00");
    }

    @Test
    @DisplayName("lays the monthly series out April to March so it reads as an Indian financial year")
    void monthlySeriesRunsAprilToMarch() {
        DashboardDto d = assemble(List.of(), List.of(), List.of(), List.of());

        assertThat(d.monthly()).hasSize(12);
        assertThat(d.monthly().stream().map(MonthlyPointDto::label)).containsExactly("Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
                "Jan", "Feb", "Mar");
        assertThat(d.monthly().get(0).month()).isEqualTo("2026-04");
        assertThat(d.monthly().get(11).month()).isEqualTo("2027-03");
    }

    @Test
    @DisplayName("places each month's figures against the right month")
    void monthlySeriesBucketsByMonth() {
        DashboardDto d = assemble(List.of(invoice(InvoiceStatus.SENT, "2026-08-01", "100000", "18000", "10000", "118000")),
                List.of(payment("2026-09-15", "40000", "0")), List.of(expense("2026-08-04", "11800", "1800", ExpenseCategory.EQUIPMENT)),
                List.of());

        MonthlyPointDto august = d.monthly().stream().filter(m -> m.month().equals("2026-08")).findFirst().orElseThrow();
        MonthlyPointDto september = d.monthly().stream().filter(m -> m.month().equals("2026-09")).findFirst().orElseThrow();

        assertThat(august.revenue()).isEqualByComparingTo("100000.00");
        assertThat(august.expenses()).isEqualByComparingTo("11800.00");
        assertThat(august.profit()).isEqualByComparingTo("90000.00");
        assertThat(august.cashFlow()).isEqualByComparingTo("-11800.00");
        assertThat(september.collected()).isEqualByComparingTo("40000.00");
        assertThat(september.revenue()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("counts only issued invoices past their due date as overdue")
    void overdueCountsIssuedInvoicesPastDue() {
        DashboardDto d = assemble(List.of(invoice(InvoiceStatus.SENT, "2026-04-01", "50000", "9000", "5000", "59000"),
                invoice(InvoiceStatus.DRAFT, "2026-04-01", "70000", "12600", "7000", "82600"),
                invoice(InvoiceStatus.SENT, "2026-08-05", "30000", "5400", "3000", "35400")), List.of(), List.of(), List.of());

        assertThat(d.revenue().overdueCount()).isEqualTo(1);
        assertThat(d.revenue().overdue()).isEqualByComparingTo("59000.00");
    }

    @Test
    @DisplayName("flags open deals that have not been invoiced yet, which is revenue left on the table")
    void surfacesUninvoicedPipeline() {
        BrandDeal invoiced = BrandDeal.builder().id(1L).amount(new BigDecimal("100000")).stage(DealStage.CONTENT_APPROVED)
                .platform(PlatformType.YOUTUBE).build();
        BrandDeal notInvoiced = BrandDeal.builder().id(2L).amount(new BigDecimal("60000")).stage(DealStage.CONTRACT_REVIEW)
                .platform(PlatformType.INSTAGRAM).build();
        BrandDeal closed = BrandDeal.builder().id(3L).amount(new BigDecimal("40000")).stage(DealStage.PAYMENT_RECEIVED)
                .platform(PlatformType.YOUTUBE).build();

        Invoice against = invoice(InvoiceStatus.SENT, "2026-08-01", "100000", "18000", "10000", "118000");
        against.setDeal(invoiced);

        DashboardDto d = assemble(List.of(against), List.of(), List.of(), List.of(invoiced, notInvoiced, closed));

        assertThat(d.pipeline().openDealCount()).isEqualTo(2);
        assertThat(d.pipeline().openDealValue()).isEqualByComparingTo("160000.00");
        assertThat(d.pipeline().uninvoicedDealCount()).isEqualTo(1);
        assertThat(d.pipeline().uninvoicedDealValue()).isEqualByComparingTo("60000.00");
    }

    @Test
    @DisplayName("ranks expense categories by spend and shares to a hundred percent")
    void ranksExpenseCategoriesByShare() {
        DashboardDto d = assemble(List.of(), List.of(),
                List.of(expense("2026-08-01", "60000", "0", ExpenseCategory.EQUIPMENT),
                        expense("2026-08-02", "30000", "0", ExpenseCategory.TRAVEL_AND_SHOOTS),
                        expense("2026-08-03", "10000", "0", ExpenseCategory.SOFTWARE_AND_SUBSCRIPTIONS)),
                List.of());

        assertThat(d.expenseByCategory()).hasSize(3);
        assertThat(d.expenseByCategory().get(0).category()).isEqualTo(ExpenseCategory.EQUIPMENT);
        assertThat(d.expenseByCategory().get(0).share()).isEqualByComparingTo("60.00");
        assertThat(d.expenseByCategory().stream().map(c -> c.share()).reduce(BigDecimal.ZERO, BigDecimal::add)).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("returns zeroed figures rather than failing for a creator with no records")
    void handlesAnEmptyYear() {
        DashboardDto d = assemble(List.of(), List.of(), List.of(), List.of());

        assertThat(d.revenue().recognised()).isEqualByComparingTo("0.00");
        assertThat(d.revenue().averageInvoiceValue()).isEqualByComparingTo("0");
        assertThat(d.netProfit()).isEqualByComparingTo("0.00");
        assertThat(d.profitMargin()).isEqualByComparingTo("0");
        assertThat(d.expenseByCategory()).isEmpty();
        assertThat(d.monthly()).hasSize(12);
    }
}
