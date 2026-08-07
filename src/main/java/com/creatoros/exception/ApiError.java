package com.creatoros.exception;

import java.sql.Timestamp;
import java.util.Map;

import org.springframework.http.HttpStatus;

public record ApiError(Timestamp timestamp, int status, String error, String message, String errorCode, String path, Map<String, String> fieldErrors) {

    public static ApiError of(HttpStatus status, String message, String errorCode, String path) {
        return new ApiError(currentTimestamp(), status.value(), status.getReasonPhrase(), message, errorCode, path, null);
    }

    public static ApiError validation(String message, String path, Map<String, String> fieldErrors) {
        return new ApiError(currentTimestamp(), HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), message, "VALIDATION_FAILED",
                path, fieldErrors);
    }

    private static Timestamp currentTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }
}
