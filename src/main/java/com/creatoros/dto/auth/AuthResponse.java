package com.creatoros.dto.auth;

import java.sql.Timestamp;
import java.util.Set;

import com.creatoros.dto.CreatorProfileDto;
import com.creatoros.enums.PermissionKey;
import com.creatoros.enums.Role;

public record AuthResponse(

        String token,

        Timestamp expiresAt,

        Role role,

        Set<PermissionKey> permissions,

        boolean onboardingCompleted,

        CreatorProfileDto profile) {
}
