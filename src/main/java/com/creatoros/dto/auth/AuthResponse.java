package com.creatoros.dto.auth;

import java.time.Instant;

import com.creatoros.dto.CreatorProfileDto;

public record AuthResponse(

        String token,

        Instant expiresAt,

        boolean onboardingCompleted,

        CreatorProfileDto profile) {
}
