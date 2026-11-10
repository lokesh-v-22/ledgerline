package com.ledgerline.ledgerline.security;

import com.ledgerline.ledgerline.user.Role;
import java.util.UUID;
import org.springframework.security.core.Authentication;

public final class CurrentUser {

    private CurrentUser() {
    }

    public static UUID id(Authentication auth) {
        return (UUID) auth.getPrincipal();
    }

    public static boolean hasRole(Authentication auth, Role role) {
        String wanted = "ROLE_" + role.name();
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(wanted));
    }

    public static boolean canSeeAll(Authentication auth) {
        return hasRole(auth, Role.ADMIN) || hasRole(auth, Role.AUDITOR);
    }
}
