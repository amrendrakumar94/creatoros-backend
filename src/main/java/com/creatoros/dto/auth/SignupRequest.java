package com.creatoros.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(

        @NotBlank(message = "Name is required") @Size(max = 150, message = "Name must be at most 150 characters") String name,

        @NotBlank(message = "Email is required") @Email(message = "Enter a valid email address") @Size(max = 255) String email,

        @NotBlank(message = "Mobile number is required") @Size(max = 30) String phone,

        @NotBlank(message = "Password is required") @Size(min = 8, max = 100, message = "Password must be at least 8 characters") String password) {
}
