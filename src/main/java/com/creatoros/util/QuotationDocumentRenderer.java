package com.creatoros.util;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.creatoros.dto.quotation.QuotationDto;
import com.creatoros.dto.quotation.QuotationLineItemDto;
import com.creatoros.dto.quotation.QuotationPartyDto;
import com.creatoros.entity.Creator;

/**
 * Builds the quotation as one XHTML document, reused for the on-screen preview, the emailed HTML
 * body, and (via {@link #renderPdf}) the PDF attachment - one template instead of three, so none
 * of them can drift out of sync with the others.
 */
@Component
public class QuotationDocumentRenderer {

    private final String fontPath = DocumentFormatting.extractFont("fonts/NotoSans-Regular.ttf");

    public String buildHtml(QuotationDto quotation, Creator creator) {
        QuotationPartyDto supplier = quotation.supplier();
        QuotationPartyDto buyer = quotation.buyer();
        boolean isTaxQuotation = quotation.taxQuotation();
        String supplierName = supplier != null && supplier.legalName() != null ? supplier.legalName()
                : creator == null ? null : DocumentFormatting.firstNonBlank(creator.getTradeName(), creator.getName());

        StringBuilder html = new StringBuilder();
        html.append("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><style>").append(css()).append("</style></head><body>");
        html.append("<div class=\"page\">");

        appendHeader(html, quotation, supplier, supplierName, creator, isTaxQuotation);
        html.append("<div class=\"divider\"></div>");
        appendQuotedToAndSupplyInfo(html, quotation, buyer);
        appendLineItems(html, quotation);
        appendAmountSection(html, quotation, isTaxQuotation);

        if (quotation.tdsAmount().signum() > 0) {
            html.append("<p class=\"muted small\" style=\"margin-top:8px;\">Estimated TDS deduction on conversion: ")
                    .append(DocumentFormatting.formatInr(quotation.tdsAmount())).append(" (")
                    .append(DocumentFormatting.escape(quotation.tdsSection().getLabel()))
                    .append("). This is an estimate only - the invoice will state the exact TDS withheld.</p>");
        }

        appendNotesAndTerms(html, quotation);
        appendFooter(html, quotation, supplierName);

        html.append("</div></body></html>");
        return html.toString();
    }

    /** Fallback body for mail clients that don't render HTML - kept brief, points at the PDF. */
    public String buildPlainText(QuotationDto quotation) {
        String buyerName = quotation.buyer() == null ? "" : DocumentFormatting.orDash(quotation.buyer().name());
        String validUntil = quotation.validUntil() == null ? "No expiry" : DocumentFormatting.formatDate(quotation.validUntil());
        return "Quotation " + quotation.quotationNumber() + " for " + buyerName + "\n" + "Total: "
                + DocumentFormatting.formatInr(quotation.quotationTotal()) + "\n" + "Valid until: " + validUntil + "\n\n"
                + "The full quotation is attached as a PDF.";
    }

    public byte[] renderPdf(String xhtml) {
        try {
            ITextRenderer renderer = new ITextRenderer();
            renderer.getFontResolver().addFont(fontPath, "Identity-H", true);
            renderer.setDocumentFromString(xhtml);
            renderer.layout();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            renderer.createPDF(out);
            return out.toByteArray();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to render quotation PDF", exception);
        }
    }

    // ---------- Sections ----------

    private void appendHeader(StringBuilder html, QuotationDto quotation, QuotationPartyDto supplier, String supplierName, Creator creator,
            boolean isTaxQuotation) {
        html.append("<table class=\"bare\"><tr><td class=\"bare\">");
        html.append("<div class=\"h1\">").append(DocumentFormatting.escape(DocumentFormatting.orDash(supplierName))).append("</div>");
        appendAddressLines(html, supplier);
        html.append("<div style=\"margin-top:5px;\">");
        if (supplier != null && supplier.gstin() != null) {
            html.append("<div><strong>GSTIN:</strong> ").append(DocumentFormatting.escape(supplier.gstin())).append("</div>");
        }
        if (supplier != null && supplier.pan() != null) {
            html.append("<div><strong>PAN:</strong> ").append(DocumentFormatting.escape(supplier.pan())).append("</div>");
        }
        if (creator != null && creator.getEmail() != null) {
            html.append("<div class=\"muted\">").append(DocumentFormatting.escape(creator.getEmail())).append("</div>");
        }
        html.append("</div>");
        html.append("</td><td class=\"bare right\" style=\"vertical-align:top;\">");
        html.append("<div class=\"h2\">QUOTATION</div>");
        if (!isTaxQuotation) {
            html.append("<div class=\"muted small\">Estimate — GST Not Applicable</div>");
        }
        html.append("<table class=\"meta\">");
        appendMetaRow(html, "Quotation No.", quotation.quotationNumber(), true);
        appendMetaRow(html, "Quotation Date", DocumentFormatting.formatDate(quotation.issueDate()), false);
        appendMetaRow(html, "Valid Until", quotation.validUntil() == null ? "No expiry" : DocumentFormatting.formatDate(quotation.validUntil()),
                false);
        if (quotation.dealNumber() != null) {
            appendMetaRow(html, "Deal Ref.", quotation.dealNumber(), false);
        }
        html.append("</table>");
        html.append("</td></tr></table>");
    }

    private void appendMetaRow(StringBuilder html, String label, String value, boolean strong) {
        html.append("<tr><td class=\"meta-label\">").append(DocumentFormatting.escape(label)).append("</td><td class=\"mono")
                .append(strong ? " strong" : "").append("\">").append(DocumentFormatting.escape(value)).append("</td></tr>");
    }

    private void appendQuotedToAndSupplyInfo(StringBuilder html, QuotationDto quotation, QuotationPartyDto buyer) {
        html.append("<table class=\"bare\"><tr><td class=\"bare\" style=\"width:50%;padding-right:8px;\">");
        html.append("<div class=\"box\"><div class=\"box-title\">Quoted To</div>");
        html.append("<div class=\"box-name\">")
                .append(DocumentFormatting.escape(
                        DocumentFormatting.orDash(buyer == null ? null : DocumentFormatting.firstNonBlank(buyer.legalName(), buyer.name()))))
                .append("</div>");
        if (buyer != null && buyer.legalName() != null && buyer.name() != null && !buyer.legalName().equals(buyer.name())) {
            html.append("<div class=\"muted\">(").append(DocumentFormatting.escape(buyer.name())).append(")</div>");
        }
        appendAddressLines(html, buyer);
        if (buyer != null && buyer.gstin() != null) {
            html.append("<div style=\"margin-top:3px;\"><strong>GSTIN:</strong> ").append(DocumentFormatting.escape(buyer.gstin())).append("</div>");
        }
        if (buyer != null && buyer.email() != null) {
            html.append("<div class=\"muted\">").append(DocumentFormatting.escape(buyer.email())).append("</div>");
        }
        html.append("</div></td><td class=\"bare\" style=\"width:50%;padding-left:8px;vertical-align:top;\">");
        html.append("<div class=\"box\">");
        appendInfoField(html, "Place of Supply", quotation.placeOfSupplyCode() != null
                ? quotation.placeOfSupplyCode() + " — " + DocumentFormatting.orDash(quotation.placeOfSupplyState()) : "Not specified");
        appendInfoField(html, "Supply Type", quotation.interState() ? "Inter-State (IGST)" : "Intra-State (CGST + SGST)");
        appendInfoField(html, "Reverse Charge", quotation.reverseCharge() ? "Yes" : "No");
        appendInfoField(html, "Financial Year", quotation.financialYear());
        html.append("</div></td></tr></table>");
    }

    private void appendInfoField(StringBuilder html, String label, String value) {
        html.append("<div class=\"field\"><span class=\"field-label\">").append(DocumentFormatting.escape(label))
                .append("</span><span class=\"field-value\">").append(DocumentFormatting.escape(value)).append("</span></div>");
    }

    private void appendLineItems(StringBuilder html, QuotationDto quotation) {
        html.append("<table class=\"items\"><thead><tr>");
        html.append("<th style=\"width:5%;\">#</th><th>Description of Service</th><th style=\"width:12%;\">SAC</th>");
        html.append("<th class=\"right\" style=\"width:9%;\">Qty</th><th class=\"right\" style=\"width:16%;\">Rate</th>");
        html.append("<th class=\"right\" style=\"width:18%;\">Taxable Value</th>");
        html.append("</tr></thead><tbody>");
        int i = 1;
        for (QuotationLineItemDto item : quotation.lineItems()) {
            html.append("<tr><td>").append(i++).append("</td><td>").append(DocumentFormatting.escape(item.description()));
            if (item.unit() != null) {
                html.append(" <span class=\"muted\">(").append(DocumentFormatting.escape(item.unit())).append(")</span>");
            }
            html.append("</td><td class=\"mono\">").append(DocumentFormatting.escape(DocumentFormatting.orDash(item.sacCode())))
                    .append("</td><td class=\"right mono\">").append(item.quantity()).append("</td><td class=\"right mono\">")
                    .append(DocumentFormatting.formatInr(item.rate())).append("</td><td class=\"right mono\">")
                    .append(DocumentFormatting.formatInr(item.taxableAmount())).append("</td></tr>");
        }
        html.append("</tbody></table>");
    }

    private void appendAmountSection(StringBuilder html, QuotationDto quotation, boolean isTaxQuotation) {
        html.append("<table class=\"bare\" style=\"margin-top:12px;\"><tr><td class=\"bare\" style=\"vertical-align:top;\">");
        html.append("<div class=\"box\"><div class=\"box-title\">Quotation Amount (in words)</div>");
        html.append("<div class=\"strong\">").append(DocumentFormatting.escape(AmountInWords.toWords(quotation.quotationTotal())))
                .append("</div></div>");

        html.append("</td><td class=\"bare\" style=\"width:82mm;vertical-align:top;\">");
        html.append("<table class=\"totals\">");
        if (quotation.discountAmount().signum() > 0) {
            appendTotalRow(html, "Subtotal", DocumentFormatting.formatInr(quotation.subtotal()), false);
            appendTotalRow(html, "Less Discount", "− " + DocumentFormatting.formatInr(quotation.discountAmount()), false);
            appendTotalRow(html, "Taxable Value", DocumentFormatting.formatInr(quotation.taxableValue()), true);
        } else {
            appendTotalRow(html, "Taxable Value", DocumentFormatting.formatInr(quotation.taxableValue()), false);
        }
        if (isTaxQuotation && !quotation.interState()) {
            appendTotalRow(html, "CGST @ " + quotation.cgstRate() + "%", DocumentFormatting.formatInr(quotation.cgstAmount()), false);
            appendTotalRow(html, "SGST @ " + quotation.sgstRate() + "%", DocumentFormatting.formatInr(quotation.sgstAmount()), false);
        }
        if (isTaxQuotation && quotation.interState()) {
            appendTotalRow(html, "IGST @ " + quotation.igstRate() + "%", DocumentFormatting.formatInr(quotation.igstAmount()), false);
        }
        if (!isTaxQuotation) {
            appendTotalRow(html, "GST", "Not applicable", false);
        }
        appendTotalRow(html, "Quotation Total", DocumentFormatting.formatInr(quotation.quotationTotal()), true);
        html.append("</table>");
        html.append("</td></tr></table>");
    }

    private void appendNotesAndTerms(StringBuilder html, QuotationDto quotation) {
        if (quotation.notes() == null && quotation.terms() == null) {
            return;
        }
        html.append("<div class=\"section-title\">Notes &amp; Terms</div>");
        if (quotation.notes() != null) {
            html.append("<div>").append(DocumentFormatting.escape(quotation.notes()).replace("\n", "<br/>")).append("</div>");
        }
        if (quotation.terms() != null) {
            html.append("<div class=\"muted\">").append(DocumentFormatting.escape(quotation.terms()).replace("\n", "<br/>")).append("</div>");
        }
    }

    private void appendFooter(StringBuilder html, QuotationDto quotation, String supplierName) {
        String validity = quotation.validUntil() == null ? "This quotation does not expire."
                : "This quotation is valid until " + DocumentFormatting.formatDate(quotation.validUntil()) + ".";

        html.append("<table class=\"bare\" style=\"margin-top:22px;\"><tr><td class=\"bare\" style=\"vertical-align:bottom;width:60%;\">");
        html.append("<div class=\"muted small\">");
        if (quotation.reverseCharge()) {
            html.append("Tax is payable on reverse charge basis by the recipient.<br/>");
        }
        html.append(DocumentFormatting.escape(validity)).append("<br/>This is a computer-generated quotation.</div>");
        html.append("</td><td class=\"bare right\" style=\"vertical-align:bottom;\">");
        html.append("<div style=\"height:16mm;\"></div>");
        html.append("<div class=\"signatory\">For ").append(DocumentFormatting.escape(DocumentFormatting.orDash(supplierName))).append("</div>");
        html.append("<div class=\"muted small\">Authorised Signatory</div>");
        html.append("</td></tr></table>");
    }

    private void appendTotalRow(StringBuilder html, String label, String value, boolean strong) {
        html.append("<tr").append(strong ? " class=\"strong\"" : "").append("><td class=\"totals-label\">").append(DocumentFormatting.escape(label))
                .append("</td><td class=\"right mono\">").append(value).append("</td></tr>");
    }

    private void appendAddressLines(StringBuilder html, QuotationPartyDto party) {
        if (party == null) {
            return;
        }
        List<String> lines = new ArrayList<>();
        if (party.address() != null) {
            lines.add(party.address());
        }
        String cityState = String.join(", ", Stream.of(party.city(), party.pincode()).filter(v -> v != null && !v.isBlank()).toList());
        if (!cityState.isBlank()) {
            lines.add(cityState);
        }
        if (party.state() != null) {
            lines.add(party.state());
        }
        if (!lines.isEmpty()) {
            html.append("<div class=\"muted\">").append(String.join("<br/>", lines.stream().map(DocumentFormatting::escape).toList()))
                    .append("</div>");
        }
    }

    // ---------- Style ----------

    private String css() {
        return DocumentFormatting.baseCss();
    }
}
