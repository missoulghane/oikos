package com.architek.oikos.user.domain.model;

/**
 * Roles scoped to a single property, granted to an AppUser through one of
 * its linked Party rows (see {@link PropertyRoleGrant}) rather than through
 * the platform-wide {@link Role}/user_role table. Each role's actual
 * permission bundle is data-driven (role_permission table, resolved via
 * RolePermissionRepository) - the enum only carries the role's identity and
 * its {@link RoleCategory}, both of which business rules (e.g. the
 * property-creation cardinality check) need to branch on at compile time.
 */
public enum PropertyRole {
    PROPERTY_BOARD_ADMIN(RoleCategory.BOARD),
    PROPERTY_BOARD_MEMBER(RoleCategory.BOARD),
    PROPERTY_MANAGER_ADMIN(RoleCategory.MANAGER),
    PROPERTY_MANAGER_MEMBER(RoleCategory.MANAGER),
    PROPERTY_OWNER(RoleCategory.OWNER);

    private final RoleCategory category;

    PropertyRole(RoleCategory category) {
        this.category = category;
    }

    public RoleCategory category() {
        return category;
    }

    /** ADMIN-tier roles create/own a property outright; MEMBER-tier roles are only ever invited onto one. */
    public boolean isAdminTier() {
        return this == PROPERTY_BOARD_ADMIN || this == PROPERTY_MANAGER_ADMIN;
    }

    /** The MEMBER-tier counterpart of an ADMIN-tier role, for member-invitation flows. */
    public PropertyRole memberTierEquivalent() {
        return switch (this) {
            case PROPERTY_BOARD_ADMIN -> PROPERTY_BOARD_MEMBER;
            case PROPERTY_MANAGER_ADMIN -> PROPERTY_MANAGER_MEMBER;
            default -> throw new IllegalStateException(this + " has no member-tier equivalent");
        };
    }
}
