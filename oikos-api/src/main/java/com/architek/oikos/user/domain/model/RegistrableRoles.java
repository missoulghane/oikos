package com.architek.oikos.user.domain.model;

import java.util.Set;

/**
 * Roles a caller may self-assign through public registration. Deliberately
 * excludes privileged roles (e.g. {@link Role#ROLE_ADMIN}, {@link Role#ROLE_MASTER}),
 * which can only be granted by an admin, so a registration request can never
 * escalate itself into a privileged role. Property-scoped roles (see
 * {@link PropertyRole}) are never self-registered either - they are granted
 * only through an explicit admin/board action against a specific property.
 */
public final class RegistrableRoles {

    private static final Set<Role> ALLOWED = Set.of(Role.ROLE_USER);

    private RegistrableRoles() {
    }

    public static boolean isAllowed(Role role) {
        return ALLOWED.contains(role);
    }
}
