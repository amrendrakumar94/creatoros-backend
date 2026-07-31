package com.creatoros.dto.auth;

import com.creatoros.dto.CreatorProfileDto;

import java.time.Instant;

/**
 * Returned by login and verify-otp. The frontend stores {@code token} and hydrates its
 * creatorProfile state from {@code profile} without a second round trip.
 *
 * <p>{@code onboardingCompleted} tells the UI whether to land on the dashboard or the onboarding
 * flow.
 */
public record AuthResponse(String token, Instant expiresAt, boolean onboardingCompleted, CreatorProfileDto profile) {
}
