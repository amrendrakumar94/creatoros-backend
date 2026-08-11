package com.creatoros.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.creatoros.dto.invoice.InvoiceDto;
import com.creatoros.dto.invoice.InvoiceLineItemDto;
import com.creatoros.dto.invoice.InvoicePartyDto;
import com.creatoros.entity.BankDetails;
import com.creatoros.entity.Creator;
import com.creatoros.enums.CreatorStatus;
import com.creatoros.enums.InvoiceStatus;
import com.creatoros.enums.PaymentTerms;
import com.creatoros.enums.Role;
import com.creatoros.enums.TdsSection;

class InvoiceDocumentRendererTest {

    private final InvoiceDocumentRenderer renderer = new InvoiceDocumentRenderer();

    /**
     * A brand-new invoice's buyer/supplier snapshots routinely have null city/state/pincode/gstin
     * - most creators don't fill in every optional field. A prior version of the renderer fed
     * these straight into `List.of(...)`, which throws on any null element; this is the exact
     * shape that broke it.
     */
    @Test
    @DisplayName("buildHtml handles a buyer/supplier with only name and email set")
    void buildHtmlHandlesSparsePartyData() {
        InvoicePartyDto sparseBuyer = new InvoicePartyDto("Acme Brand", null, null, null, "acme@example.com", null, null, null, null, null);
        InvoicePartyDto sparseSupplier = new InvoicePartyDto("Creator", "Creator", null, null, null, null, null, null, null, null);
        InvoiceDto invoice = invoice(sparseSupplier, sparseBuyer);

        assertThatCode(() -> renderer.buildHtml(invoice, creator())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("buildHtml renders the invoice number, the real ₹ symbol, and the amount in words")
    void buildHtmlRendersCoreFields() {
        InvoicePartyDto buyer = new InvoicePartyDto("Acme Brand", "Acme Brand Pvt Ltd", "22AAAAA0000A1Z5", null, "acme@example.com",
                "123 MG Road", "Bengaluru", "Karnataka", "29", "560001");
        InvoicePartyDto supplier = new InvoicePartyDto("Creator", "Creator Studio", "29BBBBB1111B1Z5", "ABCDE1234F", null, "Address", "City",
                "State", "29", "560002");
        InvoiceDto invoice = invoice(supplier, buyer);

        String html = renderer.buildHtml(invoice, creator());

        assertThat(html).contains("INV-2026-0001").contains("₹10,000.00").contains("Ten Thousand Rupees Only").contains("Acme Brand")
                .contains("HDFC0001234").contains("creator@example.com");
    }

    @Test
    @DisplayName("renderPdf turns the HTML into a non-trivial PDF")
    void renderPdfProducesPdfBytes() {
        InvoiceDto invoice = invoice(null, null);
        String html = renderer.buildHtml(invoice, creator());

        byte[] pdf = renderer.renderPdf(html);

        assertThat(pdf.length).isGreaterThan(100);
        assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");
    }

    private static Creator creator() {
        return Creator.builder().id(1L).email("creator@example.com").passwordHash("hash").status(CreatorStatus.ACTIVE).role(Role.CREATOR)
                .name("Creator").handle("@creator").tradeName("Creator Studio")
                .bankDetails(BankDetails.builder().bankName("HDFC Bank").accountNumber("1234567890").ifscCode("HDFC0001234")
                        .upiId("creator@upi").build())
                .build();
    }

    private static InvoiceDto invoice(InvoicePartyDto supplier, InvoicePartyDto buyer) {
        InvoiceLineItemDto lineItem = new InvoiceLineItemDto("1", "Sponsored video", "998363", BigDecimal.ONE, null,
                new BigDecimal("10000.00"), BigDecimal.ZERO, new BigDecimal("10000.00"));

        return new InvoiceDto("1", "INV-2026-0001", "2026-27", null, null, InvoiceStatus.SENT, false, 0, LocalDate.now(),
                LocalDate.now().plusDays(30), PaymentTerms.NET_30, supplier, buyer, null, null, false, false, false, new BigDecimal("10000.00"),
                BigDecimal.ZERO, new BigDecimal("10000.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("10000.00"), TdsSection.NONE, BigDecimal.ZERO, BigDecimal.ZERO,
                new BigDecimal("10000.00"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("10000.00"), null, null, List.of(lineItem),
                List.of(), null, null, null);
    }
}
