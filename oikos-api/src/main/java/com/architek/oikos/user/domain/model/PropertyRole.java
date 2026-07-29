package com.architek.oikos.user.domain.model;

/**
 * Roles scoped to a single property, granted to an AppUser through one of
 * its linked Party rows (see {@link PropertyRoleGrant}) rather than through
 * the platform-wide {@link Role}/user_role table.
 */
public enum PropertyRole {
    ROLE_PROPERTY_MANAGER,
    ROLE_PROPERTY_ADMIN
}
