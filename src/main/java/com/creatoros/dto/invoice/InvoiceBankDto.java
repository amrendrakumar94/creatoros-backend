package com.creatoros.dto.invoice;

public record InvoiceBankDto(

        String bankName,

        String accountNumber,

        String ifscCode,

        String upiId) {
}
