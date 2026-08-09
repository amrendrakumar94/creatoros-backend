package com.creatoros.dto;

import com.creatoros.enums.Role;
import com.creatoros.enums.PermissionKey;

import java.util.Set;

/**
 * Returned by GET /api/v1/me so the app can validate a stored token on boot.
 */
public record CurrentUserResponse(

        Long id,

        String email,

        Role role,

        Set<PermissionKey> permissions,

        boolean onboardingCompleted,

        CreatorProfileDto profile) {
}
