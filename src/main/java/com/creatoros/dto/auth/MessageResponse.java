package com.creatoros.dto.auth;

/**
 * Simple acknowledgement for endpoints that issue an OTP rather than a session.
 */
public record MessageResponse(String message, String email) {
}
