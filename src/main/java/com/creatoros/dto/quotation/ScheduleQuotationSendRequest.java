package com.creatoros.dto.quotation;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ScheduleQuotationSendRequest(

        @NotBlank(message = "Recipient email is required") @Email(message = "Enter a valid email address") String toEmail,

        @NotNull(message = "Choose when to send this quotation") LocalDateTime sendAt) {
}
