package com.creatoros.security;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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
}
