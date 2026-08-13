package com.creatoros.util;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.creatoros.dto.invoice.InvoiceDto;
import com.creatoros.dto.invoice.InvoiceLineItemDto;
import com.creatoros.dto.invoice.InvoicePartyDto;
import com.creatoros.dto.invoice.InvoicePaymentDto;
import com.creatoros.entity.BankDetails;
import com.creatoros.entity.Creator;

/**
 * Builds the invoice as one XHTML document, reused for the on-screen preview, the emailed HTML
 * body, and (via {@link #renderPdf}) the PDF attachment - one template instead of three, so none
 * of them can drift out of sync with the others.
 */
@Component
public class InvoiceDocumentRenderer {

    /**
     * Base14 PDF fonts have no glyph for ₹ (U+20B9) - it postdates them - so Flying Saucer needs
     * an embedded Unicode font. Extracted once to a real file because the legacy path-based font
     * API can't read straight out of a packaged jar's classpath.
     */
    private final String fontPath = DocumentFormatting.extractFont("fonts/NotoSans-Regular.ttf");

    public String buildHtml(InvoiceDto invoice, Creator creator) {
        InvoicePartyDto supplier = invoice.supplier();
        InvoicePartyDto buyer = invoice.buyer();
        boolean isTaxInvoice = invoice.taxInvoice();
        BankDetails bank = creator == null ? null : creator.getBankDetails();
        String supplierName = supplier != null && supplier.legalName() != null ? supplier.legalName()
                : creator == null ? null : DocumentFormatting.firstNonBlank(creator.getTradeName(), creator.getName());

        StringBuilder html = new StringBuilder();
        html.append("<html xmlns=\"http://www.w3.org/1999/xhtml\"><head><style>").append(css()).append("</style></head><body>");
        html.append("<div class=\"page\">");

        appendHeader(html, invoice, supplier, supplierName, creator, isTaxInvoice);
        html.append("<div class=\"divider\"></div>");
        appendBilledToAndSupplyInfo(html, invoice, buyer);
        appendLineItems(html, invoice);
        appendAmountSection(html, invoice, bank, isTaxInvoice);

        if (invoice.tdsAmount().signum() > 0) {
            html.append("<p class=\"muted small\" style=\"margin-top:8px;\">TDS under ")
                    .append(DocumentFormatting.escape(invoice.tdsSection().getLabel()))
                    .append(" is computed on the taxable value excluding GST, per CBDT Circular 23/2017.</p>");
        }

        appendPaymentsReceived(html, invoice);
        appendNotesAndTerms(html, invoice);
        appendFooter(html, invoice, supplierName);

        html.append("</div></body></html>");
        return html.toString();
    }

    /** Fallback body for mail clients that don't render HTML - kept brief, points at the PDF. */
    public String buildPlainText(InvoiceDto invoice) {
        String buyerName = invoice.buyer() == null ? "" : DocumentFormatting.orDash(invoice.buyer().name());
        return "Invoice " + invoice.invoiceNumber() + " for " + buyerName + "\n" + "Amount due: " + DocumentFormatting.formatInr(invoice.balanceDue())
                + "\n" + "Due date: " + DocumentFormatting.formatDate(invoice.dueDate()) + "\n\n" + "The full invoice is attached as a PDF.";
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
            throw new IllegalStateException("Unable to render invoice PDF", exception);
        }
    }

    // ---------- Sections ----------

    private void appendHeader(StringBuilder html, InvoiceDto invoice, InvoicePartyDto supplier, String supplierName, Creator creator,
            boolean isTaxInvoice) {
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
        html.append("<div class=\"h2\">").append(isTaxInvoice ? "TAX INVOICE" : "BILL OF SUPPLY").append("</div>");
        if (!isTaxInvoice) {
            html.append("<div class=\"muted small\">Not liable to collect GST</div>");
        }
        html.append("<table class=\"meta\">");
        appendMetaRow(html, "Invoice No.", invoice.invoiceNumber(), true);
        appendMetaRow(html, "Invoice Date", DocumentFormatting.formatDate(invoice.issueDate()), false);
        appendMetaRow(html, "Due Date", DocumentFormatting.formatDate(invoice.dueDate()), false);
        appendMetaRow(html, "Terms", invoice.paymentTerms().getLabel(), false);
        if (invoice.dealNumber() != null) {
            appendMetaRow(html, "Deal Ref.", invoice.dealNumber(), false);
        }
        html.append("</table>");
        html.append("</td></tr></table>");
    }

    private void appendMetaRow(StringBuilder html, String label, String value, boolean strong) {
        html.append("<tr><td class=\"meta-label\">").append(DocumentFormatting.escape(label)).append("</td><td class=\"mono")
                .append(strong ? " strong" : "").append("\">").append(DocumentFormatting.escape(value)).append("</td></tr>");
    }

    private void appendBilledToAndSupplyInfo(StringBuilder html, InvoiceDto invoice, InvoicePartyDto buyer) {
        html.append("<table class=\"bare\"><tr><td class=\"bare\" style=\"width:50%;padding-right:8px;\">");
        html.append("<div class=\"box\"><div class=\"box-title\">Billed To</div>");
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
        appendInfoField(html, "Place of Supply", invoice.placeOfSupplyCode() != null
                ? invoice.placeOfSupplyCode() + " — " + DocumentFormatting.orDash(invoice.placeOfSupplyState()) : "Not specified");
        appendInfoField(html, "Supply Type", invoice.interState() ? "Inter-State (IGST)" : "Intra-State (CGST + SGST)");
        appendInfoField(html, "Reverse Charge", invoice.reverseCharge() ? "Yes" : "No");
        appendInfoField(html, "Financial Year", invoice.financialYear());
        html.append("</div></td></tr></table>");
    }

    private void appendInfoField(StringBuilder html, String label, String value) {
        html.append("<div class=\"field\"><span class=\"field-label\">").append(DocumentFormatting.escape(label))
                .append("</span><span class=\"field-value\">").append(DocumentFormatting.escape(value)).append("</span></div>");
    }

    private void appendLineItems(StringBuilder html, InvoiceDto invoice) {
        html.append("<table class=\"items\"><thead><tr>");
        html.append("<th style=\"width:5%;\">#</th><th>Description of Service</th><th style=\"width:12%;\">SAC</th>");
        html.append("<th class=\"right\" style=\"width:9%;\">Qty</th><th class=\"right\" style=\"width:16%;\">Rate</th>");
        html.append("<th class=\"right\" style=\"width:18%;\">Taxable Value</th>");
        html.append("</tr></thead><tbody>");
        int i = 1;
        for (InvoiceLineItemDto item : invoice.lineItems()) {
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

    private void appendAmountSection(StringBuilder html, InvoiceDto invoice, BankDetails bank, boolean isTaxInvoice) {
        html.append("<table class=\"bare\" style=\"margin-top:12px;\"><tr><td class=\"bare\" style=\"vertical-align:top;\">");
        html.append("<div class=\"box\"><div class=\"box-title\">Amount Chargeable (in words)</div>");
        html.append("<div class=\"strong\">").append(DocumentFormatting.escape(AmountInWords.toWords(invoice.invoiceTotal()))).append("</div></div>");

        boolean hasBankInfo = bank != null && (DocumentFormatting.isPresent(bank.getBankName()) || DocumentFormatting.isPresent(bank.getUpiId()));
        if (hasBankInfo) {
            html.append("<div class=\"box\" style=\"margin-top:8px;\"><div class=\"box-title\">Payment Details</div>");
            if (DocumentFormatting.isPresent(bank.getBankName())) {
                appendInfoField(html, "Bank", bank.getBankName());
            }
            if (DocumentFormatting.isPresent(bank.getAccountNumber())) {
                appendInfoField(html, "A/C No.", bank.getAccountNumber());
            }
            if (DocumentFormatting.isPresent(bank.getIfscCode())) {
                appendInfoField(html, "IFSC", bank.getIfscCode());
            }
            if (DocumentFormatting.isPresent(bank.getUpiId())) {
                appendInfoField(html, "UPI", bank.getUpiId());
            }
            html.append("</div>");
        }

        html.append("</td><td class=\"bare\" style=\"width:82mm;vertical-align:top;\">");
        html.append("<table class=\"totals\">");
        if (invoice.discountAmount().signum() > 0) {
            appendTotalRow(html, "Subtotal", DocumentFormatting.formatInr(invoice.subtotal()), false);
            appendTotalRow(html, "Less Discount", "− " + DocumentFormatting.formatInr(invoice.discountAmount()), false);
            appendTotalRow(html, "Taxable Value", DocumentFormatting.formatInr(invoice.taxableValue()), true);
        } else {
            appendTotalRow(html, "Taxable Value", DocumentFormatting.formatInr(invoice.taxableValue()), false);
        }
        if (isTaxInvoice && !invoice.interState()) {
            appendTotalRow(html, "CGST @ " + invoice.cgstRate() + "%", DocumentFormatting.formatInr(invoice.cgstAmount()), false);
            appendTotalRow(html, "SGST @ " + invoice.sgstRate() + "%", DocumentFormatting.formatInr(invoice.sgstAmount()), false);
        }
        if (isTaxInvoice && invoice.interState()) {
            appendTotalRow(html, "IGST @ " + invoice.igstRate() + "%", DocumentFormatting.formatInr(invoice.igstAmount()), false);
        }
        if (!isTaxInvoice) {
            appendTotalRow(html, "GST", "Not applicable", false);
        }
        appendTotalRow(html, "Invoice Total", DocumentFormatting.formatInr(invoice.invoiceTotal()), true);
        if (invoice.tdsAmount().signum() > 0) {
            appendTotalRow(html, "Less TDS (" + invoice.tdsSection().getLabel() + ")", "− " + DocumentFormatting.formatInr(invoice.tdsAmount()),
                    false);
            appendTotalRow(html, "Net Payable to Supplier", DocumentFormatting.formatInr(invoice.netReceivable()), true);
        }
        if (invoice.amountPaid().signum() > 0) {
            appendTotalRow(html, "Received", DocumentFormatting.formatInr(invoice.amountPaid()), false);
            if (invoice.tdsWithheld().signum() > 0) {
                appendTotalRow(html, "TDS Withheld", DocumentFormatting.formatInr(invoice.tdsWithheld()), false);
            }
            appendTotalRow(html, "Balance Due", DocumentFormatting.formatInr(invoice.balanceDue()), true);
        }
        html.append("</table>");
        html.append("</td></tr></table>");
    }

    private void appendPaymentsReceived(StringBuilder html, InvoiceDto invoice) {
        if (invoice.payments().isEmpty()) {
            return;
        }
        html.append("<div class=\"section-title\">Payments Received</div>");
        html.append("<table class=\"items\"><thead><tr><th>Date</th><th>Method</th><th>Reference</th>")
                .append("<th class=\"right\">Cash Received</th><th class=\"right\">TDS Withheld</th></tr></thead><tbody>");
        for (InvoicePaymentDto payment : invoice.payments()) {
            html.append("<tr><td class=\"mono\">").append(DocumentFormatting.formatDate(payment.receivedOn())).append("</td><td>")
                    .append(DocumentFormatting.escape(payment.method().getLabel())).append("</td><td class=\"mono\">")
                    .append(DocumentFormatting.escape(DocumentFormatting.orDash(payment.reference()))).append("</td><td class=\"right mono\">")
                    .append(DocumentFormatting.formatInr(payment.amount())).append("</td><td class=\"right mono\">")
                    .append(payment.tdsWithheld() != null && payment.tdsWithheld().signum() > 0 ? DocumentFormatting.formatInr(payment.tdsWithheld())
                            : "—")
                    .append("</td></tr>");
        }
        html.append("</tbody></table>");
    }

    private void appendNotesAndTerms(StringBuilder html, InvoiceDto invoice) {
        if (invoice.notes() == null && invoice.terms() == null) {
            return;
        }
        html.append("<div class=\"section-title\">Notes &amp; Terms</div>");
        if (invoice.notes() != null) {
            html.append("<div>").append(DocumentFormatting.escape(invoice.notes()).replace("\n", "<br/>")).append("</div>");
        }
        if (invoice.terms() != null) {
            html.append("<div class=\"muted\">").append(DocumentFormatting.escape(invoice.terms()).replace("\n", "<br/>")).append("</div>");
        }
    }

    private void appendFooter(StringBuilder html, InvoiceDto invoice, String supplierName) {
        html.append("<table class=\"bare\" style=\"margin-top:22px;\"><tr><td class=\"bare\" style=\"vertical-align:bottom;width:60%;\">");
        html.append("<div class=\"muted small\">")
                .append(invoice.reverseCharge() ? "Tax is payable on reverse charge basis by the recipient."
                        : "Tax is not payable on reverse charge basis.")
                .append("<br/>This is a computer-generated ").append(invoice.taxInvoice() ? "tax invoice" : "bill of supply").append(".</div>");
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

    private void appendAddressLines(StringBuilder html, InvoicePartyDto party) {
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
