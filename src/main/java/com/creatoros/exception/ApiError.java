package com.creatoros.exception;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;

public record ApiError(Instant timestamp, int status, String error, String message, String errorCode, String path, Map<String, String> fieldErrors) {

    public static ApiError of(HttpStatus status, String message, String errorCode, String path) {
        return new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), message, errorCode, path, null);
    }

    public static ApiError validation(String message, String path, Map<String, String> fieldErrors) {
        return new ApiError(Instant.now(), HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), message, "VALIDATION_FAILED",
                path, fieldErrors);
    }
}
