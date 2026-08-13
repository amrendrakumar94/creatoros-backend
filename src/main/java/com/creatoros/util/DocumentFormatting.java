package com.creatoros.util;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.core.io.ClassPathResource;

/**
 * Formatting and layout primitives shared by every document renderer (invoice, quotation, ...)
 * so the ₹ grouping, date format, HTML escaping, and base CSS can't drift apart between documents.
 */
public final class DocumentFormatting {

    public static final String INK   = "#111827";
    public static final String MUTED = "#6B7280";
    public static final String LINE  = "#D1D5DB";
    public static final String WASH  = "#F3F4F6";

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d MMM yyyy");

    private DocumentFormatting() {
    }

    public static String formatDate(LocalDate date) {
        return date == null ? "-" : date.format(DATE_FORMAT);
    }

    public static String formatInr(BigDecimal amount) {
        BigDecimal value = amount == null ? BigDecimal.ZERO : amount;
        boolean negative = value.signum() < 0;
        String plain = value.abs().setScale(2, RoundingMode.HALF_UP).toPlainString();
        String[] parts = plain.split("\\.");
        return (negative ? "-" : "") + "₹" + groupIndian(parts[0]) + "." + parts[1];
    }

    private static String groupIndian(String digits) {
        if (digits.length() <= 3) {
            return digits;
        }
        String last3 = digits.substring(digits.length() - 3);
        String rest = digits.substring(0, digits.length() - 3);
        List<String> groups = new ArrayList<>();
        int i = rest.length();
        while (i > 0) {
            int start = Math.max(0, i - 2);
            groups.add(rest.substring(start, i));
            i = start;
        }
        Collections.reverse(groups);
        return String.join(",", groups) + "," + last3;
    }

    public static String firstNonBlank(String first, String second) {
        return isPresent(first) ? first : second;
    }

    public static boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }

    public static String orDash(String value) {
        return isPresent(value) ? value : "-";
    }

    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /**
     * Base14 PDF fonts have no glyph for ₹ (U+20B9) - it postdates them - so Flying Saucer needs
     * an embedded Unicode font. Extracted once to a real file because the legacy path-based font
     * API can't read straight out of a packaged jar's classpath.
     */
    public static String extractFont(String classpathResource) {
        try {
            ClassPathResource resource = new ClassPathResource(classpathResource);
            Path temp = Files.createTempFile("creatoros-font-", ".ttf");
            try (InputStream in = resource.getInputStream()) {
                Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
            }
            temp.toFile().deleteOnExit();
            return temp.toAbsolutePath().toString();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load PDF font: " + classpathResource, exception);
        }
    }

    /** Rules shared by every document (page setup, typography, boxes, tables, totals, signatory). */
    public static String baseCss() {
        return "@page{size:A4;margin:0;}"
                // No forced min-height on .page: a box-sizing:border-box element whose min-height
                // exactly equals the page height is a classic Flying Saucer trigger for a spurious,
                // near-empty second page (float rounding pushes it a hair over one page's content
                // box). Letting height follow content avoids that entirely.
                + "body{margin:0;background:#ffffff;}"
                + ".page{width:210mm;margin:0 auto;padding:14mm;box-sizing:border-box;background:#ffffff;color:" + INK
                + ";font-family:'Noto Sans',Helvetica,Arial,sans-serif;font-size:10.5px;line-height:1.45;}"
                + ".muted{color:" + MUTED + ";}"
                + ".small{font-size:9px;}"
                + ".right{text-align:right;}"
                + ".mono{font-family:ui-monospace,'Noto Sans',monospace;}"
                + ".strong{font-weight:800;}"
                + "table.bare{width:100%;border-collapse:collapse;}"
                + "td.bare{border:none;padding:0;vertical-align:top;}"
                + ".h1{font-size:17px;font-weight:800;letter-spacing:-0.01em;}"
                + ".h2{font-size:15px;font-weight:800;letter-spacing:0.08em;}"
                + "table.meta{margin-left:auto;margin-top:8px;border-collapse:collapse;}"
                + "table.meta td{padding:1px 0;}"
                + ".meta-label{color:" + MUTED + ";padding-right:10px;text-align:right;white-space:nowrap;}"
                + ".box{border:1px solid " + LINE + ";border-radius:4px;padding:7px 9px;}"
                + ".box-title,.section-title{font-size:9px;text-transform:uppercase;letter-spacing:0.06em;color:" + MUTED + ";}"
                + ".section-title{margin-top:14px;margin-bottom:4px;border-bottom:1px solid " + LINE + ";padding-bottom:2px;}"
                + ".box-name{font-weight:800;margin-top:2px;}"
                + ".field{display:flex;gap:6px;}"
                + ".field-label{color:" + MUTED + ";min-width:26mm;}"
                + ".field-value{font-weight:600;}"
                + "table.items{width:100%;border-collapse:collapse;margin-top:12px;}"
                + "table.items th{text-align:left;padding:5px 7px;border:1px solid " + LINE + ";font-size:9px;text-transform:uppercase;"
                + "letter-spacing:0.05em;color:" + MUTED + ";font-weight:700;background:" + WASH + ";}"
                + "table.items td{padding:5px 7px;border:1px solid " + LINE + ";}"
                + "table.totals{width:100%;border-collapse:collapse;}"
                + "table.totals td{padding:4px 8px;border:1px solid " + LINE + ";}"
                + "table.totals .totals-label{color:" + MUTED + ";}"
                + "table.totals tr.strong td{color:" + INK + ";font-weight:800;background:" + WASH + ";}"
                + ".signatory{border-top:1px solid " + INK + ";padding-top:4px;font-weight:700;min-width:58mm;text-align:center;}";
    }
}
