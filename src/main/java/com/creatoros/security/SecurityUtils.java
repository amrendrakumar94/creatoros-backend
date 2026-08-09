package com.creatoros.security;

import java.util.Optional;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.creatoros.enums.PermissionKey;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<CreatorPrincipal> currentPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof CreatorPrincipal principal)) {
            return Optional.empty();
        }
        return Optional.of(principal);
    }

    public static Long currentCreatorId() {
        return currentPrincipal().map(CreatorPrincipal::getCreatorId)
                .orElseThrow(() -> new IllegalStateException("No authenticated creator in context"));
    }

    public static Long currentTenantId() {
        return currentPrincipal().map(CreatorPrincipal::getTenantId)
                .orElseThrow(() -> new IllegalStateException("No authenticated creator in context"));
    }

    public static boolean isTenantOwner() {
        return currentPrincipal().map(principal -> principal.getCreatorId().equals(principal.getTenantId())).orElse(false);
    }

    public static Set<PermissionKey> currentPermissions() {
        return currentPrincipal().map(CreatorPrincipal::getPermissions)
                .orElseThrow(() -> new IllegalStateException("No authenticated creator in context"));
    }

    public static void requireAny(PermissionKey... permissions) {
        Set<PermissionKey> current = currentPermissions();
        for (PermissionKey permission : permissions) {
            if (current.contains(permission)) {
                return;
            }
        }
        throw new org.springframework.security.access.AccessDeniedException("Missing required permission");
    }

}
