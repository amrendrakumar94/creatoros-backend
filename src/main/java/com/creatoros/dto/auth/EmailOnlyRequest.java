package com.creatoros.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailOnlyRequest(

        @NotBlank(message = "Email is required")

        @Email(message = "Enter a valid email address") String email) {
}
