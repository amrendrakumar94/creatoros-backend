package com.creatoros.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Payload for the resend-OTP and forgot-password endpoints.
 */
public record EmailOnlyRequest(

        @NotBlank(message = "Email is required") @Email(message = "Enter a valid email address") String email) {
}
