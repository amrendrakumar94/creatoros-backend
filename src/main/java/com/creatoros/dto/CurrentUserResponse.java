package com.creatoros.dto;

import com.creatoros.enums.Role;

/**
 * Returned by GET /api/v1/me so the app can validate a stored token on boot.
 */
public record CurrentUserResponse(

        Long id,

        String email,

        Role role,

        boolean onboardingCompleted,

        CreatorProfileDto profile) {
}
