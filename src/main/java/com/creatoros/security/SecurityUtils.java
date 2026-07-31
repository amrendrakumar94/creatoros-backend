package com.creatoros.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Resolves the calling creator from the SecurityContext.
 *
 * <p>
 * Every tenant-scoped repository call in later phases derives its
 * {@code creatorId} from here, never from a client-supplied parameter.
 */
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

    /**
     * @throws IllegalStateException if called outside an authenticated request.
     *             Endpoints reachable without a token must never call this.
     */
    public static Long currentCreatorId() {
        return currentPrincipal().map(CreatorPrincipal::getCreatorId)
                .orElseThrow(() -> new IllegalStateException("No authenticated creator in context"));
    }
}
