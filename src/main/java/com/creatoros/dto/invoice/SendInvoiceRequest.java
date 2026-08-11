package com.creatoros.dto.invoice;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SendInvoiceRequest(

        @NotBlank(message = "Recipient email is required") @Email(message = "Enter a valid email address") String toEmail) {
}
