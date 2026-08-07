package com.creatoros.dto.auth;

import java.sql.Timestamp;

import com.creatoros.dto.CreatorProfileDto;

public record AuthResponse(

        String token,

        Timestamp expiresAt,

        boolean onboardingCompleted,

        CreatorProfileDto profile) {
}
