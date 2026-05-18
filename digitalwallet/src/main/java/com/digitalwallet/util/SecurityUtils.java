package com.digitalwallet.util;

import com.digitalwallet.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Thin static wrapper around SecurityContextHolder so controllers and
 * services don't have to repeat the same three-line cast every time.
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    /**
     * Returns the currently authenticated user's principal.
     * Throws if called outside an authenticated request — that's intentional.
     */
    public static UserPrincipal getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new IllegalStateException("No authenticated user in security context");
        }
        return principal;
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public static String getCurrentUserEmail() {
        return getCurrentUser().getEmail();
    }
}
