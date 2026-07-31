package com.creatoros.exception;

import lombok.Getter;

/**
 * A client-side error carrying a stable {@code errorCode} for the frontend to
 * branch on.
 */
@Getter
public class BadRequestException extends RuntimeException {

    private final String errorCode;

    public BadRequestException(String message) {
        this(message, "BAD_REQUEST");
    }

    public BadRequestException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
