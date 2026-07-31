package com.creatoros.exception;

/**
 * Wrong email or password. Deliberately does not distinguish between the two so
 * the endpoint cannot be used to enumerate registered accounts.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
