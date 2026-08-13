package com.creatoros.dto.quotation;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SendQuotationRequest(

        @NotBlank(message = "Recipient email is required") @Email(message = "Enter a valid email address") String toEmail) {
}
