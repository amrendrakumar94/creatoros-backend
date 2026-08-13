package com.creatoros.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.creatoros.dto.quotation.QuotationDto;
import com.creatoros.dto.quotation.QuotationLineItemDto;
import com.creatoros.dto.quotation.QuotationPartyDto;
import com.creatoros.entity.Creator;
import com.creatoros.enums.CreatorStatus;
import com.creatoros.enums.QuotationStatus;
import com.creatoros.enums.Role;
import com.creatoros.enums.TdsSection;

class QuotationDocumentRendererTest {

    private final QuotationDocumentRenderer renderer = new QuotationDocumentRenderer();

    /**
     * A brand-new quotation's buyer/supplier snapshots routinely have null city/state/pincode/gstin
     * - most creators don't fill in every optional field. The equivalent invoice renderer broke on
     * exactly this shape before its address-line helper was made null-tolerant.
     */
    @Test
    @DisplayName("buildHtml handles a buyer/supplier with only name and email set")
    void buildHtmlHandlesSparsePartyData() {
        QuotationPartyDto sparseBuyer = new QuotationPartyDto("Acme Brand", null, null, null, "acme@example.com", null, null, null, null, null);
        QuotationPartyDto sparseSupplier = new QuotationPartyDto("Creator", "Creator", null, null, null, null, null, null, null, null);
        QuotationDto quotation = quotation(sparseSupplier, sparseBuyer);

        assertThatCode(() -> renderer.buildHtml(quotation, creator())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("buildHtml renders the quotation number, the real ₹ symbol, and the amount in words")
    void buildHtmlRendersCoreFields() {
        QuotationPartyDto buyer = new QuotationPartyDto("Acme Brand", "Acme Brand Pvt Ltd", "22AAAAA0000A1Z5", null, "acme@example.com",
                "123 MG Road", "Bengaluru", "Karnataka", "29", "560001");
        QuotationPartyDto supplier = new QuotationPartyDto("Creator", "Creator Studio", "29BBBBB1111B1Z5", "ABCDE1234F", null, "Address", "City",
                "State", "29", "560002");
        QuotationDto quotation = quotation(supplier, buyer);

        String html = renderer.buildHtml(quotation, creator());

        assertThat(html).contains("QUO/2026-27/0001").contains("₹10,000.00").contains("Ten Thousand Rupees Only").contains("Acme Brand")
                .contains("creator@example.com");
    }

    @Test
    @DisplayName("renderPdf turns the HTML into a non-trivial PDF")
    void renderPdfProducesPdfBytes() {
        QuotationDto quotation = quotation(null, null);
        String html = renderer.buildHtml(quotation, creator());

        byte[] pdf = renderer.renderPdf(html);

        assertThat(pdf.length).isGreaterThan(100);
        assertThat(new String(pdf, 0, 5)).isEqualTo("%PDF-");
    }

    private static Creator creator() {
        return Creator.builder().id(1L).email("creator@example.com").passwordHash("hash").status(CreatorStatus.ACTIVE).role(Role.CREATOR)
                .name("Creator").handle("@creator").tradeName("Creator Studio").build();
    }

    private static QuotationDto quotation(QuotationPartyDto supplier, QuotationPartyDto buyer) {
        QuotationLineItemDto lineItem = new QuotationLineItemDto("1", "Sponsored video", "998363", BigDecimal.ONE, null,
                new BigDecimal("10000.00"), BigDecimal.ZERO, new BigDecimal("10000.00"));

        return new QuotationDto("1", "QUO/2026-27/0001", "2026-27", null, null, QuotationStatus.SENT, LocalDate.now(),
                LocalDate.now().plusDays(30), false, supplier, buyer, null, null, false, false, false, new BigDecimal("10000.00"), BigDecimal.ZERO,
                new BigDecimal("10000.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, new BigDecimal("10000.00"), TdsSection.NONE, BigDecimal.ZERO, BigDecimal.ZERO, null, null, List.of(lineItem), null,
                null, null, null, null);
    }
}
