package com.creatoros.exception;

import lombok.Getter;

/** Thrown when Razorpay itself rejects a call or is unreachable - distinct from a bad request the caller made. */
@Getter
public class PaymentGatewayException extends RuntimeException {

    private final String errorCode;

    public PaymentGatewayException(String message, Throwable cause) {
        this(message, "PAYMENT_GATEWAY_ERROR", cause);
    }

    public PaymentGatewayException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
