package com.creatoros.exception;

import lombok.Getter;

@Getter
public class AccountNotVerifiedException extends RuntimeException {

    private final String email;

    public AccountNotVerifiedException(String email) {
        super("Account is not verified. Please enter the OTP sent to " + email);
        this.email = email;
    }
}
