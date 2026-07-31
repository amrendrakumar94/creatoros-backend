package com.creatoros.exception;

import lombok.Getter;

/**
 * Login attempt against a PENDING account. The frontend uses the {@code email}
 * to jump straight to the OTP step and resend a code rather than showing a
 * dead-end error.
 */
@Getter
public class AccountNotVerifiedException extends RuntimeException {

    private final String email;

    public AccountNotVerifiedException(String email) {
        super("Account is not verified. Please enter the OTP sent to " + email);
        this.email = email;
    }
}
