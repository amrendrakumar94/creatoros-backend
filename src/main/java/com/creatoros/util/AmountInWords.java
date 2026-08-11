package com.creatoros.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Indian-numbering (crore/lakh) amount-in-words, matching the frontend's `amountInWords.ts` exactly. */
public final class AmountInWords {

    private static final String[] ONES = { "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven",
            "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen" };

    private static final String[] TENS = { "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety" };

    private AmountInWords() {
    }

    public static String toWords(BigDecimal amount) {
        if (amount == null) {
            return "";
        }

        boolean negative = amount.signum() < 0;
        BigDecimal rounded = amount.abs().setScale(2, RoundingMode.HALF_UP);
        long rupees = rounded.longValue();
        int paise = rounded.subtract(BigDecimal.valueOf(rupees)).movePointRight(2).setScale(0, RoundingMode.HALF_UP).intValue();

        String head = integerToWords(rupees) + " Rupee" + (rupees == 1 ? "" : "s");
        String tail = paise > 0 ? " and " + underHundred(paise) + " Paise" : "";

        return (negative ? "Minus " : "") + head + tail + " Only";
    }

    private static String integerToWords(long value) {
        if (value == 0) {
            return "Zero";
        }

        long crore = value / 10000000;
        long lakh = value % 10000000 / 100000;
        long thousand = value % 100000 / 1000;
        long rest = value % 1000;

        StringBuilder parts = new StringBuilder();
        if (crore > 0) {
            appendPart(parts, integerToWords(crore) + " Crore");
        }
        if (lakh > 0) {
            appendPart(parts, underHundred(lakh) + " Lakh");
        }
        if (thousand > 0) {
            appendPart(parts, underHundred(thousand) + " Thousand");
        }
        if (rest > 0) {
            appendPart(parts, underThousand(rest));
        }
        return parts.toString();
    }

    private static String underThousand(long n) {
        long hundreds = n / 100;
        long rest = n % 100;
        StringBuilder parts = new StringBuilder();
        if (hundreds > 0) {
            appendPart(parts, ONES[(int) hundreds] + " Hundred");
        }
        if (rest > 0) {
            appendPart(parts, underHundred(rest));
        }
        return parts.toString();
    }

    private static String underHundred(long n) {
        if (n < 20) {
            return ONES[(int) n];
        }
        String tens = TENS[(int) (n / 10)];
        String ones = ONES[(int) (n % 10)];
        return ones.isEmpty() ? tens : tens + " " + ones;
    }

    private static void appendPart(StringBuilder builder, String part) {
        if (builder.length() > 0) {
            builder.append(' ');
        }
        builder.append(part);
    }
}
